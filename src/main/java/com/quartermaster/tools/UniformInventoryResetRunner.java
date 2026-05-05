package com.quartermaster.tools;

import com.quartermaster.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class UniformInventoryResetRunner {
    private UniformInventoryResetRunner() {
    }

    public static void main(String[] args) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            run(connection, """
                    CREATE TABLE IF NOT EXISTS uniform_inventory (
                        uniform_id INT AUTO_INCREMENT PRIMARY KEY,
                        item_name VARCHAR(100) NOT NULL,
                        category ENUM('UNIFORM','OUTERWEAR','FOOTWEAR','GEAR') NOT NULL DEFAULT 'UNIFORM',
                        brand VARCHAR(100) NULL,
                        model VARCHAR(100) NULL,
                        size_id INT NULL,
                        quantity_on_hand INT NOT NULL DEFAULT 0,
                        unit_cost DECIMAL(10,2) NULL,
                        supplier VARCHAR(150) NULL,
                        storage_location_id INT NULL,
                        status ENUM('AVAILABLE','ISSUED','MAINTENANCE','RETIRED','DESTROYED') NOT NULL DEFAULT 'AVAILABLE',
                        `condition` ENUM('NEW','GOOD','FAIR','POOR') NOT NULL DEFAULT 'GOOD',
                        CONSTRAINT fk_uniform_size FOREIGN KEY (size_id) REFERENCES uniform_sizes(id) ON DELETE SET NULL,
                        CONSTRAINT fk_uniform_storage FOREIGN KEY (storage_location_id) REFERENCES storage_locations(id) ON DELETE SET NULL
                    )
                    """);

            int beforeLegacy = count(connection, "SELECT COUNT(*) FROM equipment_items WHERE branch = 'UNIFORM'");
            int beforeNew = count(connection, "SELECT COUNT(*) FROM uniform_inventory");

            run(connection, "DELETE FROM issuances WHERE item_id IN (SELECT item_id FROM equipment_items WHERE branch = 'UNIFORM')");
            run(connection, "DELETE FROM equipment_items WHERE branch = 'UNIFORM'");
            run(connection, "DELETE FROM uniform_inventory");

            int afterLegacy = count(connection, "SELECT COUNT(*) FROM equipment_items WHERE branch = 'UNIFORM'");
            int afterNew = count(connection, "SELECT COUNT(*) FROM uniform_inventory");

            connection.commit();

            System.out.println("LEGACY_UNIFORM_BEFORE=" + beforeLegacy);
            System.out.println("NEW_UNIFORM_BEFORE=" + beforeNew);
            System.out.println("LEGACY_UNIFORM_AFTER=" + afterLegacy);
            System.out.println("NEW_UNIFORM_AFTER=" + afterNew);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to reset uniform inventories", ex);
        }
    }

    private static void run(Connection connection, String sql) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    private static int count(Connection connection, String sql) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
