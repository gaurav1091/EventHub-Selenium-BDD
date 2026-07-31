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

    public void setId(String id) {
        this.id = id;
    }

    public String email() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String role() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
