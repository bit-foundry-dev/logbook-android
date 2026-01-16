package com.bit.logbook.feature.auth.presentation.login;

import com.bit.logbook.feature.auth.domain.entity.User;

public class LoginState {
    private final boolean isLoading;
    private final User user;
    private final String error;
    private final boolean forgotPasswordSuccess;
    private final boolean emailNotVerified;
    private final String unverifiedEmail;
    private final boolean resendVerificationSuccess;

    private LoginState(boolean isLoading, User user, String error, boolean forgotPasswordSuccess,
                       boolean emailNotVerified, String unverifiedEmail, boolean resendVerificationSuccess) {
        this.isLoading = isLoading;
        this.user = user;
        this.error = error;
        this.forgotPasswordSuccess = forgotPasswordSuccess;
        this.emailNotVerified = emailNotVerified;
        this.unverifiedEmail = unverifiedEmail;
        this.resendVerificationSuccess = resendVerificationSuccess;
    }

    public static LoginState loading() {
        return new LoginState(true, null, null, false, false, null, false);
    }

    public static LoginState success(User user) {
        return new LoginState(false, user, null, false, false, null, false);
    }

    public static LoginState error(String error) {
        return new LoginState(false, null, error, false, false, null, false);
    }

    public static LoginState emailNotVerified(String email) {
        return new LoginState(false, null, null, false, true, email, false);
    }

    public static LoginState forgotPasswordSuccess() {
        return new LoginState(false, null, null, true, false, null, false);
    }

    public static LoginState resendVerificationSuccess() {
        return new LoginState(false, null, null, false, false, null, true);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public User getUser() {
        return user;
    }

    public String getError() {
        return error;
    }

    public boolean isForgotPasswordSuccess() {
        return forgotPasswordSuccess;
    }

    public boolean isEmailNotVerified() {
        return emailNotVerified;
    }

    public String getUnverifiedEmail() {
        return unverifiedEmail;
    }

    public boolean isResendVerificationSuccess() {
        return resendVerificationSuccess;
    }
}