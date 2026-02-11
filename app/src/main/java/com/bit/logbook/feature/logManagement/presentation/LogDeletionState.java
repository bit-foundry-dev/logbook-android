package com.bit.logbook.feature.logManagement.presentation;

public class LogDeletionState {
    private final boolean isLoading;
    private final boolean isDeleted;
    private final String error;

    private LogDeletionState(boolean isLoading, boolean isDeleted, String error) {
        this.isLoading = isLoading;
        this.isDeleted = isDeleted;
        this.error = error;
    }

    public static LogDeletionState loading() {
        return new LogDeletionState(true, false, null);
    }

    public static LogDeletionState success() {
        return new LogDeletionState(false, true, null);
    }

    public static LogDeletionState error(String error) {
        return new LogDeletionState(false, false, error);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getError() {
        return error;
    }
}
