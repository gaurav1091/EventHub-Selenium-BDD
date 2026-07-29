package com.eventhub.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private String role;

    public String id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String name() {
        return name;
    }

    public String role() {
        return role;
    }
}
