package com.bit.logbook.feature.auth.data.model;


public class RegisterRequest {
    private String email;

    private String username;

    private String password;

    public RegisterRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
