package com.quartermaster.dao;

import com.quartermaster.auth.UserRole;
import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, role, officer_id FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        UserRole.valueOf(rs.getString("role")),
                        (Integer) rs.getObject("officer_id")
                );
                return Optional.of(user);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load user", ex);
        }
    }
}
