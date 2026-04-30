package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.EquipmentBranch;
import com.quartermaster.model.EquipmentCondition;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDao {

    private static final String SELECT_BASE = """
            SELECT e.item_id,
                   e.name,
                   e.branch,
                   e.equipment_type_id,
                   et.name AS equipment_type_name,
                   e.weapon_type_id,
                   wt.name AS weapon_type_name,
                   e.caliber_id,
                   ca.name AS caliber_name,
                   e.size_id,
                   sz.name AS size_name,
                   e.storage_location_id,
                   sl.name AS storage_location_name,
                   e.serial_number,
                   e.`condition`,
                   e.status,
                   e.is_attachment
            FROM equipment_items e
            LEFT JOIN equipment_types   et ON et.id = e.equipment_type_id
            LEFT JOIN weapon_types      wt ON wt.id = e.weapon_type_id
            LEFT JOIN calibers          ca ON ca.id = e.caliber_id
            LEFT JOIN uniform_sizes     sz ON sz.id = e.size_id
            LEFT JOIN storage_locations sl ON sl.id = e.storage_location_id
            """;

    public List<EquipmentItem> findAll() {
        return runSelect(SELECT_BASE + " ORDER BY e.name");
    }

    public List<EquipmentItem> findByBranch(EquipmentBranch branch) {
        String sql = SELECT_BASE + " WHERE e.branch = ? ORDER BY e.name";
        List<EquipmentItem> items = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, branch.name());
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load equipment", ex);
        }
    }

    public List<EquipmentItem> findAvailableByBranch(EquipmentBranch branch) {
        String sql = SELECT_BASE + " WHERE e.branch = ? AND e.status = 'AVAILABLE' AND e.is_attachment = 0 ORDER BY e.name";
        List<EquipmentItem> items = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, branch.name());
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load available equipment", ex);
        }
    }

    public List<EquipmentItem> findAvailableAttachments() {
        String sql = SELECT_BASE + " WHERE e.is_attachment = 1 AND e.status = 'AVAILABLE' ORDER BY e.name";
        return runSelect(sql);
    }

    public void insert(EquipmentItem item) {
        String sql = """
                INSERT INTO equipment_items
                    (name, branch, equipment_type_id, weapon_type_id, caliber_id,
                     size_id, storage_location_id, serial_number, `condition`, status, is_attachment)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindMutable(preparedStatement, item);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add equipment", ex);
        }
    }

    public void update(EquipmentItem item) {
        String sql = """
                UPDATE equipment_items
                SET name = ?, branch = ?, equipment_type_id = ?, weapon_type_id = ?, caliber_id = ?,
                    size_id = ?, storage_location_id = ?, serial_number = ?, `condition` = ?, status = ?, is_attachment = ?
                WHERE item_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindMutable(preparedStatement, item);
            preparedStatement.setInt(12, item.getItemId());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update equipment", ex);
        }
    }

    public void delete(int itemId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM equipment_items WHERE item_id = ?")) {
            preparedStatement.setInt(1, itemId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete equipment", ex);
        }
    }

    public List<EquipmentItem> getAttachmentsForWeapon(int weaponItemId) {
        String sql = SELECT_BASE + """
                JOIN weapon_attachments wa ON wa.attachment_item_id = e.item_id
                WHERE wa.weapon_item_id = ?
                ORDER BY e.name
                """;
        List<EquipmentItem> items = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, weaponItemId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load weapon attachments", ex);
        }
    }

    public void attachToWeapon(int weaponItemId, int attachmentItemId) {
        String sql = "INSERT INTO weapon_attachments(weapon_item_id, attachment_item_id) VALUES(?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, weaponItemId);
            preparedStatement.setInt(2, attachmentItemId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to attach item to weapon", ex);
        }
    }

    public void detachFromWeapon(int weaponItemId, int attachmentItemId) {
        String sql = "DELETE FROM weapon_attachments WHERE weapon_item_id = ? AND attachment_item_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, weaponItemId);
            preparedStatement.setInt(2, attachmentItemId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to detach item from weapon", ex);
        }
    }

    public void updateStatus(Connection connection, int itemId, EquipmentStatus status) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE equipment_items SET status = ? WHERE item_id = ?")) {
            preparedStatement.setString(1, status.name());
            preparedStatement.setInt(2, itemId);
            preparedStatement.executeUpdate();
        }
    }

    public List<Integer> findAttachmentIds(Connection connection, int weaponItemId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT attachment_item_id FROM weapon_attachments WHERE weapon_item_id = ?")) {
            preparedStatement.setInt(1, weaponItemId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    public EquipmentBranch findBranch(Connection connection, int itemId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT branch FROM equipment_items WHERE item_id = ?")) {
            preparedStatement.setInt(1, itemId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Item not found");
                }
                return EquipmentBranch.fromName(rs.getString("branch"));
            }
        }
    }

    private List<EquipmentItem> runSelect(String sql) {
        List<EquipmentItem> items = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                items.add(mapItem(rs));
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load equipment", ex);
        }
    }

    private void bindMutable(PreparedStatement ps, EquipmentItem item) throws SQLException {
        ps.setString(1, item.getName());
        ps.setString(2, item.getBranch().name());
        setNullableInt(ps, 3, item.getEquipmentTypeId());
        setNullableInt(ps, 4, item.getWeaponTypeId());
        setNullableInt(ps, 5, item.getCaliberId());
        setNullableInt(ps, 6, item.getSizeId());
        setNullableInt(ps, 7, item.getStorageLocationId());
        if (item.getSerialNumber() == null || item.getSerialNumber().isBlank()) {
            ps.setNull(8, Types.VARCHAR);
        } else {
            ps.setString(8, item.getSerialNumber());
        }
        ps.setString(9, item.getCondition().name());
        ps.setString(10, item.getStatus().name());
        ps.setInt(11, item.isAttachment() ? 1 : 0);
    }

    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private EquipmentItem mapItem(ResultSet rs) throws SQLException {
        return new EquipmentItem(
                rs.getInt("item_id"),
                rs.getString("name"),
                EquipmentBranch.fromName(rs.getString("branch")),
                (Integer) rs.getObject("equipment_type_id"),
                rs.getString("equipment_type_name"),
                (Integer) rs.getObject("weapon_type_id"),
                rs.getString("weapon_type_name"),
                (Integer) rs.getObject("caliber_id"),
                rs.getString("caliber_name"),
                (Integer) rs.getObject("size_id"),
                rs.getString("size_name"),
                (Integer) rs.getObject("storage_location_id"),
                rs.getString("storage_location_name"),
                rs.getString("serial_number"),
                EquipmentCondition.valueOf(rs.getString("condition")),
                EquipmentStatus.valueOf(rs.getString("status")),
                rs.getBoolean("is_attachment")
        );
    }
}
