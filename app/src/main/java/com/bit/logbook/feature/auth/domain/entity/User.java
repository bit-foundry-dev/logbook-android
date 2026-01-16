package com.bit.logbook.feature.auth.domain.entity;

public class User {
    private final String userId;
    private final String email;
    private final String username;
    private final String accessToken;
    private final String tokenType;
    private final boolean isVerified;

    public User(String userId, String email, String username, String accessToken,
                String tokenType, boolean isVerified) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.isVerified = isVerified;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getFullToken() {
        return tokenType + " " + accessToken;
    }
}