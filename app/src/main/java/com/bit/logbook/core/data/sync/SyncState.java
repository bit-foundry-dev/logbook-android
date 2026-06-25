package com.bit.logbook.core.data.sync;

public class SyncState {
    private final Status status;
    private final int pendingCount;
    private final String lastSyncTime;
    private final String message;

    public SyncState(Status status, int pendingCount, String lastSyncTime, String message) {
        this.status = status;
        this.pendingCount = pendingCount;
        this.lastSyncTime = lastSyncTime;
        this.message = message;
    }

    public Status getStatus() { return status; }
    public int getPendingCount() { return pendingCount; }
    public String getLastSyncTime() { return lastSyncTime; }
    public String getMessage() { return message; }

    public enum Status {
        SYNCED,
        SYNCING,
        PENDING,
        ERROR
    }

    public static SyncState synced(String lastSyncTime) {
        return new SyncState(Status.SYNCED, 0, lastSyncTime, null);
    }

    public static SyncState syncing() {
        return new SyncState(Status.SYNCING, 0, null, null);
    }

    public static SyncState pending(int count) {
        return new SyncState(Status.PENDING, count, null, null);
    }

    public static SyncState error(String message) {
        return new SyncState(Status.ERROR, 0, null, message);
    }
}
