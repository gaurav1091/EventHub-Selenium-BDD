package com.eventhub.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private boolean success;
    private String token;
    private UserResponse user;

    public boolean success() {
        return success;
    }

    public String token() {
        return token;
    }

    public UserResponse user() {
        return user;
    }
}
