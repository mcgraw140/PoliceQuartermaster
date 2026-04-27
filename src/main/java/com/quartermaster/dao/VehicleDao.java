package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDao {
    public List<Vehicle> findAll() {
        String sql = "SELECT vehicle_id, unit_number, make, model, year, vin, plate_number FROM vehicles ORDER BY unit_number";
        List<Vehicle> vehicles = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                vehicles.add(new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("unit_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("vin"),
                        rs.getString("plate_number")
                ));
            }
            return vehicles;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load vehicles", ex);
        }
    }

    public void insert(String unitNumber, String make, String model, int year, String vin, String plateNumber) {
        String sql = "INSERT INTO vehicles(unit_number, make, model, year, vin, plate_number) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, unitNumber);
            preparedStatement.setString(2, make);
            preparedStatement.setString(3, model);
            preparedStatement.setInt(4, year);
            preparedStatement.setString(5, vin);
            preparedStatement.setString(6, plateNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add vehicle", ex);
        }
    }

    public void update(int vehicleId, String unitNumber, String make, String model, int year, String vin, String plateNumber) {
        String sql = """
                UPDATE vehicles
                SET unit_number = ?, make = ?, model = ?, year = ?, vin = ?, plate_number = ?
                WHERE vehicle_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, unitNumber);
            preparedStatement.setString(2, make);
            preparedStatement.setString(3, model);
            preparedStatement.setInt(4, year);
            preparedStatement.setString(5, vin);
            preparedStatement.setString(6, plateNumber);
            preparedStatement.setInt(7, vehicleId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update vehicle", ex);
        }
    }

    public void delete(int vehicleId) {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, vehicleId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete vehicle", ex);
        }
    }
}
