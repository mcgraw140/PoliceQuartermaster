package com.quartermaster.model;

import com.quartermaster.auth.UserRole;

public class User {
    private final int userId;
    private final String username;
    private final String passwordHash;
    private final UserRole role;
    private final Integer officerId;

    public User(int userId, String username, String passwordHash, UserRole role, Integer officerId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.officerId = officerId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Integer getOfficerId() {
        return officerId;
    }
}
