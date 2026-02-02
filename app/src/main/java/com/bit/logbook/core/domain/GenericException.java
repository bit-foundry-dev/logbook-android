package com.bit.logbook.core.domain;

public class GenericException extends Exception {
    private final int errorCode;

    public GenericException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }


    public boolean isForbidden() {
        return errorCode == 403;
    }

    public boolean isUnauthorized() {
        return errorCode == 401;
    }

    public boolean isBadRequest() {
        return errorCode == 400;
    }

    public boolean isConflict() {
        return errorCode == 409;
    }

    public boolean isServerError() {
        return errorCode >= 500;
    }
}
