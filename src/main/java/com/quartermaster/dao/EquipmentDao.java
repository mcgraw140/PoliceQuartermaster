package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.EquipmentCategory;
import com.quartermaster.model.EquipmentCondition;
import com.quartermaster.model.EquipmentItem;
import com.quartermaster.model.EquipmentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDao {
    private static final String CATEGORY_CTE = """
            WITH RECURSIVE category_tree AS (
                SELECT category_id,
                       name,
                       parent_category_id,
                       system_key,
                       CAST(name AS CHAR(500)) AS category_path,
                       system_key AS branch_key
                FROM equipment_categories
                WHERE parent_category_id IS NULL
                UNION ALL
                SELECT c.category_id,
                       c.name,
                       c.parent_category_id,
                       c.system_key,
                       CONCAT(ct.category_path, ' / ', c.name) AS category_path,
                       COALESCE(c.system_key, ct.branch_key) AS branch_key
                FROM equipment_categories c
                JOIN category_tree ct ON c.parent_category_id = ct.category_id
            )
            """;

    public List<EquipmentItem> findAll() {
        String sql = CATEGORY_CTE + """
                SELECT e.item_id,
                       e.name,
                       e.serial_number,
                       e.`condition`,
                       e.status,
                       ct.category_id,
                       ct.name AS category_name,
                       ct.parent_category_id,
                       ct.system_key,
                       ct.branch_key,
                       ct.category_path
                FROM equipment_items e
                JOIN category_tree ct ON ct.category_id = e.category_id
                ORDER BY name
                """;
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

    public List<EquipmentItem> findAvailable() {
        String sql = CATEGORY_CTE + """
            SELECT e.item_id,
                   e.name,
                   e.serial_number,
                   e.`condition`,
                   e.status,
                   ct.category_id,
                   ct.name AS category_name,
                   ct.parent_category_id,
                   ct.system_key,
                   ct.branch_key,
                   ct.category_path
            FROM equipment_items e
            JOIN category_tree ct ON ct.category_id = e.category_id
            WHERE e.status = 'AVAILABLE'
                ORDER BY name
                """;
        List<EquipmentItem> items = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                EquipmentItem item = mapItem(rs);
                if (!item.getCategory().isWeaponAttachment()) {
                    items.add(item);
                }
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load available equipment", ex);
        }
    }

    public void insert(String name, EquipmentCategory category, String serialNumber,
                       EquipmentCondition condition, EquipmentStatus status) {
        String sql = "INSERT INTO equipment_items(name, category_id, serial_number, `condition`, status) VALUES(?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, category.getCategoryId());
            preparedStatement.setString(3, serialNumber);
            preparedStatement.setString(4, condition.name());
            preparedStatement.setString(5, status.name());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add equipment", ex);
        }
    }

    public void update(int itemId, String name, EquipmentCategory category, String serialNumber,
                       EquipmentCondition condition, EquipmentStatus status) {
        String sql = """
                UPDATE equipment_items
            SET name = ?, category_id = ?, serial_number = ?, `condition` = ?, status = ?
                WHERE item_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, category.getCategoryId());
            preparedStatement.setString(3, serialNumber);
            preparedStatement.setString(4, condition.name());
            preparedStatement.setString(5, status.name());
            preparedStatement.setInt(6, itemId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update equipment", ex);
        }
    }

    public void delete(int itemId) {
        String sql = "DELETE FROM equipment_items WHERE item_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, itemId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete equipment", ex);
        }
    }

    public List<EquipmentItem> getAttachmentsForWeapon(int weaponItemId) {
        String sql = CATEGORY_CTE + """
            SELECT e.item_id,
                   e.name,
                   e.serial_number,
                   e.`condition`,
                   e.status,
                   ct.category_id,
                   ct.name AS category_name,
                   ct.parent_category_id,
                   ct.system_key,
                   ct.branch_key,
                   ct.category_path
            FROM weapon_attachments wa
            JOIN equipment_items e ON e.item_id = wa.attachment_item_id
            JOIN category_tree ct ON ct.category_id = e.category_id
                WHERE wa.weapon_item_id = ?
                ORDER BY e.name
                """;
        List<EquipmentItem> attachments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, weaponItemId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapItem(rs));
                }
            }
            return attachments;
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
        String sql = "UPDATE equipment_items SET status = ? WHERE item_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, status.name());
            preparedStatement.setInt(2, itemId);
            preparedStatement.executeUpdate();
        }
    }

    public List<Integer> findAttachmentIds(Connection connection, int weaponItemId) throws SQLException {
        String sql = "SELECT attachment_item_id FROM weapon_attachments WHERE weapon_item_id = ?";
        List<Integer> ids = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, weaponItemId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("attachment_item_id"));
                }
            }
        }
        return ids;
    }

    private EquipmentItem mapItem(ResultSet rs) throws SQLException {
        return new EquipmentItem(
                rs.getInt("item_id"),
                rs.getString("name"),
            new EquipmentCategory(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                (Integer) rs.getObject("parent_category_id"),
                rs.getString("system_key"),
                rs.getString("branch_key"),
                rs.getString("category_path")
            ),
                rs.getString("serial_number"),
                EquipmentCondition.valueOf(rs.getString("condition")),
                EquipmentStatus.valueOf(rs.getString("status"))
        );
    }
}
