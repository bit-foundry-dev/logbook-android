package com.bit.logbook.feature.logManagement.data.repository;

import android.content.Context;

import com.bit.logbook.R;
import com.bit.logbook.core.data.ApiResponse;
import com.bit.logbook.core.data.BaseRepository;
import com.bit.logbook.core.domain.GenericException;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.LogDto;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.data.source.remote.LogApiService;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Response;

public class LogRepositoryImpl extends BaseRepository implements LogRepository {

    private final LogApiService apiService;
    private final StringProvider strings;

    @Inject
    public LogRepositoryImpl(LogApiService apiService, @ApplicationContext Context context, StringProvider strings) {
        this.apiService = apiService;
        this.strings = strings;
    }

    @Override
    public List<Log> getAllLogs(LocalDate startDate, boolean isTrash) throws Exception {
        Response<ApiResponse<List<LogDto>>> response = apiService.getLogs(startDate, isTrash).execute();

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<List<LogDto>> logsResponse = response.body();
            if (logsResponse.isSuccess() && logsResponse.getData() != null) {
                return mapToLogs(logsResponse.getData());
            } else {
                throw new GenericException(
                        logsResponse.getMessage() != null ? logsResponse.getMessage() : strings.get(R.string.fetch_logs_failed),
                        response.code()
                );
            }
        } else {
            throw new GenericException(strings.get(R.string.generic_error), response.code());
        }
    }

    @Override
    public Log createLog(CreateLogRequest request) throws Exception {
        Response<ApiResponse<LogDto>> response = apiService.createLog(request).execute();

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<LogDto> creationRequest = response.body();
            if (creationRequest.isSuccess() && creationRequest.getData() != null) {
                return mapToLog(creationRequest.getData());
            } else {
                throw new GenericException(
                        creationRequest.getMessage() != null ? creationRequest.getMessage() : strings.get(R.string.create_log_failed),
                        response.code()
                );
            }
        } else {
            throw new GenericException(strings.get(R.string.generic_error), response.code());
        }
    }

    @Override
    public Log updateLog(UpdateLogRequest request, String id) throws Exception {
        Response<ApiResponse<LogDto>> response = apiService.updateLog(request, id).execute();

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<LogDto> updateResponse = response.body();
            if (updateResponse.isSuccess() && updateResponse.getData() != null) {
                return mapToLog(updateResponse.getData());
            } else {
                throw new GenericException(
                        updateResponse.getMessage() != null ? updateResponse.getMessage() : strings.get(R.string.update_log_failed),
                        response.code()
                );
            }
        } else {
            throw new GenericException(strings.get(R.string.generic_error), response.code());
        }
    }

    @Override
    public void deleteLog(String id) throws Exception {
        Response<ApiResponse<Void>> response = apiService.deleteLog(id).execute();

        if (!response.isSuccessful()) {
            throw new GenericException(strings.get(R.string.generic_error), response.code());
        }
    }

    private List<Log> mapToLogs(List<LogDto> dtos) {
        return dtos.stream().map(dto -> new Log(
                dto.getId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getTag(),
                dto.getStartDate()
        )).collect(Collectors.toList());
    }

    private Log mapToLog(LogDto dto) {
        return new Log(
                dto.getId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getTag(),
                dto.getStartDate()
        );
    }
}
