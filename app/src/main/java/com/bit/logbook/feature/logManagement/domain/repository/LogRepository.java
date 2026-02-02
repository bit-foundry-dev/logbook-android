package com.bit.logbook.feature.logManagement.domain.repository;

import com.bit.logbook.feature.logManagement.domain.entity.Log;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface LogRepository {
    List<Log> getAllLogs(LocalDate startDate) throws Exception;
}