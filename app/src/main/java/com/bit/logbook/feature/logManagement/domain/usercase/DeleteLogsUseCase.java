package com.bit.logbook.feature.logManagement.domain.usercase;

import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import java.util.List;

import javax.inject.Inject;

public class DeleteLogsUseCase extends UseCase<Void, DeleteLogsUseCase.Params> {

    private final LogRepository repository;
    private final StringProvider strings;

    @Inject
    public DeleteLogsUseCase(LogRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected Void execute(DeleteLogsUseCase.Params params) throws Exception {
        repository.deleteLogs(params.ids);
        return null;
    }

    public record Params(List<String> ids) {
    }
}
