package com.quartermaster.auth;

public final class SessionManager {
    private static AuthenticatedUser currentUser;

    private SessionManager() {
    }

    public static void login(AuthenticatedUser user) {
        currentUser = user;
    }

    public static AuthenticatedUser getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}
