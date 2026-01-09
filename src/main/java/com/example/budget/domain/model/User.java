package com.example.budget.domain.model;

import java.util.UUID;

public class User {

    private UUID id;
    private String username;
    private String passwordHash;

    public User(UUID id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}