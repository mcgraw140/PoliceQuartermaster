package com.quartermaster.controller;

import com.quartermaster.Main;
import com.quartermaster.auth.AuthenticatedUser;
import com.quartermaster.auth.SessionManager;
import com.quartermaster.auth.UserRole;
import com.quartermaster.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML
    public void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            UiAlerts.error("Login Failed", "Username and password are required.");
            return;
        }

        Optional<AuthenticatedUser> authResult;
        try {
            authResult = authService.authenticate(username, password);
        } catch (IllegalStateException ex) {
            UiAlerts.error("Database Unavailable", "Cannot connect to MariaDB. Configure config.properties and try again.");
            return;
        }
        if (authResult.isEmpty()) {
            UiAlerts.error("Login Failed", "Invalid credentials.");
            return;
        }

        AuthenticatedUser user = authResult.get();
        SessionManager.login(user);

        try {
            if (user.getRole() == UserRole.ADMIN) {
                Main.switchScene("/com/quartermaster/fxml/main_admin.fxml", "Quartermaster - Admin", 1180, 740);
            } else {
                if (user.getOfficerId() == null) {
                    UiAlerts.error("Login Failed", "Officer account is not linked to an officer record.");
                    SessionManager.logout();
                    return;
                }
                Main.switchScene("/com/quartermaster/fxml/my_equipment.fxml", "Quartermaster - My Equipment", 900, 600);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to open app screen", ex);
        }
    }
}
