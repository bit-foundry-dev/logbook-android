package com.bit.logbook.feature.auth.presentation.register;

public class RegisterState {
    private final boolean isLoading;
    private final boolean isSuccess;
    private final String error;
    private final String email;

    private RegisterState(boolean isLoading, boolean isSuccess, String error, String email) {
        this.isLoading = isLoading;
        this.isSuccess = isSuccess;
        this.error = error;
        this.email = email;
    }

    public static RegisterState loading() {
        return new RegisterState(true, false, null, null);
    }

    public static RegisterState success(String email) {
        return new RegisterState(false, true, null, email);
    }

    public static RegisterState error(String error) {
        return new RegisterState(false, false, error, null);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getError() {
        return error;
    }

    public String getEmail() {
        return email;
    }
}