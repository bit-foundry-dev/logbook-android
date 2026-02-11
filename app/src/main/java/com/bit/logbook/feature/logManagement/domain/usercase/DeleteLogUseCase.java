package com.bit.logbook.feature.logManagement.domain.usercase;

import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import javax.inject.Inject;

public class DeleteLogUseCase extends UseCase<Void, DeleteLogUseCase.Params> {

    private final LogRepository repository;
    private final StringProvider strings;

    @Inject
    public DeleteLogUseCase(LogRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected Void execute(DeleteLogUseCase.Params params) throws Exception {
        repository.deleteLog(params.id);
        return null;
    }

    public record Params(String id) {
    }
}
