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
                                  Integer makeId, String makeName,
                                  Integer modelId, String modelName,
                                  Integer caliberId, String caliberName,
                                  Integer useId, String useName,
                                  int roundsOnHand,
                                  Double unitCost,
                                  Integer storageLocationId, String storageLocationName,
                                  String notes,
                                  String status) {
    }

    public List<AmmoStockRecord> findAll() {
        String sql = """
                SELECT a.ammo_id, a.make_id, a.model_id, a.caliber_id, a.use_id,
                       a.rounds_on_hand, a.unit_cost, a.storage_location_id, a.notes, a.status,
                       mk.name AS make_name, md.name AS model_name, cb.name AS caliber_name,
                       us.name AS use_name, sl.name AS storage_name
                FROM ammunition_inventory a
                LEFT JOIN ammo_makes mk      ON mk.id = a.make_id
                LEFT JOIN ammo_models md     ON md.id = a.model_id
                LEFT JOIN ammo_calibers cb   ON cb.id = a.caliber_id
                LEFT JOIN ammo_uses us       ON us.id = a.use_id
                LEFT JOIN storage_locations sl ON sl.id = a.storage_location_id
                ORDER BY mk.name, md.name, cb.name
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
                SELECT a.ammo_id, a.make_id, a.model_id, a.caliber_id, a.use_id,
                       a.rounds_on_hand, a.unit_cost, a.storage_location_id, a.notes, a.status,
                       mk.name AS make_name, md.name AS model_name, cb.name AS caliber_name,
                       us.name AS use_name, sl.name AS storage_name
                FROM ammunition_inventory a
                LEFT JOIN ammo_makes mk      ON mk.id = a.make_id
                LEFT JOIN ammo_models md     ON md.id = a.model_id
                LEFT JOIN ammo_calibers cb   ON cb.id = a.caliber_id
                LEFT JOIN ammo_uses us       ON us.id = a.use_id
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

    public int insert(Integer makeId, Integer modelId, Integer caliberId, Integer useId,
                      int roundsOnHand, Double unitCost, Integer storageLocationId, String notes) {
        String sql = "INSERT INTO ammunition_inventory "
                + "(make_id, model_id, caliber_id, use_id, rounds_on_hand, unit_cost, storage_location_id, notes, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setIntOrNull(ps, 1, makeId);
            setIntOrNull(ps, 2, modelId);
            setIntOrNull(ps, 3, caliberId);
            setIntOrNull(ps, 4, useId);
            ps.setInt(5, roundsOnHand);
            if (unitCost == null) ps.setNull(6, Types.DECIMAL); else ps.setDouble(6, unitCost);
            setIntOrNull(ps, 7, storageLocationId);
            ps.setString(8, notes);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to insert ammunition row", ex);
        }
    }

    public void update(int ammoId, Integer makeId, Integer modelId, Integer caliberId, Integer useId,
                       int roundsOnHand, Double unitCost, Integer storageLocationId, String notes) {
        String sql = "UPDATE ammunition_inventory SET make_id=?, model_id=?, caliber_id=?, use_id=?, "
                + "rounds_on_hand=?, unit_cost=?, storage_location_id=?, notes=? WHERE ammo_id=?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setIntOrNull(ps, 1, makeId);
            setIntOrNull(ps, 2, modelId);
            setIntOrNull(ps, 3, caliberId);
            setIntOrNull(ps, 4, useId);
            ps.setInt(5, roundsOnHand);
            if (unitCost == null) ps.setNull(6, Types.DECIMAL); else ps.setDouble(6, unitCost);
            setIntOrNull(ps, 7, storageLocationId);
            ps.setString(8, notes);
            ps.setInt(9, ammoId);
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
        Integer makeId = rs.getObject("make_id") == null ? null : rs.getInt("make_id");
        Integer modelId = rs.getObject("model_id") == null ? null : rs.getInt("model_id");
        Integer caliberId = rs.getObject("caliber_id") == null ? null : rs.getInt("caliber_id");
        Integer useId = rs.getObject("use_id") == null ? null : rs.getInt("use_id");
        Integer storageId = rs.getObject("storage_location_id") == null ? null : rs.getInt("storage_location_id");
        return new AmmoStockRecord(
                rs.getInt("ammo_id"),
                makeId, rs.getString("make_name"),
                modelId, rs.getString("model_name"),
                caliberId, rs.getString("caliber_name"),
                useId, rs.getString("use_name"),
                rs.getInt("rounds_on_hand"),
                unitCost,
                storageId, rs.getString("storage_name"),
                rs.getString("notes"),
                rs.getString("status")
        );
    }
}
