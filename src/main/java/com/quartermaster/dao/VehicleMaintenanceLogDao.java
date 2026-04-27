package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.VehicleMaintenanceLog;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehicleMaintenanceLogDao {
    public List<VehicleMaintenanceLog> findByVehicle(int vehicleId) {
        String sql = """
                SELECT log_id, vehicle_id, log_date, mileage, description, performed_by
                FROM vehicle_maintenance_logs
                WHERE vehicle_id = ?
                ORDER BY log_date DESC, log_id DESC
                """;

        List<VehicleMaintenanceLog> logs = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, vehicleId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    logs.add(new VehicleMaintenanceLog(
                            rs.getInt("log_id"),
                            rs.getInt("vehicle_id"),
                            rs.getDate("log_date").toLocalDate(),
                            (Integer) rs.getObject("mileage"),
                            rs.getString("description"),
                            rs.getString("performed_by")
                    ));
                }
            }
            return logs;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load maintenance logs", ex);
        }
    }

    public void insert(int vehicleId, LocalDate logDate, Integer mileage, String description, String performedBy) {
        String sql = """
                INSERT INTO vehicle_maintenance_logs(vehicle_id, log_date, mileage, description, performed_by)
                VALUES(?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, vehicleId);
            preparedStatement.setDate(2, Date.valueOf(logDate));
            if (mileage == null) {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            } else {
                preparedStatement.setInt(3, mileage);
            }
            preparedStatement.setString(4, description);
            preparedStatement.setString(5, performedBy);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add maintenance log", ex);
        }
    }
}
