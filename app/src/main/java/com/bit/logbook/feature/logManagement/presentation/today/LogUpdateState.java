package com.bit.logbook.feature.logManagement.presentation.today;

import com.bit.logbook.feature.logManagement.domain.entity.Log;


public class LogUpdateState {
    private final boolean isLoading;
    private final Log log;
    private final String error;

    private LogUpdateState(boolean isLoading, Log log, String error) {
        this.isLoading = isLoading;
        this.log = log;
        this.error = error;
    }

    public static LogUpdateState loading() {
        return new LogUpdateState(true, null, null);
    }

    public static LogUpdateState success(Log log) {
        return new LogUpdateState(false, log, null);
    }

    public static LogUpdateState error(String error) {
        return new LogUpdateState(false, null, error);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public Log getLog() {
        return log;
    }

    public String getError() {
        return error;
    }
}