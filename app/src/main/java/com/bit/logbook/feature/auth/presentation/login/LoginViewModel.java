package com.bit.logbook.feature.auth.presentation.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bit.logbook.core.presentation.BaseViewModel;
import com.bit.logbook.feature.auth.domain.entity.User;
import com.bit.logbook.feature.auth.domain.exception.AuthException;
import com.bit.logbook.feature.auth.domain.usercase.ForgotPasswordUseCase;
import com.bit.logbook.feature.auth.domain.usercase.LoginUseCase;
import com.bit.logbook.feature.auth.domain.usercase.ResendVerificationUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends BaseViewModel {

    private final LoginUseCase loginUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResendVerificationUseCase resendVerificationUseCase) {
        this.loginUseCase = loginUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String emailOrUsername, String password) {
        loginState.setValue(LoginState.loading());

        LoginUseCase.Params params = new LoginUseCase.Params(emailOrUsername, password);
        loginUseCase.executeAsync(params, new LoginUseCase.UseCaseCallback<>() {
            @Override
            public void onSuccess(User data) {
                loginState.postValue(LoginState.success(data));
            }

            @Override
            public void onError(Throwable error) {
                if (error instanceof AuthException authException) {
                    if (authException.isEmailNotVerified()) {
                        loginState.postValue(LoginState.emailNotVerified(authException.getEmail()));
                    } else {
                        loginState.postValue(LoginState.error(authException.getMessage()));
                    }
                } else {
                    loginState.postValue(LoginState.error(error.getMessage()));
                }
            }
        });
    }

    public void forgotPassword(String email) {
        loginState.setValue(LoginState.loading());

        ForgotPasswordUseCase.Params params = new ForgotPasswordUseCase.Params(email);
        forgotPasswordUseCase.executeAsync(params, new ForgotPasswordUseCase.UseCaseCallback<>() {
            @Override
            public void onSuccess(Void data) {
                loginState.postValue(LoginState.forgotPasswordSuccess());
            }

            @Override
            public void onError(Throwable error) {
                loginState.postValue(LoginState.error(error.getMessage()));
            }
        });
    }

    public void resendVerificationEmail(String email) {
        loginState.setValue(LoginState.loading());

        ResendVerificationUseCase.Params params = new ResendVerificationUseCase.Params(email);
        resendVerificationUseCase.executeAsync(params, new ResendVerificationUseCase.UseCaseCallback<>() {
            @Override
            public void onSuccess(Void data) {
                loginState.postValue(LoginState.resendVerificationSuccess());
            }

            @Override
            public void onError(Throwable error) {
                loginState.postValue(LoginState.error(error.getMessage()));
            }
        });
    }
}