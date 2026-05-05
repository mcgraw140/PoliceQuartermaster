package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AmmoTypeDefinitionDao {

    public record AmmoTypeDefRecord(int id,
                                    String caliber,
                                    String typeName,
                                    String useType,
                                    Double costPerBox,
                                    Integer roundsPerBox) {
        public String displayLabel() {
            return caliber + " — " + typeName + " (" + useType + ")";
        }
    }

    public List<AmmoTypeDefRecord> findAll() {
        String sql = "SELECT id, caliber, type_name, use_type, cost_per_box, rounds_per_box "
                   + "FROM ammo_type_definitions ORDER BY caliber, type_name";
        List<AmmoTypeDefRecord> rows = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load ammo type definitions", ex);
        }
        return rows;
    }

    public AmmoTypeDefRecord findById(int id) {
        String sql = "SELECT id, caliber, type_name, use_type, cost_per_box, rounds_per_box "
                   + "FROM ammo_type_definitions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load ammo type definition", ex);
        }
    }

    public int insert(String caliber, String typeName, String useType, Double costPerBox, Integer roundsPerBox) {
        String sql = "INSERT INTO ammo_type_definitions (caliber, type_name, use_type, cost_per_box, rounds_per_box) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, caliber);
            ps.setString(2, typeName);
            ps.setString(3, useType == null || useType.isBlank() ? "Duty" : useType);
            if (costPerBox == null) ps.setNull(4, Types.DECIMAL); else ps.setDouble(4, costPerBox);
            if (roundsPerBox == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, roundsPerBox);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to insert ammo type definition", ex);
        }
    }

    public void update(int id, String caliber, String typeName, String useType, Double costPerBox, Integer roundsPerBox) {
        String sql = "UPDATE ammo_type_definitions SET caliber=?, type_name=?, use_type=?, cost_per_box=?, rounds_per_box=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caliber);
            ps.setString(2, typeName);
            ps.setString(3, useType == null || useType.isBlank() ? "Duty" : useType);
            if (costPerBox == null) ps.setNull(4, Types.DECIMAL); else ps.setDouble(4, costPerBox);
            if (roundsPerBox == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, roundsPerBox);
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update ammo type definition", ex);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM ammo_type_definitions WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete ammo type definition", ex);
        }
    }

    private static AmmoTypeDefRecord map(ResultSet rs) throws SQLException {
        Double costPerBox = rs.getObject("cost_per_box") == null ? null : rs.getDouble("cost_per_box");
        Integer roundsPerBox = rs.getObject("rounds_per_box") == null ? null : rs.getInt("rounds_per_box");
        return new AmmoTypeDefRecord(
                rs.getInt("id"),
                rs.getString("caliber"),
                rs.getString("type_name"),
                rs.getString("use_type"),
                costPerBox,
                roundsPerBox);
    }
}
