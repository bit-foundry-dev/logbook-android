package com.bit.logbook.feature.auth.domain.exception;

public class AuthException extends Exception {
    private final int errorCode;
    private final String email;

    public AuthException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.email = null;
    }

    public AuthException(String message, int errorCode, String email) {
        super(message);
        this.errorCode = errorCode;
        this.email = email;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailNotVerified() {
        return errorCode == 403;
    }

    public boolean isInvalidCredentials() {
        return errorCode == 401;
    }

    public boolean isUsernameOrEmailTaken() {
        return errorCode == 409;
    }
}
