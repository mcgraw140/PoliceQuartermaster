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

public class AmmoIssuanceDao {

    public record AmmoIssuanceRow(int id,
                                  int ammoId,
                                  String makeName,
                                  String modelName,
                                  String caliberName,
                                  int rounds,
                                  Double unitCostAtIssue,
                                  Integer reasonId,
                                  String reasonName,
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
     * Issue rounds to an officer (decrement stock + insert history row, transactionally).
     * Returns the new issuance id, or -1 when there isn't enough stock.
     */
    public int issue(int ammoId, int officerId, int rounds, Integer reasonId,
                     Double unitCostAtIssue, Integer issuedByUserId) {
        if (rounds <= 0) {
            throw new IllegalArgumentException("rounds must be > 0");
        }
        String decSql = "UPDATE ammunition_inventory SET rounds_on_hand = rounds_on_hand - ? "
                + "WHERE ammo_id = ? AND rounds_on_hand >= ?";
        String insSql = "INSERT INTO ammunition_issuances "
                + "(ammo_id, officer_id, rounds, reason_id, unit_cost_at_issue, issued_by_user_id, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'ISSUED')";
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement dec = connection.prepareStatement(decSql)) {
                dec.setInt(1, rounds);
                dec.setInt(2, ammoId);
                dec.setInt(3, rounds);
                if (dec.executeUpdate() == 0) {
                    connection.rollback();
                    return -1;
                }
            }
            try (PreparedStatement ins = connection.prepareStatement(insSql, Statement.RETURN_GENERATED_KEYS)) {
                ins.setInt(1, ammoId);
                ins.setInt(2, officerId);
                ins.setInt(3, rounds);
                if (reasonId == null) ins.setNull(4, Types.INTEGER); else ins.setInt(4, reasonId);
                if (unitCostAtIssue == null) ins.setNull(5, Types.DECIMAL); else ins.setDouble(5, unitCostAtIssue);
                if (issuedByUserId == null) ins.setNull(6, Types.INTEGER); else ins.setInt(6, issuedByUserId);
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
            throw new IllegalStateException("Failed to issue ammunition", ex);
        }
    }

    /**
     * Mark an issuance as returned and credit rounds back to stock. True on success.
     */
    public boolean returnIssuance(int issuanceId, Integer returnedByUserId) {
        String fetchSql = "SELECT ammo_id, rounds, status FROM ammunition_issuances WHERE id = ?";
        String updSql = "UPDATE ammunition_issuances SET status='RETURNED', returned_at=CURRENT_TIMESTAMP, "
                + "returned_by_user_id=? WHERE id=? AND status='ISSUED'";
        String incSql = "UPDATE ammunition_inventory SET rounds_on_hand = rounds_on_hand + ? WHERE ammo_id = ?";
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            int ammoId;
            int rounds;
            try (PreparedStatement ps = connection.prepareStatement(fetchSql)) {
                ps.setInt(1, issuanceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { connection.rollback(); return false; }
                    if (!"ISSUED".equals(rs.getString("status"))) { connection.rollback(); return false; }
                    ammoId = rs.getInt("ammo_id");
                    rounds = rs.getInt("rounds");
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(updSql)) {
                if (returnedByUserId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, returnedByUserId);
                ps.setInt(2, issuanceId);
                if (ps.executeUpdate() == 0) { connection.rollback(); return false; }
            }
            try (PreparedStatement ps = connection.prepareStatement(incSql)) {
                ps.setInt(1, rounds);
                ps.setInt(2, ammoId);
                ps.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to return ammunition issuance", ex);
        }
    }

    public List<AmmoIssuanceRow> findActiveByOfficer(int officerId) {
        return query("WHERE ai.officer_id = ? AND ai.status = 'ISSUED' ORDER BY ai.issued_at DESC",
                ps -> ps.setInt(1, officerId));
    }

    public List<AmmoIssuanceRow> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) placeholders.append(i == 0 ? "?" : ",?");
        return query("WHERE ai.id IN (" + placeholders + ") ORDER BY ai.issued_at DESC",
                ps -> {
                    for (int i = 0; i < ids.size(); i++) ps.setInt(i + 1, ids.get(i));
                });
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<AmmoIssuanceRow> query(String whereOrderClause, Binder binder) {
        String sql = """
                SELECT ai.id, ai.ammo_id, ai.rounds, ai.reason_id, ai.unit_cost_at_issue,
                       ai.issued_at, ai.returned_at, ai.status,
                       u_iss.username  AS issued_by_name,
                       u_ret.username  AS returned_by_name,
                       mk.name AS make_name, md.name AS model_name, cb.name AS caliber_name,
                       rs.name AS reason_name,
                       o.officer_id, o.name AS officer_name, o.badge_number
                FROM ammunition_issuances ai
                JOIN ammunition_inventory inv ON inv.ammo_id = ai.ammo_id
                LEFT JOIN ammo_makes    mk ON mk.id = inv.make_id
                LEFT JOIN ammo_models   md ON md.id = inv.model_id
                LEFT JOIN ammo_calibers cb ON cb.id = inv.caliber_id
                LEFT JOIN ammo_reasons  rs ON rs.id = ai.reason_id
                JOIN officers o ON o.officer_id = ai.officer_id
                LEFT JOIN users u_iss ON u_iss.user_id = ai.issued_by_user_id
                LEFT JOIN users u_ret ON u_ret.user_id = ai.returned_by_user_id
                """ + " " + whereOrderClause;

        List<AmmoIssuanceRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp issuedTs = rs.getTimestamp("issued_at");
                    Timestamp returnedTs = rs.getTimestamp("returned_at");
                    Double unitCost = rs.getObject("unit_cost_at_issue") == null ? null : rs.getDouble("unit_cost_at_issue");
                    Integer reasonId = rs.getObject("reason_id") == null ? null : rs.getInt("reason_id");
                    rows.add(new AmmoIssuanceRow(
                            rs.getInt("id"),
                            rs.getInt("ammo_id"),
                            rs.getString("make_name"),
                            rs.getString("model_name"),
                            rs.getString("caliber_name"),
                            rs.getInt("rounds"),
                            unitCost,
                            reasonId,
                            rs.getString("reason_name"),
                            issuedTs == null ? null : issuedTs.toLocalDateTime(),
                            rs.getString("issued_by_name"),
                            returnedTs == null ? null : returnedTs.toLocalDateTime(),
                            rs.getString("returned_by_name"),
                            rs.getString("status"),
                            rs.getInt("officer_id"),
                            rs.getString("officer_name"),
                            rs.getString("badge_number")
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to query ammunition issuances", ex);
        }
        return rows;
    }
}
