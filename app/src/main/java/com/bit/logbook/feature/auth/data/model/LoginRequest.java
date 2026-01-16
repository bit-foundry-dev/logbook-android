package com.bit.logbook.feature.auth.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String usernameOrEmail;
    
    private String password;

    public LoginRequest(String emailOrUsername, String password) {
        this.usernameOrEmail = emailOrUsername;
        this.password = password;
    }
}