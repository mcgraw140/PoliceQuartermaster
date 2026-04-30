package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDao {

    public String getAgencyName() {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = 'agency_name'";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
                String value = rs.getString("setting_value");
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
            return "Police Quartermaster";
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load settings", ex);
        }
    }

    public void saveAgencyName(String agencyName) {
        String normalized = agencyName == null ? "" : agencyName.trim();
        if (normalized.isEmpty()) {
            normalized = "Police Quartermaster";
        }

        String sql = """
                INSERT INTO app_settings(setting_key, setting_value)
                VALUES('agency_name', ?)
                ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, normalized);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save settings", ex);
        }
    }
}
