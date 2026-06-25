package com.bit.logbook.core.data.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bit.logbook.core.data.local.dao.LogDao;
import com.bit.logbook.core.data.local.entity.LogEntity;
import com.bit.logbook.core.data.local.entity.SyncStatus;
import com.bit.logbook.core.data.remote.LogRemoteDataSource;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.LogDto;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SyncManager {

    private final Context context;
    private final LogDao logDao;
    private final LogRemoteDataSource remoteDataSource;
    private final ConnectivityManager connectivityManager;
    private final Executor executor;
    private final MutableLiveData<SyncState> syncState = new MutableLiveData<>();
    private boolean isOnline;
    private String lastSyncTime = "";

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isOnline = true;
            syncPendingChanges();
        }

        @Override
        public void onLost(Network network) {
            isOnline = false;
            updatePendingState();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            boolean wasOnline = isOnline;
            isOnline = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            if (isOnline && !wasOnline) {
                syncPendingChanges();
            } else if (!isOnline) {
                updatePendingState();
            }
        }
    };

    @Inject
    public SyncManager(@ApplicationContext Context context, LogDao logDao,
                       LogRemoteDataSource remoteDataSource) {
        this.context = context;
        this.logDao = logDao;
        this.remoteDataSource = remoteDataSource;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.executor = Executors.newSingleThreadExecutor();
        this.isOnline = checkConnectivity();
        syncState.setValue(SyncState.synced(lastSyncTime));
        registerNetworkCallback();
    }

    public LiveData<SyncState> getSyncState() {
        return syncState;
    }

    public void syncNow() {
        if (isOnline) {
            syncPendingChanges();
        }
    }

    public boolean isOnline() {
        return isOnline;
    }

    private boolean checkConnectivity() {
        if (connectivityManager == null) return false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void registerNetworkCallback() {
        if (connectivityManager != null) {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

    public void unregisterNetworkCallback() {
        if (connectivityManager != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }
    }

    private void updatePendingState() {
        executor.execute(() -> {
            int pending = logDao.getPendingSyncCount();
            if (pending > 0) {
                syncState.postValue(SyncState.pending(pending));
            } else {
                syncState.postValue(SyncState.synced(lastSyncTime));
            }
        });
    }

    public void onSyncStatusChanged() {
        if (isOnline) {
            syncPendingChanges();
        } else {
            updatePendingState();
        }
    }

    private void syncPendingChanges() {
        syncState.postValue(SyncState.syncing());
        executor.execute(() -> {
            try {
                List<LogEntity> pending = logDao.getPendingSyncLogs();
                if (pending.isEmpty()) {
                    lastSyncTime = getCurrentTimestamp();
                    syncState.postValue(SyncState.synced(lastSyncTime));
                    return;
                }

                for (LogEntity entity : pending) {
                    processPendingEntity(entity);
                }

                lastSyncTime = getCurrentTimestamp();
                int remaining = logDao.getPendingSyncCount();
                if (remaining > 0) {
                    syncState.postValue(SyncState.pending(remaining));
                } else {
                    syncState.postValue(SyncState.synced(lastSyncTime));
                }
            } catch (Exception e) {
                syncState.postValue(SyncState.error(
                        e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Sync failed"
                ));
            }
        });
    }

    private void processPendingEntity(LogEntity entity) {
        try {
            switch (entity.getSyncStatus()) {
                case PENDING_CREATE:
                    handlePendingCreate(entity);
                    break;
                case PENDING_UPDATE:
                    handlePendingUpdate(entity);
                    break;
                case PENDING_DELETE:
                    handlePendingDelete(entity);
                    break;
                case PENDING_RESTORE:
                    handlePendingRestore(entity);
                    break;
                case SYNCED:
                    break;
            }
        } catch (Exception e) {
            if (!isNetworkError(e)) {
                logDao.updateSyncStatus(entity.getLocalId(), SyncStatus.SYNCED, entity.isTrash());
            }
        }
    }

    private void handlePendingCreate(LogEntity entity) throws Exception {
        if (entity.getId() == null) return;
        CreateLogRequest request = new CreateLogRequest(
                entity.getTitle(),
                entity.getDescription() != null ? entity.getDescription() : "",
                entity.getTag() != null ? entity.getTag() : "",
                entity.getStartDate()
        );
        LogDto created = remoteDataSource.createLog(request);
        logDao.updateServerId(entity.getLocalId(), created.getId());
    }

    private void handlePendingUpdate(LogEntity entity) throws Exception {
        if (entity.getId() == null) return;
        UpdateLogRequest request = new UpdateLogRequest();
        request.setTitle(entity.getTitle());
        request.setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        request.setTag(entity.getTag() != null ? entity.getTag() : "");
        request.setStartDate(entity.getStartDate());
        request.setTrash(false);
        remoteDataSource.updateLog(request, entity.getId());
        logDao.updateSyncStatusOnly(entity.getLocalId(), SyncStatus.SYNCED);
    }

    private void handlePendingDelete(LogEntity entity) throws Exception {
        if (entity.getId() != null) {
            remoteDataSource.deleteLogs(List.of(entity.getId()));
        }
        logDao.deleteByLocalId(entity.getLocalId());
    }

    private void handlePendingRestore(LogEntity entity) throws Exception {
        if (entity.getId() == null) {
            logDao.deleteByLocalId(entity.getLocalId());
            return;
        }
        remoteDataSource.restoreLogs(List.of(entity.getId()));
        logDao.updateSyncStatus(entity.getLocalId(), SyncStatus.SYNCED, false);
    }

    private boolean isNetworkError(Exception e) {
        return e instanceof UnknownHostException
                || e instanceof java.net.ConnectException
                || e instanceof java.net.SocketTimeoutException
                || (e.getCause() != null && isNetworkError(new Exception(e.getCause())));
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }
}
