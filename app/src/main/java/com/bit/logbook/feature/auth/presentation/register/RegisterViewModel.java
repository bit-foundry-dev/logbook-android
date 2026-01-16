package com.bit.logbook.feature.auth.presentation.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bit.logbook.core.presentation.BaseViewModel;
import com.bit.logbook.feature.auth.domain.usercase.RegisterUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends BaseViewModel {

    private final RegisterUseCase registerUseCase;

    private final MutableLiveData<RegisterState> registerState = new MutableLiveData<>();

    @Inject
    public RegisterViewModel(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    public LiveData<RegisterState> getRegisterState() {
        return registerState;
    }

    public void register(String email, String username, String password, String confirmPassword) {
        registerState.setValue(RegisterState.loading());

        RegisterUseCase.Params params = new RegisterUseCase.Params(email, username, password, confirmPassword);
        registerUseCase.executeAsync(params, new RegisterUseCase.UseCaseCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                registerState.postValue(RegisterState.success(email));
            }

            @Override
            public void onError(Throwable error) {
                registerState.postValue(RegisterState.error(error.getMessage()));
            }
        });
    }
}