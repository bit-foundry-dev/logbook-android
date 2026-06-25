package com.bit.logbook.core.data.remote;

import com.bit.logbook.core.data.ApiResponse;
import com.bit.logbook.core.domain.GenericException;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.LogDto;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.data.source.remote.LogApiService;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class LogRemoteDataSource {

    private final LogApiService apiService;
    private final StringProvider strings;

    @Inject
    public LogRemoteDataSource(LogApiService apiService, StringProvider strings) {
        this.apiService = apiService;
        this.strings = strings;
    }

    public List<LogDto> getLogs(LocalDate startDate, boolean isTrash) throws Exception {
        Response<ApiResponse<List<LogDto>>> response = apiService.getLogs(startDate, isTrash).execute();
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<List<LogDto>> logsResponse = response.body();
            if (logsResponse.isSuccess() && logsResponse.getData() != null) {
                return logsResponse.getData();
            }
            throw new GenericException(
                    logsResponse.getMessage() != null ? logsResponse.getMessage() : strings.get(
                            com.bit.logbook.R.string.fetch_logs_failed),
                    response.code()
            );
        }
        throw new GenericException(strings.get(com.bit.logbook.R.string.generic_error), response.code());
    }

    public LogDto createLog(CreateLogRequest request) throws Exception {
        Response<ApiResponse<LogDto>> response = apiService.createLog(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<LogDto> creationResponse = response.body();
            if (creationResponse.isSuccess() && creationResponse.getData() != null) {
                return creationResponse.getData();
            }
            throw new GenericException(
                    creationResponse.getMessage() != null ? creationResponse.getMessage() : strings.get(
                            com.bit.logbook.R.string.create_log_failed),
                    response.code()
            );
        }
        throw new GenericException(strings.get(com.bit.logbook.R.string.generic_error), response.code());
    }

    public LogDto updateLog(UpdateLogRequest request, String id) throws Exception {
        Response<ApiResponse<LogDto>> response = apiService.updateLog(request, id).execute();
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<LogDto> updateResponse = response.body();
            if (updateResponse.isSuccess() && updateResponse.getData() != null) {
                return updateResponse.getData();
            }
            throw new GenericException(
                    updateResponse.getMessage() != null ? updateResponse.getMessage() : strings.get(
                            com.bit.logbook.R.string.update_log_failed),
                    response.code()
            );
        }
        throw new GenericException(strings.get(com.bit.logbook.R.string.generic_error), response.code());
    }

    public void restoreLogs(List<String> ids) throws Exception {
        Response<ApiResponse<Void>> response = apiService.restoreLogs(ids).execute();
        if (!response.isSuccessful()) {
            throw new GenericException(strings.get(com.bit.logbook.R.string.generic_error), response.code());
        }
    }

    public void deleteLogs(List<String> ids) throws Exception {
        Response<ApiResponse<Void>> response = apiService.deleteLogs(ids).execute();
        if (!response.isSuccessful()) {
            throw new GenericException(strings.get(com.bit.logbook.R.string.generic_error), response.code());
        }
    }
}
