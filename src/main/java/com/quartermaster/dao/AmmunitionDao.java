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

public class AmmunitionDao {

    public record AmmoStockRecord(int ammoId,
                                  Integer typeDefId,
                                  String typeName,
                                  String caliberName,
                                  String useName,
                                  Double costPerBox,
                                  Integer roundsPerBox,
                                  int roundsOnHand,
                                  Double unitCost,
                                  Integer storageLocationId, String storageLocationName,
                                  String notes,
                                  String status) {
    }

    public List<AmmoStockRecord> findAll() {
        String sql = """
              SELECT a.ammo_id, a.type_def_id,
                       a.rounds_on_hand, a.unit_cost, a.storage_location_id, a.notes, a.status,
                       atd.type_name, atd.caliber AS caliber_name, atd.use_type AS use_name,
                       atd.cost_per_box, atd.rounds_per_box,
                       sl.name AS storage_name
                FROM ammunition_inventory a
              LEFT JOIN ammo_type_definitions atd ON atd.id = a.type_def_id
                LEFT JOIN storage_locations sl ON sl.id = a.storage_location_id
              ORDER BY atd.caliber, atd.type_name
                """;
        List<AmmoStockRecord> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load ammunition inventory", ex);
        }
        return rows;
    }

    public AmmoStockRecord findById(int ammoId) {
        String sql = """
              SELECT a.ammo_id, a.type_def_id,
                       a.rounds_on_hand, a.unit_cost, a.storage_location_id, a.notes, a.status,
                       atd.type_name, atd.caliber AS caliber_name, atd.use_type AS use_name,
                       atd.cost_per_box, atd.rounds_per_box,
                       sl.name AS storage_name
                FROM ammunition_inventory a
              LEFT JOIN ammo_type_definitions atd ON atd.id = a.type_def_id
                LEFT JOIN storage_locations sl ON sl.id = a.storage_location_id
                WHERE a.ammo_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ammoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load ammunition row", ex);
        }
    }

    public int insert(Integer typeDefId, int roundsOnHand, Double unitCost,
                      Integer storageLocationId, String notes, String status) {
        String sql = "INSERT INTO ammunition_inventory "
                + "(type_def_id, rounds_on_hand, unit_cost, storage_location_id, notes, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setIntOrNull(ps, 1, typeDefId);
            ps.setInt(2, roundsOnHand);
            if (unitCost == null) ps.setNull(3, Types.DECIMAL); else ps.setDouble(3, unitCost);
            setIntOrNull(ps, 4, storageLocationId);
            ps.setString(5, notes);
            ps.setString(6, status == null || status.isBlank() ? "ACTIVE" : status);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to insert ammunition row", ex);
        }
    }

    public void update(int ammoId, Integer typeDefId, int roundsOnHand, Double unitCost,
                       Integer storageLocationId, String notes, String status) {
        String sql = "UPDATE ammunition_inventory SET type_def_id=?, "
                + "rounds_on_hand=?, unit_cost=?, storage_location_id=?, notes=?, status=? WHERE ammo_id=?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setIntOrNull(ps, 1, typeDefId);
            ps.setInt(2, roundsOnHand);
            if (unitCost == null) ps.setNull(3, Types.DECIMAL); else ps.setDouble(3, unitCost);
            setIntOrNull(ps, 4, storageLocationId);
            ps.setString(5, notes);
            ps.setString(6, status == null || status.isBlank() ? "ACTIVE" : status);
            ps.setInt(7, ammoId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update ammunition row", ex);
        }
    }

    public void delete(int ammoId) {
        String sql = "DELETE FROM ammunition_inventory WHERE ammo_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ammoId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete ammunition row", ex);
        }
    }

    private static void setIntOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private static AmmoStockRecord map(ResultSet rs) throws SQLException {
        Double unitCost = rs.getObject("unit_cost") == null ? null : rs.getDouble("unit_cost");
        Double costPerBox = rs.getObject("cost_per_box") == null ? null : rs.getDouble("cost_per_box");
        Integer typeDefId = rs.getObject("type_def_id") == null ? null : rs.getInt("type_def_id");
        Integer roundsPerBox = rs.getObject("rounds_per_box") == null ? null : rs.getInt("rounds_per_box");
        Integer storageId = rs.getObject("storage_location_id") == null ? null : rs.getInt("storage_location_id");
        return new AmmoStockRecord(
                rs.getInt("ammo_id"),
                typeDefId,
                rs.getString("type_name"),
                rs.getString("caliber_name"),
                rs.getString("use_name"),
                costPerBox,
                roundsPerBox,
                rs.getInt("rounds_on_hand"),
                unitCost,
                storageId, rs.getString("storage_name"),
                rs.getString("notes"),
                rs.getString("status")
        );
    }
}
