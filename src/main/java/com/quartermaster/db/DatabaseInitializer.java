package com.quartermaster.db;

import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        String schemaSql = readResource("/com/quartermaster/sql/schema.sql");

        try (Connection connection = DatabaseManager.getConnection()) {
            runSqlScript(connection, schemaSql);
            seedDefaultAdmin(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize database", ex);
        }
    }

    private static String readResource(String path) {
        try (InputStream inputStream = DatabaseInitializer.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing SQL resource: " + path);
            }
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
            }
            return builder.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed reading SQL resource: " + path, ex);
        }
    }

    private static void runSqlScript(Connection connection, String script) throws SQLException {
        String[] statements = Arrays.stream(script.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private static void seedDefaultAdmin(Connection connection) throws SQLException {
        String adminHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        String sql = """
                INSERT INTO users (username, password_hash, role, officer_id)
                SELECT ?, ?, 'ADMIN', NULL
                WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = ?)
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "admin");
            preparedStatement.setString(2, adminHash);
            preparedStatement.setString(3, "admin");
            preparedStatement.executeUpdate();
        }
    }
}
