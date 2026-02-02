package com.bit.logbook.feature.logManagement.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bit.logbook.core.presentation.BaseViewModel;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.domain.usercase.GetAllLogsUseCase;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LogsViewModel extends BaseViewModel {

    private final GetAllLogsUseCase getAllLogsUseCase;

    private final MutableLiveData<LogState> logState = new MutableLiveData<>();

    @Inject
    public LogsViewModel(GetAllLogsUseCase getAllLogsUseCase) {
        this.getAllLogsUseCase = getAllLogsUseCase;
    }

    public LiveData<LogState> getLogState() {
        return logState;
    }

    public void getLogs(LocalDate startDate) {
        logState.setValue(LogState.loading());

        GetAllLogsUseCase.Params params = new GetAllLogsUseCase.Params(startDate);
        getAllLogsUseCase.executeAsync(params, new GetAllLogsUseCase.UseCaseCallback<>() {
            @Override
            public void onSuccess(List<Log> data) {
                logState.postValue(LogState.success(data));
            }

            @Override
            public void onError(Throwable error) {
                logState.postValue(LogState.error(error.getMessage()));
            }
        });
    }
}