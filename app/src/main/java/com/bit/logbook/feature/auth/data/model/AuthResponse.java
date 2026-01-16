package com.bit.logbook.feature.auth.data.model;


import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    private boolean success;

    private String message;

    private UserDto data;

    private String timestamp;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserDto getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
