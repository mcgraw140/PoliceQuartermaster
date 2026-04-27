package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.Officer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OfficerDao {
    public List<Officer> findAll() {
        String sql = "SELECT officer_id, name, rank, badge_number FROM officers ORDER BY name";
        List<Officer> officers = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                officers.add(new Officer(
                        rs.getInt("officer_id"),
                        rs.getString("name"),
                        rs.getString("rank"),
                        rs.getString("badge_number")
                ));
            }
            return officers;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load officers", ex);
        }
    }

    public void insert(String name, String rank, String badgeNumber) {
        String sql = "INSERT INTO officers(name, rank, badge_number) VALUES(?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, rank);
            preparedStatement.setString(3, badgeNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add officer", ex);
        }
    }

    public void update(int officerId, String name, String rank, String badgeNumber) {
        String sql = "UPDATE officers SET name = ?, rank = ?, badge_number = ? WHERE officer_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, rank);
            preparedStatement.setString(3, badgeNumber);
            preparedStatement.setInt(4, officerId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update officer", ex);
        }
    }

    public void delete(int officerId) {
        String sql = "DELETE FROM officers WHERE officer_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, officerId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete officer", ex);
        }
    }
}
