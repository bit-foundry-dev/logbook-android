package com.bit.logbook.feature.logManagement.domain.usercase;

import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.logManagement.data.model.CreateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

public class CreateLogUseCase extends UseCase<Log, CreateLogUseCase.Params> {

    private final LogRepository repository;
    private final StringProvider strings;

    @Inject
    public CreateLogUseCase(LogRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected Log execute(CreateLogUseCase.Params params) throws Exception {
        return repository.createLog(params.request);
    }

    public record Params(CreateLogRequest request) {
    }
}
