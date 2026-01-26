package com.bit.logbook.feature.auth.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.bit.logbook.R;
import com.bit.logbook.core.data.ApiResponse;
import com.bit.logbook.core.data.BaseRepository;
import com.bit.logbook.core.domain.StringProvider;
import com.bit.logbook.core.utils.Constants;
import com.bit.logbook.feature.auth.data.model.ForgotPasswordRequest;
import com.bit.logbook.feature.auth.data.model.LoginRequest;
import com.bit.logbook.feature.auth.data.model.RegisterRequest;
import com.bit.logbook.feature.auth.data.model.UserDto;
import com.bit.logbook.feature.auth.data.source.remote.AuthApiService;
import com.bit.logbook.feature.auth.domain.entity.User;
import com.bit.logbook.feature.auth.domain.exception.AuthException;
import com.bit.logbook.feature.auth.domain.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Response;

public class AuthRepositoryImpl extends BaseRepository implements AuthRepository {

    private final AuthApiService apiService;
    private final SharedPreferences sharedPreferences;
    private final StringProvider strings;

    @Inject
    public AuthRepositoryImpl(AuthApiService apiService, @ApplicationContext Context context, StringProvider strings) {
        this.apiService = apiService;
        this.sharedPreferences = context.getSharedPreferences(Constants.AUTH_PREFS_NAME, Context.MODE_PRIVATE);
        this.strings = strings;
    }

    @Override
    public User login(String emailOrUsername, String password) throws Exception {
        LoginRequest request = new LoginRequest(emailOrUsername, password);
        Response<ApiResponse<UserDto>> response = apiService.login(request).execute();

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<UserDto> authResponse = response.body();
            if (authResponse.isSuccess() && authResponse.getData() != null) {
                return mapToUser(authResponse.getData());
            } else {
                throw new AuthException(
                        authResponse.getMessage() != null ? authResponse.getMessage() : strings.get(R.string.login_failed),
                        response.code()
                );
            }
        } else if (response.code() == 401) {
            throw new AuthException(strings.get(R.string.invalid_username_password), 401);
        } else if (response.code() == 403) {
            throw new AuthException(strings.get(R.string.email_not_verified), 403, emailOrUsername);
        } else if (response.code() >= 500) {
            throw new AuthException(strings.get(R.string.server_error), response.code());
        } else {
            throw new AuthException(strings.get(R.string.login_failed), response.code());
        }
    }

    @Override
    public void register(String email, String username, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(email, username, password);
        Response<Void> response = apiService.register(request).execute();

        if (!response.isSuccessful()) {
            if (response.code() == 400) {
                throw new AuthException(strings.get(R.string.invalid_inputs), 400);
            } else if (response.code() == 409) {
                throw new AuthException(strings.get(R.string.email_exists), 409);
            } else if (response.code() >= 500) {
                throw new AuthException(strings.get(R.string.server_error), response.code());
            } else {
                throw new AuthException(strings.get(R.string.registration_failed), response.code());
            }
        }
    }

    @Override
    public void forgotPassword(String email) throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        Response<ApiResponse<UserDto>> response = apiService.forgotPassword(request).execute();

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<UserDto> authResponse = response.body();
            if (!authResponse.isSuccess()) {
                throw new Exception(authResponse.getMessage() != null ?
                        authResponse.getMessage() : strings.get(R.string.reset_password_failed));
            }
        } else if (response.code() == 404) {
            throw new AuthException(strings.get(R.string.email_not_found), 404);
        } else if (response.code() >= 500) {
            throw new AuthException(strings.get(R.string.server_error), response.code());
        } else {
            throw new AuthException(strings.get(R.string.reset_password_failed), response.code());
        }
    }

    @Override
    public void resendVerificationEmail(String email) throws Exception {
        Response<ApiResponse<UserDto>> response = apiService.resendVerification(email).execute();

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<UserDto> authResponse = response.body();
            if (!authResponse.isSuccess()) {
                throw new AuthException(
                        authResponse.getMessage() != null ? authResponse.getMessage() : strings.get(R.string.resend_verification_failed),
                        response.code()
                );
            }
        } else if (response.code() == 404) {
            throw new AuthException(strings.get(R.string.email_not_found), 404);
        } else if (response.code() >= 500) {
            throw new AuthException(strings.get(R.string.server_error), response.code());
        } else {
            throw new AuthException(strings.get(R.string.resend_verification_failed), response.code());
        }
    }

    @Override
    public void saveUserToken(String token) {
        sharedPreferences.edit()
                .putString(Constants.KEY_USER_TOKEN, token)
                .apply();
    }

    @Override
    public void saveUserId(String userId) {
        sharedPreferences.edit()
                .putString(Constants.KEY_USER_ID, userId)
                .apply();
    }

    @Override
    public void saveUserEmail(String email) {
        sharedPreferences.edit()
                .putString(Constants.KEY_USER_EMAIL, email)
                .apply();
    }

    @Override
    public void saveUserUsername(String username) {
        sharedPreferences.edit()
                .putString(Constants.KEY_USER_USERNAME, username)
                .apply();
    }

    @Override
    public String getUserToken() {
        return sharedPreferences.getString(Constants.KEY_USER_TOKEN, null);
    }

    @Override
    public String getUserId() {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null);
    }

    @Override
    public String getUserEmail() {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null);
    }

    @Override
    public String getUserUsername() {
        return sharedPreferences.getString(Constants.KEY_USER_USERNAME, null);
    }

    @Override
    public void clearUserData() {
        sharedPreferences.edit()
                .remove(Constants.KEY_USER_TOKEN)
                .remove(Constants.KEY_USER_ID)
                .apply();
    }

    private User mapToUser(UserDto dto) {
        return new User(
                dto.getUserId(),
                dto.getEmail(),
                dto.getUsername(),
                dto.getAccessToken(),
                dto.getTokenType(),
                dto.isVerified()
        );
    }
}
