package com.bit.logbook.feature.auth.domain.usercase;


import com.bit.logbook.R;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.auth.domain.repository.AuthRepository;

import javax.inject.Inject;


public class RegisterUseCase extends UseCase<Void, RegisterUseCase.Params> {

    private final AuthRepository repository;
    private final StringProvider strings;

    @Inject
    public RegisterUseCase(AuthRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected Void execute(Params params) throws Exception {
        // Email validation
        if (params.email == null || params.email.trim().isEmpty()) {
            throw new IllegalArgumentException(strings.get(R.string.email_required));
        }
        if (!isValidEmail(params.email)) {
            throw new IllegalArgumentException(strings.get(R.string.invalid_email_format));
        }

        // Username validation
        if (params.username == null || params.username.trim().isEmpty()) {
            throw new IllegalArgumentException(strings.get(R.string.username_required));
        }
        if (params.username.length() < 3) {
            throw new IllegalArgumentException(strings.get(R.string.username_length));
        }

        // Password validation
        if (params.password == null || params.password.trim().isEmpty()) {
            throw new IllegalArgumentException(strings.get(R.string.password_required));
        }
        if (params.password.length() < 8) {
            throw new IllegalArgumentException(strings.get(R.string.password_length));
        }
        if (!hasCapitalLetter(params.password)) {
            throw new IllegalArgumentException(strings.get(R.string.password_one_capital_letter));
        }
        if (!hasNumber(params.password)) {
            throw new IllegalArgumentException(strings.get(R.string.password_one_number));
        }

        // Confirm password validation
        if (!params.password.equals(params.confirmPassword)) {
            throw new IllegalArgumentException(strings.get(R.string.passwords_mismatch));
        }

        repository.register(params.email, params.username, params.password);
        return null;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean hasCapitalLetter(String password) {
        return password.matches(".*[A-Z].*");
    }

    private boolean hasNumber(String password) {
        return password.matches(".*\\d.*");
    }

    public record Params(String email, String username, String password, String confirmPassword) {
    }
}