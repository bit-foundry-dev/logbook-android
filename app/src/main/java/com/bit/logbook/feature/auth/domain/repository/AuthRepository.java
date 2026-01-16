package com.bit.logbook.feature.auth.domain.repository;

import com.bit.logbook.feature.auth.domain.entity.User;

public interface AuthRepository {
    User login(String emailOrUsername, String password) throws Exception;

    void register(String email, String username, String password) throws Exception;

    void forgotPassword(String email) throws Exception;

    void resendVerificationEmail(String email) throws Exception;

    void saveUserToken(String token);

    void saveUserId(String userId);

    String getUserToken();

    String getUserId();

    void clearUserData();
}