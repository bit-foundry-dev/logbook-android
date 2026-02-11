package com.bit.logbook.feature.logManagement.domain.usercase;

import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

public class GetAllLogsUseCase extends UseCase<List<Log>, GetAllLogsUseCase.Params> {

    private final LogRepository repository;
    private final StringProvider strings;

    @Inject
    public GetAllLogsUseCase(LogRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected List<Log> execute(GetAllLogsUseCase.Params params) throws Exception {
        return repository.getAllLogs(params.startDate == null ? LocalDate.now() : params.startDate, params.isTrash);
    }

    public record Params(LocalDate startDate, boolean isTrash) {
    }
}
