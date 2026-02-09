package com.bit.logbook.feature.logManagement.presentation.today;

import com.bit.logbook.feature.logManagement.domain.entity.Log;


public class LogCreationState {
    private final boolean isLoading;
    private final Log log;
    private final String error;

    private LogCreationState(boolean isLoading, Log log, String error) {
        this.isLoading = isLoading;
        this.log = log;
        this.error = error;
    }

    public static LogCreationState loading() {
        return new LogCreationState(true, null, null);
    }

    public static LogCreationState success(Log log) {
        return new LogCreationState(false, log, null);
    }

    public static LogCreationState error(String error) {
        return new LogCreationState(false, null, error);
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