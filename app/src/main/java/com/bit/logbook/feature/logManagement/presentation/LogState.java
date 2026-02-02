package com.bit.logbook.feature.logManagement.presentation;

import com.bit.logbook.feature.logManagement.domain.entity.Log;

import java.util.List;

public class LogState {
    private final boolean isLoading;
    private final List<Log> logs;
    private final String error;

    private LogState(boolean isLoading, List<Log> logs, String error) {
        this.isLoading = isLoading;
        this.logs = logs;
        this.error = error;
    }

    public static LogState loading() {
        return new LogState(true, null, null);
    }

    public static LogState success(List<Log> logs) {
        return new LogState(false, logs, null);
    }

    public static LogState error(String error) {
        return new LogState(false, null, error);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public String getError() {
        return error;
    }
}