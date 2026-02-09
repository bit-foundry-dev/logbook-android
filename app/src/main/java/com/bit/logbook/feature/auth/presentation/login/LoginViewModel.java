package com.bit.logbook.feature.auth.presentation.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bit.logbook.R;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.presentation.BaseViewModel;
import com.bit.logbook.feature.auth.domain.entity.User;
import com.bit.logbook.feature.auth.domain.exception.AuthException;
import com.bit.logbook.feature.auth.domain.usercase.ForgotPasswordUseCase;
import com.bit.logbook.feature.auth.domain.usercase.LoginUseCase;
import com.bit.logbook.feature.auth.domain.usercase.ResendVerificationUseCase;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends BaseViewModel {

    private final LoginUseCase loginUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final StringProvider stringProvider;

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResendVerificationUseCase resendVerificationUseCase,
                          StringProvider stringProvider) {
        this.loginUseCase = loginUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
        this.stringProvider = stringProvider;
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
                } else if (error instanceof IOException) {
                    loginState.postValue(LoginState.error(stringProvider.get(R.string.error_no_network)));
                } else {
                    loginState.postValue(LoginState.error(stringProvider.get(R.string.generic_error)));
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
                if (error instanceof IOException) {
                    loginState.postValue(LoginState.error(stringProvider.get(R.string.error_no_network)));
                } else {
                    loginState.postValue(LoginState.error(stringProvider.get(R.string.generic_error)));
                }
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
                if (error instanceof IOException) {
                    loginState.postValue(LoginState.error(stringProvider.get(R.string.error_no_network)));
                } else {
                    loginState.postValue(LoginState.error(stringProvider.get(R.string.generic_error)));
                }
            }
        });
    }
}