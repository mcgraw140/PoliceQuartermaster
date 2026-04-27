package com.quartermaster.auth;

public class AuthenticatedUser {
    private final int userId;
    private final String username;
    private final UserRole role;
    private final Integer officerId;

    public AuthenticatedUser(int userId, String username, UserRole role, Integer officerId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.officerId = officerId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public Integer getOfficerId() {
        return officerId;
    }
}
