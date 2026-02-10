package com.bit.logbook.feature.logManagement.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bit.logbook.R;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.presentation.BaseViewModel;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.domain.usercase.CreateLogUseCase;
import com.bit.logbook.feature.logManagement.domain.usercase.GetAllLogsUseCase;
import com.bit.logbook.feature.logManagement.domain.usercase.UpdateLogUseCase;
import com.bit.logbook.feature.logManagement.presentation.today.LogCreationState;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LogsViewModel extends BaseViewModel {

    private final GetAllLogsUseCase getAllLogsUseCase;
    private final CreateLogUseCase createLogUseCase;
    private final UpdateLogUseCase updateLogUseCase;
    private final StringProvider stringProvider;

    private final MutableLiveData<LogState> logState = new MutableLiveData<>();
    private final MutableLiveData<LogCreationState> logCreationState = new MutableLiveData<>();

    @Inject
    public LogsViewModel(GetAllLogsUseCase getAllLogsUseCase, CreateLogUseCase createLogUseCase, UpdateLogUseCase updateLogUseCase, StringProvider stringProvider) {
        this.getAllLogsUseCase = getAllLogsUseCase;
        this.createLogUseCase = createLogUseCase;
        this.updateLogUseCase = updateLogUseCase;
        this.stringProvider = stringProvider;
    }

    public LiveData<LogState> getLogState() {
        return logState;
    }

    public MutableLiveData<LogCreationState> getLogCreationState() {
        return logCreationState;
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
                if (error instanceof IOException) {
                    logState.postValue(LogState.error(stringProvider.get(R.string.error_no_network)));
                } else {
                    logState.postValue(LogState.error(stringProvider.get(R.string.generic_error)));
                }
            }
        });
    }

    public void createLog(CreateLogRequest request) {
        logCreationState.setValue(LogCreationState.loading());

        CreateLogUseCase.Params params = new CreateLogUseCase.Params(request);
        createLogUseCase.executeAsync(params, new CreateLogUseCase.UseCaseCallback<>() {
            @Override
            public void onSuccess(Log newLog) {
                logCreationState.postValue(LogCreationState.success(newLog));
            }

            @Override
            public void onError(Throwable error) {
                if (error instanceof IOException) {
                    logCreationState.postValue(LogCreationState.error(stringProvider.get(R.string.error_no_network)));
                } else {
                    logCreationState.postValue(LogCreationState.error(stringProvider.get(R.string.create_log_failed)));
                }
            }
        });
    }

    public void updateLog(UpdateLogRequest request, String id) {
        logCreationState.setValue(LogCreationState.loading());

        UpdateLogUseCase.Params params = new UpdateLogUseCase.Params(request, id);
        updateLogUseCase.executeAsync(params, new UpdateLogUseCase.UseCaseCallback<>() {
            @Override
            public void onSuccess(Log newLog) {
                logCreationState.postValue(LogCreationState.success(newLog));
            }

            @Override
            public void onError(Throwable error) {
                if (error instanceof IOException) {
                    logCreationState.postValue(LogCreationState.error(stringProvider.get(R.string.error_no_network)));
                } else {
                    logCreationState.postValue(LogCreationState.error(stringProvider.get(R.string.update_log_failed)));
                }
            }
        });
    }

    public void resetLogCreationState() {
        logCreationState.setValue(null);
    }
}