package com.bit.logbook.feature.logManagement.presentation.today;

import com.bit.logbook.feature.logManagement.domain.entity.Log;


public class LogCreationState {
    private final boolean isLoading;
    private final Log log;
    private final String error;
    private final String message;

    private LogCreationState(boolean isLoading, Log log, String error, String message) {
        this.isLoading = isLoading;
        this.log = log;
        this.error = error;
        this.message = message;
    }

    public static LogCreationState loading() {
        return new LogCreationState(true, null, null, null);
    }

    public static LogCreationState success(Log log) {
        return new LogCreationState(false, log, null, null);
    }

    public static LogCreationState success(Log log, String message) {
        return new LogCreationState(false, log, null, message);
    }

    public static LogCreationState error(String error) {
        return new LogCreationState(false, null, error, null);
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

    public String getMessage() {
        return message;
    }
}
