package com.quartermaster.dao;

import com.quartermaster.auth.UserRole;
import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    public List<User> findAll() {
        String sql = "SELECT user_id, username, password_hash, role, officer_id FROM users ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        UserRole.valueOf(rs.getString("role")),
                        (Integer) rs.getObject("officer_id")
                ));
            }
            return users;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load users", ex);
        }
    }

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

    public void createUser(String username, String passwordHash, UserRole role, Integer officerId) {
        String sql = "INSERT INTO users(username, password_hash, role, officer_id) VALUES(?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, passwordHash);
            preparedStatement.setString(3, role.name());
            if (officerId == null) {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            } else {
                preparedStatement.setInt(4, officerId);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create user", ex);
        }
    }

    public void updateUser(int userId, String username, UserRole role, Integer officerId) {
        String sql = "UPDATE users SET username = ?, role = ?, officer_id = ? WHERE user_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, role.name());
            if (officerId == null) {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            } else {
                preparedStatement.setInt(3, officerId);
            }
            preparedStatement.setInt(4, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update user", ex);
        }
    }

    public void updatePasswordHash(int userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, passwordHash);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update user password", ex);
        }
    }

    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete user", ex);
        }
    }
}
