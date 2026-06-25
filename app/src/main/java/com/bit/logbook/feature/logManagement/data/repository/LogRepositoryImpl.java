package com.bit.logbook.feature.logManagement.data.repository;

import com.bit.logbook.core.data.local.dao.LogDao;
import com.bit.logbook.core.data.local.entity.LogEntity;
import com.bit.logbook.core.data.local.entity.SyncStatus;
import com.bit.logbook.core.data.remote.LogRemoteDataSource;
import com.bit.logbook.core.data.sync.SyncManager;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.LogDto;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class LogRepositoryImpl implements LogRepository {

    private final LogDao logDao;
    private final LogRemoteDataSource remoteDataSource;
    private final SyncManager syncManager;

    @Inject
    public LogRepositoryImpl(LogDao logDao, LogRemoteDataSource remoteDataSource,
                             SyncManager syncManager) {
        this.logDao = logDao;
        this.remoteDataSource = remoteDataSource;
        this.syncManager = syncManager;
    }

    @Override
    public List<Log> getAllLogs(LocalDate startDate, boolean isTrash) throws Exception {
        List<LogEntity> localLogs;
        if (startDate != null) {
            localLogs = logDao.getLogsByDate(startDate.toString(), isTrash);
        } else {
            localLogs = logDao.getAllLogs(isTrash);
        }

        if (syncManager.isOnline()) {
            try {
                List<LogDto> remoteLogs = remoteDataSource.getLogs(startDate, isTrash);
                updateLocalCache(remoteLogs, isTrash);
                if (startDate != null) {
                    return logDao.getLogsByDate(startDate.toString(), isTrash)
                            .stream().map(LogRepositoryImpl::mapToLog)
                            .collect(Collectors.toList());
                }
                return logDao.getAllLogs(isTrash)
                        .stream().map(LogRepositoryImpl::mapToLog)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                if (localLogs.isEmpty()) {
                    throw e;
                }
            }
        }

        return localLogs.stream().map(LogRepositoryImpl::mapToLog).collect(Collectors.toList());
    }

    @Override
    public Log createLog(CreateLogRequest request) throws Exception {
        String tempId = UUID.randomUUID().toString();
        String startDate = request.getStartDate();

        LogEntity entity = new LogEntity(
                tempId,
                request.getTitle(),
                request.getDescription(),
                request.getTag(),
                startDate,
                false,
                SyncStatus.PENDING_CREATE
        );
        logDao.insert(entity);

        if (syncManager.isOnline()) {
            try {
                LogDto created = remoteDataSource.createLog(request);
                LogEntity saved = logDao.getLogByServerId(tempId);
                if (saved != null) {
                    logDao.updateServerId(saved.getLocalId(), created.getId());
                }
                return new Log(
                        created.getId(),
                        created.getTitle(),
                        created.getDescription(),
                        created.getTag(),
                        created.getStartDate()
                );
            } catch (Exception e) {
                syncManager.onSyncStatusChanged();
                return mapToLog(entity);
            }
        }

        syncManager.onSyncStatusChanged();
        return mapToLog(entity);
    }

    @Override
    public Log updateLog(UpdateLogRequest request, String id) throws Exception {
        LogEntity existing = logDao.getLogByServerId(id);
        if (existing != null) {
            String title = request.getTitle() != null ? request.getTitle() : existing.getTitle();
            String description = request.getDescription() != null ? request.getDescription() : existing.getDescription();
            String tag = request.getTag() != null ? request.getTag() : existing.getTag();
            String startDate = request.getStartDate() != null ? request.getStartDate() : existing.getStartDate();

            existing.setTitle(title);
            existing.setDescription(description);
            existing.setTag(tag);
            existing.setStartDate(startDate);
            existing.setSyncStatus(SyncStatus.PENDING_UPDATE);
            logDao.update(existing);
        } else {
            LogEntity entity = new LogEntity(
                    id,
                    request.getTitle() != null ? request.getTitle() : "",
                    request.getDescription() != null ? request.getDescription() : "",
                    request.getTag() != null ? request.getTag() : "",
                    request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    false,
                    SyncStatus.PENDING_UPDATE
            );
            logDao.insert(entity);
        }

        if (syncManager.isOnline()) {
            try {
                LogDto updated = remoteDataSource.updateLog(request, id);
                LogEntity saved = logDao.getLogByServerId(id);
                if (saved != null) {
                    logDao.updateSyncStatusOnly(saved.getLocalId(), SyncStatus.SYNCED);
                }
                return new Log(
                        updated.getId(),
                        updated.getTitle(),
                        updated.getDescription(),
                        updated.getTag(),
                        updated.getStartDate()
                );
            } catch (Exception e) {
                syncManager.onSyncStatusChanged();
                LogEntity saved = logDao.getLogByServerId(id);
                if (saved != null) return mapToLog(saved);
                throw e;
            }
        }

        syncManager.onSyncStatusChanged();
        LogEntity saved = logDao.getLogByServerId(id);
        if (saved != null) return mapToLog(saved);
        return new Log(id, "", "", null, LocalDateTime.now());
    }

    @Override
    public void restoreLogs(List<String> ids) throws Exception {
        for (String id : ids) {
            LogEntity entity = logDao.getLogByServerId(id);
            if (entity != null) {
                entity.setTrash(false);
                entity.setSyncStatus(SyncStatus.PENDING_RESTORE);
                logDao.update(entity);
            }
        }

        if (syncManager.isOnline()) {
            try {
                remoteDataSource.restoreLogs(ids);
                for (String id : ids) {
                    LogEntity entity = logDao.getLogByServerId(id);
                    if (entity != null) {
                        logDao.updateSyncStatus(entity.getLocalId(), SyncStatus.SYNCED, false);
                    }
                }
                return;
            } catch (Exception e) {
                syncManager.onSyncStatusChanged();
                if (!isNetworkError(e)) throw e;
                return;
            }
        }

        syncManager.onSyncStatusChanged();
    }

    @Override
    public void deleteLogs(List<String> ids) throws Exception {
        for (String id : ids) {
            LogEntity entity = logDao.getLogByServerId(id);
            if (entity != null) {
                entity.setTrash(true);
                entity.setSyncStatus(SyncStatus.PENDING_DELETE);
                logDao.update(entity);
            }
        }

        if (syncManager.isOnline()) {
            try {
                remoteDataSource.deleteLogs(ids);
                for (String id : ids) {
                    LogEntity entity = logDao.getLogByServerId(id);
                    if (entity != null) {
                        logDao.deleteByLocalId(entity.getLocalId());
                    }
                }
            } catch (Exception e) {
                syncManager.onSyncStatusChanged();
                if (!isNetworkError(e)) throw e;
            }
        } else {
            syncManager.onSyncStatusChanged();
        }
    }

    private void updateLocalCache(List<LogDto> remoteLogs, boolean isTrash) {
        for (LogDto dto : remoteLogs) {
            LogEntity existing = logDao.getLogByServerId(dto.getId());
            LogEntity entity = new LogEntity(
                    dto.getId(),
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getTag(),
                    dto.getStartDate() != null ? dto.getStartDate().toString() : null,
                    isTrash,
                    SyncStatus.SYNCED
            );
            if (existing != null) {
                if (existing.getSyncStatus() == SyncStatus.SYNCED) {
                    entity.setLocalId(existing.getLocalId());
                    logDao.update(entity);
                }
            } else {
                logDao.insert(entity);
            }
        }
    }

    private boolean isNetworkError(Exception e) {
        return e instanceof java.net.UnknownHostException
                || e instanceof java.net.ConnectException
                || e instanceof java.net.SocketTimeoutException;
    }

    private static Log mapToLog(LogEntity entity) {
        return new Log(
                entity.getId() != null ? entity.getId() : "",
                entity.getTitle(),
                entity.getDescription(),
                entity.getTag(),
                entity.getStartDate()
        );
    }
}
