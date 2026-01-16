package com.bit.logbook.feature.auth.data.model;


public class UserDto {
    private String userId;

    private String email;

    private String username;

    private String accessToken;

    private String tokenType;

    private boolean isVerified;

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
}
