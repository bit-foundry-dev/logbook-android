package com.bit.logbook.feature.auth.domain.usercase;

import com.bit.logbook.R;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.auth.domain.repository.AuthRepository;

import javax.inject.Inject;

public class ResendVerificationUseCase extends UseCase<Void, ResendVerificationUseCase.Params> {

    private final AuthRepository repository;
    private final StringProvider strings;

    @Inject
    public ResendVerificationUseCase(AuthRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected Void execute(Params params) throws Exception {
        if (params.email == null || params.email.trim().isEmpty()) {
            throw new IllegalArgumentException(strings.get(R.string.email_required));
        }
        if (!isValidEmail(params.email)) {
            throw new IllegalArgumentException(strings.get(R.string.invalid_email_format));
        }

        repository.resendVerificationEmail(params.email);
        return null;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public record Params(String email) {
    }
}