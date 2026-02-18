package com.bit.logbook.feature.logManagement.domain.usercase;

import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import java.util.List;

import javax.inject.Inject;

public class RestoreLogsUseCase extends UseCase<Void, RestoreLogsUseCase.Params> {

    private final LogRepository repository;
    private final StringProvider strings;

    @Inject
    public RestoreLogsUseCase(LogRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected Void execute(RestoreLogsUseCase.Params params) throws Exception {
        repository.restoreLogs(params.ids);
        return null;
    }

    public record Params(List<String> ids) {
    }
}
