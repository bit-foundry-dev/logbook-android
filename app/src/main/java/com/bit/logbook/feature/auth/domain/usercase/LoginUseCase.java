package com.bit.logbook.feature.auth.domain.usercase;

import com.bit.logbook.R;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.domain.UseCase;
import com.bit.logbook.feature.auth.domain.entity.User;
import com.bit.logbook.feature.auth.domain.repository.AuthRepository;

import javax.inject.Inject;

public class LoginUseCase extends UseCase<User, LoginUseCase.Params> {

    private final AuthRepository repository;
    private final StringProvider strings;

    @Inject
    public LoginUseCase(AuthRepository repository, StringProvider strings) {
        this.repository = repository;
        this.strings = strings;
    }

    @Override
    protected User execute(Params params) throws Exception {
        if (params.emailOrUsername == null || params.emailOrUsername.trim().isEmpty()) {
            throw new IllegalArgumentException(strings.get(R.string.email_username_required));
        }
        if (params.password == null || params.password.trim().isEmpty()) {
            throw new IllegalArgumentException(strings.get(R.string.password_required));
        }

        User user = repository.login(params.emailOrUsername, params.password);

        // Save user credentials
        repository.saveUserToken(user.getFullToken());
        repository.saveUserId(user.getUserId());

        return user;
    }

    public record Params(String emailOrUsername, String password) {
    }
}
