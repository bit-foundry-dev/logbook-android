package com.bit.logbook.feature.logManagement.domain.usercase;

import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.logManagement.data.model.UpdateLogRequest;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.bit.logbook.feature.logManagement.domain.repository.LogRepository;

import javax.inject.Inject;

public class UpdateLogUseCase extends UseCase<Log, UpdateLogUseCase.Params> {

    private final LogRepository repository;
    private final StringProvider strings;

    @Inject
    public UpdateLogUseCase(LogRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected Log execute(UpdateLogUseCase.Params params) throws Exception {
        return repository.updateLog(params.request, params.id);
    }

    public record Params(UpdateLogRequest request, String id) {
    }
}
