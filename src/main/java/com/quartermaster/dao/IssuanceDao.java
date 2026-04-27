package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.EquipmentCategory;
import com.quartermaster.model.IssuanceAdminRow;
import com.quartermaster.model.IssuedItemRow;
import com.quartermaster.model.EquipmentStatus;

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
        String itemCategorySql = "SELECT category FROM equipment_items WHERE item_id = ?";
        String insertIssuanceSql = "INSERT INTO issuances(officer_id, item_id, issued_date, returned_date) VALUES(?, ?, ?, NULL)";

        EquipmentDao equipmentDao = new EquipmentDao();

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            String category;
            try (PreparedStatement categoryStatement = connection.prepareStatement(itemCategorySql)) {
                categoryStatement.setInt(1, itemId);
                try (ResultSet rs = categoryStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Item not found");
                    }
                    category = rs.getString("category");
                }
            }

            try (PreparedStatement issueStatement = connection.prepareStatement(insertIssuanceSql)) {
                issueStatement.setInt(1, officerId);
                issueStatement.setInt(2, itemId);
                issueStatement.setDate(3, Date.valueOf(issuedDate));
                issueStatement.executeUpdate();
            }
            equipmentDao.updateStatus(connection, itemId, EquipmentStatus.ISSUED);

            if (EquipmentCategory.WEAPON.name().equals(category)) {
                List<Integer> attachmentIds = equipmentDao.findAttachmentIds(connection, itemId);
                for (int attachmentId : attachmentIds) {
                    try (PreparedStatement issueAttachmentStatement = connection.prepareStatement(insertIssuanceSql)) {
                        issueAttachmentStatement.setInt(1, officerId);
                        issueAttachmentStatement.setInt(2, attachmentId);
                        issueAttachmentStatement.setDate(3, Date.valueOf(issuedDate));
                        issueAttachmentStatement.executeUpdate();
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
        String closeByItemSql = """
                UPDATE issuances
                SET returned_date = CURDATE()
                WHERE item_id = ? AND returned_date IS NULL
                """;
        String itemCategorySql = "SELECT category FROM equipment_items WHERE item_id = ?";

        EquipmentDao equipmentDao = new EquipmentDao();

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            int itemId;
            try (PreparedStatement issuanceStatement = connection.prepareStatement(issuanceSql)) {
                issuanceStatement.setInt(1, issuanceId);
                try (ResultSet rs = issuanceStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Issuance not found");
                    }
                    itemId = rs.getInt("item_id");
                }
            }

            try (PreparedStatement closeStatement = connection.prepareStatement(closeIssuanceSql)) {
                closeStatement.setInt(1, issuanceId);
                closeStatement.executeUpdate();
            }

            equipmentDao.updateStatus(connection, itemId, EquipmentStatus.AVAILABLE);

            String category;
            try (PreparedStatement categoryStatement = connection.prepareStatement(itemCategorySql)) {
                categoryStatement.setInt(1, itemId);
                try (ResultSet rs = categoryStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Item not found");
                    }
                    category = rs.getString("category");
                }
            }

            if (EquipmentCategory.WEAPON.name().equals(category)) {
                List<Integer> attachmentIds = equipmentDao.findAttachmentIds(connection, itemId);
                try (PreparedStatement closeAttachmentStatement = connection.prepareStatement(closeByItemSql)) {
                    for (int attachmentId : attachmentIds) {
                        closeAttachmentStatement.setInt(1, attachmentId);
                        closeAttachmentStatement.executeUpdate();
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
                       e.name AS item_name,
                       e.category,
                       e.serial_number,
                       i.issued_date,
                       w.name AS attached_to_weapon
                FROM issuances i
                JOIN equipment_items e ON e.item_id = i.item_id
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
                    rows.add(new IssuedItemRow(
                            rs.getInt("issuance_id"),
                            rs.getString("item_name"),
                            EquipmentCategory.valueOf(rs.getString("category")),
                            rs.getString("serial_number"),
                            rs.getDate("issued_date").toLocalDate(),
                            rs.getString("attached_to_weapon")
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load issued items", ex);
        }
        return rows;
    }
}
