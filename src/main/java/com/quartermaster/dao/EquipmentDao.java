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

    public record UniformStockRecord(int uniformId,
                                     String itemName,
                                     UniformCategory category,
                                     String brand,
                                     String model,
                                     Integer sizeId,
                                     String sizeName,
                                     int quantityOnHand,
                                     Double unitCost,
                                     String supplier,
                                     Integer storageLocationId,
                                     String storageLocationName,
                                     EquipmentCondition condition,
                                     EquipmentStatus status) {
    }

    public enum UniformCategory {
        UNIFORM,
        OUTERWEAR,
        FOOTWEAR,
        GEAR;

        public static UniformCategory fromDisplayName(String value) {
            if (value == null || value.isBlank()) {
                return UNIFORM;
            }
            return switch (value.trim().toUpperCase()) {
                case "UNIFORM" -> UNIFORM;
                case "OUTERWEAR" -> OUTERWEAR;
                case "FOOTWEAR" -> FOOTWEAR;
                case "GEAR" -> GEAR;
                default -> UNIFORM;
            };
        }
    }

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
                   e.replacement_cost,
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
        if (branch == EquipmentBranch.UNIFORM) {
            return findUniformInventory();
        }
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
        if (item.getBranch() == EquipmentBranch.UNIFORM) {
            insertUniformStock(
                    item.getName(),
                    UniformCategory.UNIFORM,
                    null,
                    null,
                    item.getSizeId(),
                    parseQuantity(item.getSerialNumber(), 1),
                    item.getReplacementCost(),
                    null,
                    item.getStorageLocationId(),
                    item.getCondition(),
                    item.getStatus()
            );
            return;
        }
        String sql = """
                INSERT INTO equipment_items
                    (name, branch, equipment_type_id, weapon_type_id, caliber_id,
                     size_id, storage_location_id, serial_number, replacement_cost, `condition`, status, is_attachment)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
        if (item.getBranch() == EquipmentBranch.UNIFORM) {
            updateUniformStock(
                    item.getItemId(),
                    item.getName(),
                    UniformCategory.fromDisplayName(item.getEquipmentTypeName()),
                    item.getWeaponTypeName(),
                    item.getCaliberName(),
                    item.getSizeId(),
                    parseQuantity(item.getSerialNumber(), 0),
                    item.getReplacementCost(),
                    null,
                    item.getStorageLocationId(),
                    item.getCondition(),
                    item.getStatus()
            );
            return;
        }
        String sql = """
                UPDATE equipment_items
                SET name = ?, branch = ?, equipment_type_id = ?, weapon_type_id = ?, caliber_id = ?,
                    size_id = ?, storage_location_id = ?, serial_number = ?, replacement_cost = ?, `condition` = ?, status = ?, is_attachment = ?
                WHERE item_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindMutable(preparedStatement, item);
            preparedStatement.setInt(13, item.getItemId());
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

    public void updateStatus(int itemId, EquipmentStatus status) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE equipment_items SET status = ? WHERE item_id = ?")) {
            preparedStatement.setString(1, status.name());
            preparedStatement.setInt(2, itemId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update equipment status", ex);
        }
    }

    public boolean hasIssuanceHistory(int itemId) {
        String sql = "SELECT 1 FROM issuances WHERE item_id = ? LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, itemId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to check issuance history", ex);
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
        if (item.getReplacementCost() == null) {
            ps.setNull(9, Types.DECIMAL);
        } else {
            ps.setDouble(9, item.getReplacementCost());
        }
        ps.setString(10, item.getCondition().name());
        ps.setString(11, item.getStatus().name());
        ps.setInt(12, item.isAttachment() ? 1 : 0);
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
                nullableDouble(rs, "replacement_cost"),
                EquipmentCondition.valueOf(rs.getString("condition")),
                EquipmentStatus.valueOf(rs.getString("status")),
                rs.getBoolean("is_attachment")
        );
    }

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : value.doubleValue();
    }

    public void insertUniformStock(String itemName,
                                   UniformCategory category,
                                   String brand,
                                   String model,
                                   Integer sizeId,
                                   int quantityOnHand,
                                   Double unitCost,
                                   String supplier,
                                   Integer storageLocationId,
                                   EquipmentCondition condition,
                                   EquipmentStatus status) {
        String sql = """
                INSERT INTO uniform_inventory
                    (item_name, category, brand, model, size_id, quantity_on_hand,
                     unit_cost, supplier, storage_location_id, `condition`, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, itemName);
            preparedStatement.setString(2, (category == null ? UniformCategory.UNIFORM : category).name());
            setNullableString(preparedStatement, 3, brand);
            setNullableString(preparedStatement, 4, model);
            setNullableInt(preparedStatement, 5, sizeId);
            preparedStatement.setInt(6, Math.max(0, quantityOnHand));
            if (unitCost == null) {
                preparedStatement.setNull(7, Types.DECIMAL);
            } else {
                preparedStatement.setDouble(7, unitCost);
            }
            setNullableString(preparedStatement, 8, supplier);
            setNullableInt(preparedStatement, 9, storageLocationId);
            preparedStatement.setString(10, condition == null ? EquipmentCondition.GOOD.name() : condition.name());
            preparedStatement.setString(11, status == null ? EquipmentStatus.AVAILABLE.name() : status.name());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add uniform stock", ex);
        }
    }

    public void updateUniformStock(int uniformId,
                                   String itemName,
                                   UniformCategory category,
                                   String brand,
                                   String model,
                                   Integer sizeId,
                                   int quantityOnHand,
                                   Double unitCost,
                                   String supplier,
                                   Integer storageLocationId,
                                   EquipmentCondition condition,
                                   EquipmentStatus status) {
        String sql = """
                UPDATE uniform_inventory
                SET item_name = ?,
                    category = ?,
                    brand = ?,
                    model = ?,
                    size_id = ?,
                    quantity_on_hand = ?,
                    unit_cost = ?,
                    supplier = ?,
                    storage_location_id = ?,
                    `condition` = ?,
                    status = ?
                WHERE uniform_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, itemName);
            preparedStatement.setString(2, (category == null ? UniformCategory.UNIFORM : category).name());
            setNullableString(preparedStatement, 3, brand);
            setNullableString(preparedStatement, 4, model);
            setNullableInt(preparedStatement, 5, sizeId);
            preparedStatement.setInt(6, Math.max(0, quantityOnHand));
            if (unitCost == null) {
                preparedStatement.setNull(7, Types.DECIMAL);
            } else {
                preparedStatement.setDouble(7, unitCost);
            }
            setNullableString(preparedStatement, 8, supplier);
            setNullableInt(preparedStatement, 9, storageLocationId);
            preparedStatement.setString(10, condition == null ? EquipmentCondition.GOOD.name() : condition.name());
            preparedStatement.setString(11, status == null ? EquipmentStatus.AVAILABLE.name() : status.name());
            preparedStatement.setInt(12, uniformId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update uniform stock", ex);
        }
    }

    public void deleteUniformStock(int uniformId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM uniform_inventory WHERE uniform_id = ?")) {
            preparedStatement.setInt(1, uniformId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete uniform stock", ex);
        }
    }

    public boolean adjustUniformQuantity(int uniformId, int delta) {
        String sql = "UPDATE uniform_inventory SET quantity_on_hand = quantity_on_hand + ? "
                + "WHERE uniform_id = ? AND quantity_on_hand + ? >= 0";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, uniformId);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to adjust uniform quantity", ex);
        }
    }

    public List<UniformStockRecord> findAllUniformStockRecords() {
        String sql = """
                SELECT ui.uniform_id,
                       ui.item_name,
                       ui.category,
                       ui.brand,
                       ui.model,
                       ui.size_id,
                       sz.name AS size_name,
                       ui.quantity_on_hand,
                       ui.unit_cost,
                       ui.supplier,
                       ui.storage_location_id,
                       sl.name AS storage_location_name,
                       ui.`condition`,
                       ui.status
                FROM uniform_inventory ui
                LEFT JOIN uniform_sizes sz ON sz.id = ui.size_id
                LEFT JOIN storage_locations sl ON sl.id = ui.storage_location_id
                ORDER BY ui.item_name, sz.name
                """;
        List<UniformStockRecord> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new UniformStockRecord(
                        rs.getInt("uniform_id"),
                        rs.getString("item_name"),
                        UniformCategory.fromDisplayName(rs.getString("category")),
                        rs.getString("brand"),
                        rs.getString("model"),
                        (Integer) rs.getObject("size_id"),
                        rs.getString("size_name"),
                        rs.getInt("quantity_on_hand"),
                        nullableDouble(rs, "unit_cost"),
                        rs.getString("supplier"),
                        (Integer) rs.getObject("storage_location_id"),
                        rs.getString("storage_location_name"),
                        EquipmentCondition.valueOf(rs.getString("condition")),
                        EquipmentStatus.valueOf(rs.getString("status"))
                ));
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load uniform stock records", ex);
        }
    }

    public UniformStockRecord findUniformStockById(int uniformId) {
        String sql = """
                SELECT ui.uniform_id,
                       ui.item_name,
                       ui.category,
                       ui.brand,
                       ui.model,
                       ui.size_id,
                       sz.name AS size_name,
                       ui.quantity_on_hand,
                       ui.unit_cost,
                       ui.supplier,
                       ui.storage_location_id,
                       sl.name AS storage_location_name,
                       ui.`condition`,
                       ui.status
                FROM uniform_inventory ui
                LEFT JOIN uniform_sizes sz ON sz.id = ui.size_id
                LEFT JOIN storage_locations sl ON sl.id = ui.storage_location_id
                WHERE ui.uniform_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, uniformId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UniformStockRecord(
                        rs.getInt("uniform_id"),
                        rs.getString("item_name"),
                        UniformCategory.fromDisplayName(rs.getString("category")),
                        rs.getString("brand"),
                        rs.getString("model"),
                        (Integer) rs.getObject("size_id"),
                        rs.getString("size_name"),
                        rs.getInt("quantity_on_hand"),
                        nullableDouble(rs, "unit_cost"),
                        rs.getString("supplier"),
                        (Integer) rs.getObject("storage_location_id"),
                        rs.getString("storage_location_name"),
                        EquipmentCondition.valueOf(rs.getString("condition")),
                        EquipmentStatus.valueOf(rs.getString("status"))
                );
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load uniform stock row", ex);
        }
    }

    private List<EquipmentItem> findUniformInventory() {
        String sql = """
                SELECT ui.uniform_id,
                       ui.item_name,
                       ui.category,
                       ui.brand,
                       ui.model,
                       ui.size_id,
                       sz.name AS size_name,
                       ui.quantity_on_hand,
                       ui.unit_cost,
                       ui.supplier,
                       ui.storage_location_id,
                       sl.name AS storage_location_name,
                       ui.`condition`,
                       ui.status
                FROM uniform_inventory ui
                LEFT JOIN uniform_sizes sz ON sz.id = ui.size_id
                LEFT JOIN storage_locations sl ON sl.id = ui.storage_location_id
                ORDER BY ui.item_name, sz.name
                """;

        List<EquipmentItem> items = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                int qty = rs.getInt("quantity_on_hand");
                String category = rs.getString("category");
                String brand = rs.getString("brand");
                String model = rs.getString("model");

                items.add(new EquipmentItem(
                        rs.getInt("uniform_id"),
                        rs.getString("item_name"),
                        EquipmentBranch.UNIFORM,
                        null,
                        category,
                        null,
                        brand,
                        null,
                        model,
                        (Integer) rs.getObject("size_id"),
                        rs.getString("size_name"),
                        (Integer) rs.getObject("storage_location_id"),
                        rs.getString("storage_location_name"),
                        String.valueOf(qty),
                        nullableDouble(rs, "unit_cost"),
                        EquipmentCondition.valueOf(rs.getString("condition")),
                        EquipmentStatus.valueOf(rs.getString("status")),
                        false
                ));
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load uniform inventory", ex);
        }
    }

    private static int parseQuantity(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}
