package com.quartermaster.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseManager {
    private static final String CONFIG_PATH = "/com/quartermaster/config.properties";
    private static DatabaseConfig config;

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConfig databaseConfig = getConfig();
        return DriverManager.getConnection(databaseConfig.getJdbcUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
    }

    private static synchronized DatabaseConfig getConfig() {
        if (config != null) {
            return config;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseManager.class.getResourceAsStream(CONFIG_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing config file: " + CONFIG_PATH);
            }
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read DB config", ex);
        }

        String host = properties.getProperty("db.host", "100.64.0.1");
        int port = Integer.parseInt(properties.getProperty("db.port", "3306"));
        String name = properties.getProperty("db.name", "quartermaster");
        String user = properties.getProperty("db.user", "quartermaster_user");
        String password = properties.getProperty("db.password", "change_me");

        config = new DatabaseConfig(host, port, name, user, password);
        return config;
    }
}
