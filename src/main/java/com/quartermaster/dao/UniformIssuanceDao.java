package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UniformIssuanceDao {

    public record UniformIssuanceRow(int id,
                                     int uniformId,
                                     String itemName,
                                     String category,
                                     String brand,
                                     String model,
                                     String sizeName,
                                     int quantity,
                                     Double unitCostAtIssue,
                                     LocalDateTime issuedAt,
                                     String issuedByUsername,
                                     LocalDateTime returnedAt,
                                     String returnedByUsername,
                                     String status,
                                     int officerId,
                                     String officerName,
                                     String officerBadge) {
    }

    /**
     * Issues uniform stock to an officer atomically: inserts an issuance record and
     * decrements quantity_on_hand. Returns the new issuance id, or -1 if there isn't
     * enough stock.
     */
    public int issue(int uniformId, int officerId, int quantity, Double unitCostAtIssue, Integer issuedByUserId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        String decrementSql = "UPDATE uniform_inventory SET quantity_on_hand = quantity_on_hand - ? "
                + "WHERE uniform_id = ? AND quantity_on_hand >= ?";
        String insertSql = "INSERT INTO uniform_issuances "
                + "(uniform_id, officer_id, quantity, unit_cost_at_issue, issued_by_user_id, status) "
                + "VALUES (?, ?, ?, ?, ?, 'ISSUED')";
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement dec = connection.prepareStatement(decrementSql)) {
                dec.setInt(1, quantity);
                dec.setInt(2, uniformId);
                dec.setInt(3, quantity);
                if (dec.executeUpdate() == 0) {
                    connection.rollback();
                    return -1;
                }
            }
            try (PreparedStatement ins = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ins.setInt(1, uniformId);
                ins.setInt(2, officerId);
                ins.setInt(3, quantity);
                if (unitCostAtIssue == null) {
                    ins.setNull(4, Types.DECIMAL);
                } else {
                    ins.setDouble(4, unitCostAtIssue);
                }
                if (issuedByUserId == null) {
                    ins.setNull(5, Types.INTEGER);
                } else {
                    ins.setInt(5, issuedByUserId);
                }
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    int newId = keys.next() ? keys.getInt(1) : 0;
                    connection.commit();
                    return newId;
                }
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to issue uniform", ex);
        }
    }

    /**
     * Marks an issuance as returned and increments stock back. Returns true on success.
     */
    public boolean returnIssuance(int issuanceId, Integer returnedByUserId) {
        String fetchSql = "SELECT uniform_id, quantity, status FROM uniform_issuances WHERE id = ?";
        String updateIssuanceSql = "UPDATE uniform_issuances SET status = 'RETURNED', returned_at = CURRENT_TIMESTAMP, "
                + "returned_by_user_id = ? WHERE id = ? AND status = 'ISSUED'";
        String incrementSql = "UPDATE uniform_inventory SET quantity_on_hand = quantity_on_hand + ? WHERE uniform_id = ?";
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            int uniformId;
            int qty;
            try (PreparedStatement ps = connection.prepareStatement(fetchSql)) {
                ps.setInt(1, issuanceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }
                    if (!"ISSUED".equals(rs.getString("status"))) {
                        connection.rollback();
                        return false;
                    }
                    uniformId = rs.getInt("uniform_id");
                    qty = rs.getInt("quantity");
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(updateIssuanceSql)) {
                if (returnedByUserId == null) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, returnedByUserId);
                }
                ps.setInt(2, issuanceId);
                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(incrementSql)) {
                ps.setInt(1, qty);
                ps.setInt(2, uniformId);
                ps.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to return uniform issuance", ex);
        }
    }

    public List<UniformIssuanceRow> findActiveByOfficer(int officerId) {
        return query(
                "WHERE ui.officer_id = ? AND ui.status = 'ISSUED' ORDER BY ui.issued_at DESC",
                ps -> ps.setInt(1, officerId)
        );
    }

    public List<UniformIssuanceRow> findAllByOfficer(int officerId) {
        return query(
                "WHERE ui.officer_id = ? ORDER BY ui.issued_at DESC",
                ps -> ps.setInt(1, officerId)
        );
    }

    public List<UniformIssuanceRow> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }
        return query(
                "WHERE ui.id IN (" + placeholders + ") ORDER BY ui.issued_at DESC",
                ps -> {
                    for (int i = 0; i < ids.size(); i++) {
                        ps.setInt(i + 1, ids.get(i));
                    }
                }
        );
    }

    @FunctionalInterface
    private interface PsBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<UniformIssuanceRow> query(String whereOrderClause, PsBinder binder) {
        String sql = """
                SELECT ui.id, ui.uniform_id, ui.quantity, ui.unit_cost_at_issue,
                       ui.issued_at, ui.returned_at, ui.status,
                       u_issued.username AS issued_by_name,
                       u_returned.username AS returned_by_name,
                       inv.item_name, inv.category, inv.brand, inv.model,
                       sz.name AS size_name,
                       o.officer_id, o.name AS officer_name, o.badge_number
                FROM uniform_issuances ui
                JOIN uniform_inventory inv ON inv.uniform_id = ui.uniform_id
                LEFT JOIN uniform_sizes sz ON sz.id = inv.size_id
                JOIN officers o ON o.officer_id = ui.officer_id
                LEFT JOIN users u_issued ON u_issued.user_id = ui.issued_by_user_id
                LEFT JOIN users u_returned ON u_returned.user_id = ui.returned_by_user_id
                """ + " " + whereOrderClause;

        List<UniformIssuanceRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp issuedAtTs = rs.getTimestamp("issued_at");
                    Timestamp returnedAtTs = rs.getTimestamp("returned_at");
                    Double unitCost = rs.getObject("unit_cost_at_issue") == null
                            ? null
                            : rs.getDouble("unit_cost_at_issue");
                    rows.add(new UniformIssuanceRow(
                            rs.getInt("id"),
                            rs.getInt("uniform_id"),
                            rs.getString("item_name"),
                            rs.getString("category"),
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getString("size_name"),
                            rs.getInt("quantity"),
                            unitCost,
                            issuedAtTs == null ? null : issuedAtTs.toLocalDateTime(),
                            rs.getString("issued_by_name"),
                            returnedAtTs == null ? null : returnedAtTs.toLocalDateTime(),
                            rs.getString("returned_by_name"),
                            rs.getString("status"),
                            rs.getInt("officer_id"),
                            rs.getString("officer_name"),
                            rs.getString("badge_number")
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to query uniform issuances", ex);
        }
        return rows;
    }
}
