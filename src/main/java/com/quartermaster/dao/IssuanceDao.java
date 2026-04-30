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
import java.util.List;

public class IssuanceDao {

    public List<IssuanceAdminRow> findAll() {
        String sql = """
                SELECT i.issuance_id,
                       o.officer_id,
                       o.name AS officer_name,
                       e.item_id,
                       e.name AS item_name,
                       i.issued_date,
                       i.returned_date
                FROM issuances i
                JOIN officers o ON o.officer_id = i.officer_id
                JOIN equipment_items e ON e.item_id = i.item_id
                ORDER BY i.issued_date DESC, i.issuance_id DESC
                """;
        List<IssuanceAdminRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                Date returnedDate = rs.getDate("returned_date");
                rows.add(new IssuanceAdminRow(
                        rs.getInt("issuance_id"),
                        rs.getInt("officer_id"),
                        rs.getString("officer_name"),
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDate("issued_date").toLocalDate(),
                        returnedDate == null ? null : returnedDate.toLocalDate()
                ));
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load issuances", ex);
        }
    }

    public void issueItem(int officerId, int itemId, LocalDate issuedDate) {
        String insertSql = "INSERT INTO issuances(officer_id, item_id, issued_date, returned_date) VALUES(?, ?, ?, NULL)";
        EquipmentDao equipmentDao = new EquipmentDao();

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            EquipmentBranch branch = equipmentDao.findBranch(connection, itemId);

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                preparedStatement.setInt(1, officerId);
                preparedStatement.setInt(2, itemId);
                preparedStatement.setDate(3, Date.valueOf(issuedDate));
                preparedStatement.executeUpdate();
            }
            equipmentDao.updateStatus(connection, itemId, EquipmentStatus.ISSUED);

            if (branch == EquipmentBranch.WEAPON) {
                List<Integer> attachmentIds = equipmentDao.findAttachmentIds(connection, itemId);
                for (int attachmentId : attachmentIds) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                        preparedStatement.setInt(1, officerId);
                        preparedStatement.setInt(2, attachmentId);
                        preparedStatement.setDate(3, Date.valueOf(issuedDate));
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

    public void returnItemAndAttachments(int issuanceId) {
        String issuanceSql = "SELECT item_id FROM issuances WHERE issuance_id = ?";
        String closeIssuanceSql = "UPDATE issuances SET returned_date = CURDATE() WHERE issuance_id = ?";
        String closeByItemSql = "UPDATE issuances SET returned_date = CURDATE() WHERE item_id = ? AND returned_date IS NULL";
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
                preparedStatement.setInt(1, issuanceId);
                preparedStatement.executeUpdate();
            }
            equipmentDao.updateStatus(connection, itemId, EquipmentStatus.AVAILABLE);

            EquipmentBranch branch = equipmentDao.findBranch(connection, itemId);
            if (branch == EquipmentBranch.WEAPON) {
                List<Integer> attachmentIds = equipmentDao.findAttachmentIds(connection, itemId);
                try (PreparedStatement preparedStatement = connection.prepareStatement(closeByItemSql)) {
                    for (int attachmentId : attachmentIds) {
                        preparedStatement.setInt(1, attachmentId);
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
                       e.branch,
                       e.is_attachment,
                       et.name AS equipment_type_name,
                       wt.name AS weapon_type_name,
                       ca.name AS caliber_name,
                       sz.name AS size_name,
                       i.issued_date,
                       w.name AS attached_to_weapon
                FROM issuances i
                JOIN equipment_items e ON e.item_id = i.item_id
                LEFT JOIN equipment_types   et ON et.id = e.equipment_type_id
                LEFT JOIN weapon_types      wt ON wt.id = e.weapon_type_id
                LEFT JOIN calibers          ca ON ca.id = e.caliber_id
                LEFT JOIN uniform_sizes     sz ON sz.id = e.size_id
                LEFT JOIN weapon_attachments wa ON wa.attachment_item_id = e.item_id
                LEFT JOIN equipment_items w ON w.item_id = wa.weapon_item_id
                WHERE i.officer_id = ? AND i.returned_date IS NULL
                ORDER BY i.issued_date DESC, e.name
                """;

        List<IssuedItemRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, officerId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    EquipmentBranch branch = EquipmentBranch.fromName(rs.getString("branch"));
                    String summary = buildSummary(branch,
                            rs.getString("equipment_type_name"),
                            rs.getString("weapon_type_name"),
                            rs.getString("caliber_name"),
                            rs.getString("size_name"),
                            rs.getBoolean("is_attachment"));
                    rows.add(new IssuedItemRow(
                            rs.getInt("issuance_id"),
                            rs.getString("item_name"),
                            branch,
                            summary,
                            rs.getString("serial_number"),
                            rs.getDate("issued_date").toLocalDate(),
                            rs.getString("attached_to_weapon")
                    ));
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load issued items", ex);
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
}
