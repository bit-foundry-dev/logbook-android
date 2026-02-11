package com.bit.logbook.feature.logManagement.domain.repository;

import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;

import java.time.LocalDate;
import java.util.List;

public interface LogRepository {
    List<Log> getAllLogs(LocalDate startDate, boolean isTrash) throws Exception;

    Log createLog(CreateLogRequest request) throws Exception;

    Log updateLog(UpdateLogRequest request, String id) throws Exception;

    void deleteLog(String id) throws Exception;
}
