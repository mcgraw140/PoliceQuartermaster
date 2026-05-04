package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.EquipmentBranch;
import com.quartermaster.model.EquipmentStatus;
import com.quartermaster.model.IssuanceAdminRow;
import com.quartermaster.model.IssuedItemRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssuanceDao {

    public List<IssuanceAdminRow> findAll() {
        String sql = """
                SELECT i.issuance_id,
                       o.officer_id,
                       o.name AS officer_name,
                       e.item_id,
                       e.name AS item_name,
                       e.branch,
                       e.serial_number,
                       e.replacement_cost,
                       e.`condition`,
                       e.status,
                       e.is_attachment,
                       COALESCE(et.name, ui.name) AS equipment_type_name,
                       wt.name AS weapon_type_name,
                       ca.name AS caliber_name,
                       sz.name AS size_name,
                       issued_by.username AS issued_by_username,
                       returned_by.username AS returned_by_username,
                       i.issued_date,
                       i.returned_date
                FROM issuances i
                JOIN officers o ON o.officer_id = i.officer_id
                JOIN equipment_items e ON e.item_id = i.item_id
                LEFT JOIN equipment_types et ON et.id = e.equipment_type_id
                LEFT JOIN uniform_items ui ON ui.id = e.equipment_type_id
                LEFT JOIN weapon_types wt ON wt.id = e.weapon_type_id
                LEFT JOIN calibers ca ON ca.id = e.caliber_id
                LEFT JOIN uniform_sizes sz ON sz.id = e.size_id
                LEFT JOIN users issued_by ON issued_by.user_id = i.issued_by_user_id
                LEFT JOIN users returned_by ON returned_by.user_id = i.returned_by_user_id
                ORDER BY i.issued_date DESC, i.issuance_id DESC
                """;
        List<IssuanceAdminRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                rows.add(mapIssuanceAdminRow(rs));
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load issuances", ex);
        }
    }

    public List<IssuanceAdminRow> findByItemId(int itemId) {
        String sql = """
                SELECT i.issuance_id,
                       o.officer_id,
                       o.name AS officer_name,
                       e.item_id,
                       e.name AS item_name,
                  e.branch,
                  e.serial_number,
                  e.replacement_cost,
                  e.`condition`,
                  e.status,
                  e.is_attachment,
                  COALESCE(et.name, ui.name) AS equipment_type_name,
                  wt.name AS weapon_type_name,
                  ca.name AS caliber_name,
                  sz.name AS size_name,
                       issued_by.username AS issued_by_username,
                       returned_by.username AS returned_by_username,
                       i.issued_date,
                       i.returned_date
                FROM issuances i
                JOIN officers o ON o.officer_id = i.officer_id
                JOIN equipment_items e ON e.item_id = i.item_id
              LEFT JOIN equipment_types et ON et.id = e.equipment_type_id
              LEFT JOIN uniform_items ui ON ui.id = e.equipment_type_id
              LEFT JOIN weapon_types wt ON wt.id = e.weapon_type_id
              LEFT JOIN calibers ca ON ca.id = e.caliber_id
              LEFT JOIN uniform_sizes sz ON sz.id = e.size_id
                LEFT JOIN users issued_by ON issued_by.user_id = i.issued_by_user_id
                LEFT JOIN users returned_by ON returned_by.user_id = i.returned_by_user_id
                WHERE i.item_id = ?
                ORDER BY i.issued_date DESC, i.issuance_id DESC
                """;

        List<IssuanceAdminRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, itemId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapIssuanceAdminRow(rs));
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load item issuance history", ex);
        }
    }

    public void issueItem(int officerId, int itemId, Integer issuedByUserId, LocalDate issuedDate) {
        String insertSql = "INSERT INTO issuances(officer_id, item_id, issued_by_user_id, issued_date, returned_by_user_id, returned_date) VALUES(?, ?, ?, ?, NULL, NULL)";
        EquipmentDao equipmentDao = new EquipmentDao();

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            EquipmentBranch branch = equipmentDao.findBranch(connection, itemId);

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                preparedStatement.setInt(1, officerId);
                preparedStatement.setInt(2, itemId);
                if (issuedByUserId == null) {
                    preparedStatement.setNull(3, java.sql.Types.INTEGER);
                } else {
                    preparedStatement.setInt(3, issuedByUserId);
                }
                preparedStatement.setDate(4, Date.valueOf(issuedDate));
                preparedStatement.executeUpdate();
            }
            equipmentDao.updateStatus(connection, itemId, EquipmentStatus.ISSUED);

            if (branch == EquipmentBranch.WEAPON) {
                List<Integer> attachmentIds = equipmentDao.findAttachmentIds(connection, itemId);
                for (int attachmentId : attachmentIds) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                        preparedStatement.setInt(1, officerId);
                        preparedStatement.setInt(2, attachmentId);
                        if (issuedByUserId == null) {
                            preparedStatement.setNull(3, java.sql.Types.INTEGER);
                        } else {
                            preparedStatement.setInt(3, issuedByUserId);
                        }
                        preparedStatement.setDate(4, Date.valueOf(issuedDate));
                        preparedStatement.executeUpdate();
                    }
                    equipmentDao.updateStatus(connection, attachmentId, EquipmentStatus.ISSUED);
                }
            }

            connection.commit();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to issue item", ex);
        }
    }

    public void returnItemAndAttachments(int issuanceId, Integer returnedByUserId) {
        String issuanceSql = "SELECT item_id FROM issuances WHERE issuance_id = ?";
        String closeIssuanceSql = "UPDATE issuances SET returned_by_user_id = ?, returned_date = CURDATE() WHERE issuance_id = ?";
        String closeByItemSql = "UPDATE issuances SET returned_by_user_id = ?, returned_date = CURDATE() WHERE item_id = ? AND returned_date IS NULL";
        EquipmentDao equipmentDao = new EquipmentDao();

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            int itemId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(issuanceSql)) {
                preparedStatement.setInt(1, issuanceId);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Issuance not found");
                    }
                    itemId = rs.getInt("item_id");
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(closeIssuanceSql)) {
                if (returnedByUserId == null) {
                    preparedStatement.setNull(1, java.sql.Types.INTEGER);
                } else {
                    preparedStatement.setInt(1, returnedByUserId);
                }
                preparedStatement.setInt(2, issuanceId);
                preparedStatement.executeUpdate();
            }
            equipmentDao.updateStatus(connection, itemId, EquipmentStatus.AVAILABLE);

            EquipmentBranch branch = equipmentDao.findBranch(connection, itemId);
            if (branch == EquipmentBranch.WEAPON) {
                List<Integer> attachmentIds = equipmentDao.findAttachmentIds(connection, itemId);
                try (PreparedStatement preparedStatement = connection.prepareStatement(closeByItemSql)) {
                    for (int attachmentId : attachmentIds) {
                        if (returnedByUserId == null) {
                            preparedStatement.setNull(1, java.sql.Types.INTEGER);
                        } else {
                            preparedStatement.setInt(1, returnedByUserId);
                        }
                        preparedStatement.setInt(2, attachmentId);
                        preparedStatement.executeUpdate();
                        equipmentDao.updateStatus(connection, attachmentId, EquipmentStatus.AVAILABLE);
                    }
                }
            }

            connection.commit();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to return item", ex);
        }
    }

    public List<IssuedItemRow> findActiveIssuedItemsByOfficer(int officerId) {
        String sql = """
                SELECT i.issuance_id,
                       e.item_id,
                       e.name AS item_name,
                       e.serial_number,
                       e.replacement_cost,
                       e.branch,
                       e.is_attachment,
                      COALESCE(et.name, ui.name) AS equipment_type_name,
                       wt.name AS weapon_type_name,
                       ca.name AS caliber_name,
                       sz.name AS size_name,
                       i.issued_date,
                       w.name AS attached_to_weapon
                FROM issuances i
                JOIN equipment_items e ON e.item_id = i.item_id
                LEFT JOIN equipment_types   et ON et.id = e.equipment_type_id
                  LEFT JOIN uniform_items     ui ON ui.id = e.equipment_type_id
                LEFT JOIN weapon_types      wt ON wt.id = e.weapon_type_id
                LEFT JOIN calibers          ca ON ca.id = e.caliber_id
                LEFT JOIN uniform_sizes     sz ON sz.id = e.size_id
                LEFT JOIN weapon_attachments wa ON wa.attachment_item_id = e.item_id
                LEFT JOIN equipment_items w ON w.item_id = wa.weapon_item_id
                WHERE i.officer_id = ? AND i.returned_date IS NULL
                ORDER BY i.issued_date DESC, e.name
                """;

        List<IssuedItemRow> rows = new ArrayList<>();
        Map<String, UniformIssuedGroup> uniformGroups = new HashMap<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, officerId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    EquipmentBranch branch = EquipmentBranch.fromName(rs.getString("branch"));
                    String typeName = rs.getString("equipment_type_name");
                    String sizeName = rs.getString("size_name");
                    String itemName = rs.getString("item_name");
                    Double replacementCost = nullableDouble(rs, "replacement_cost");
                    LocalDate issuedDate = rs.getDate("issued_date").toLocalDate();
                    String summary = buildSummary(branch,
                            typeName,
                            rs.getString("weapon_type_name"),
                            rs.getString("caliber_name"),
                            sizeName,
                            rs.getBoolean("is_attachment"));

                    if (branch == EquipmentBranch.UNIFORM) {
                        String normalizedType = typeName == null ? "" : typeName.trim();
                        String normalizedSize = sizeName == null ? "" : sizeName.trim();
                        String key = normalizedType + "|" + normalizedSize;
                        UniformIssuedGroup group = uniformGroups.get(key);
                        if (group == null) {
                            group = new UniformIssuedGroup(
                                    normalizedType.isEmpty() ? itemName : normalizedType,
                                    summary,
                                    replacementCost,
                                    issuedDate
                            );
                            uniformGroups.put(key, group);
                        }
                        group.issuanceIds.add(rs.getInt("issuance_id"));
                        continue;
                    }

                    rows.add(new IssuedItemRow(
                            rs.getInt("issuance_id"),
                            List.of(rs.getInt("issuance_id")),
                            1,
                            itemName,
                            branch,
                            summary,
                            rs.getString("serial_number"),
                            replacementCost,
                            issuedDate,
                            rs.getString("attached_to_weapon")
                    ));
                }
            }
            for (UniformIssuedGroup group : uniformGroups.values()) {
                int firstIssuanceId = group.issuanceIds.get(0);
                rows.add(new IssuedItemRow(
                        firstIssuanceId,
                        List.copyOf(group.issuanceIds),
                        group.issuanceIds.size(),
                        group.itemName,
                        EquipmentBranch.UNIFORM,
                        group.categorySummary,
                        null,
                        group.replacementCost,
                        group.issuedDate,
                        null
                ));
            }
            rows.sort(Comparator
                    .comparing(IssuedItemRow::getIssuedDate, Comparator.reverseOrder())
                    .thenComparing(IssuedItemRow::getItemName, String.CASE_INSENSITIVE_ORDER));
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load issued items", ex);
        }
    }

    private static final class UniformIssuedGroup {
        private final String itemName;
        private final String categorySummary;
        private final Double replacementCost;
        private final LocalDate issuedDate;
        private final List<Integer> issuanceIds = new ArrayList<>();

        private UniformIssuedGroup(String itemName, String categorySummary, Double replacementCost, LocalDate issuedDate) {
            this.itemName = itemName;
            this.categorySummary = categorySummary;
            this.replacementCost = replacementCost;
            this.issuedDate = issuedDate;
        }
    }

    private static String buildSummary(EquipmentBranch branch, String typeName, String weaponType,
                                       String caliber, String size, boolean attachment) {
        StringBuilder builder = new StringBuilder();
        switch (branch) {
            case WEAPON -> {
                if (weaponType != null) builder.append(weaponType);
                if (caliber != null) {
                    if (builder.length() > 0) builder.append(" \u00b7 ");
                    builder.append(caliber);
                }
            }
            case UNIFORM -> {
                if (typeName != null) builder.append(typeName);
                if (size != null) {
                    if (builder.length() > 0) builder.append(" \u00b7 ");
                    builder.append("Size ").append(size);
                }
            }
            case EQUIPMENT -> {
                if (typeName != null) builder.append(typeName);
                if (attachment) {
                    if (builder.length() > 0) builder.append(" \u00b7 ");
                    builder.append("Attachment");
                }
            }
        }
        if (builder.length() == 0) {
            builder.append(branch.getDisplayName());
        }
        return builder.toString();
    }

    private IssuanceAdminRow mapIssuanceAdminRow(ResultSet rs) throws SQLException {
        Date returnedDate = rs.getDate("returned_date");
        EquipmentBranch branch = EquipmentBranch.fromName(rs.getString("branch"));
        String summary = buildSummary(
                branch,
                rs.getString("equipment_type_name"),
                rs.getString("weapon_type_name"),
                rs.getString("caliber_name"),
                rs.getString("size_name"),
                rs.getBoolean("is_attachment")
        );

        return new IssuanceAdminRow(
                rs.getInt("issuance_id"),
                rs.getInt("officer_id"),
                rs.getString("officer_name"),
                rs.getInt("item_id"),
                rs.getString("item_name"),
                branch.getDisplayName(),
                summary,
                rs.getString("serial_number"),
                nullableDouble(rs, "replacement_cost"),
                rs.getString("condition"),
                rs.getString("status"),
                rs.getString("issued_by_username"),
                rs.getString("returned_by_username"),
                rs.getDate("issued_date").toLocalDate(),
                returnedDate == null ? null : returnedDate.toLocalDate()
        );
    }

    private Double nullableDouble(ResultSet rs, String column) throws SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : value.doubleValue();
    }
}
