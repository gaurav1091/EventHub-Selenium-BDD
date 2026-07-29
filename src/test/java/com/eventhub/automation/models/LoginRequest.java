package com.eventhub.automation.models;

public class LoginRequest {
    private final String email;
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
