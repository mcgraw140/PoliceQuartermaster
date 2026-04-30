package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.LookupCategory;
import com.quartermaster.model.LookupItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Single CRUD entry-point for every lookup reference table.
 * The set of allowed table names is locked down to {@link LookupCategory} so we
 * never interpolate untrusted input into SQL.
 */
public class LookupDao {

    private static final Set<String> ALLOWED_TABLES = collectAllowedTables();

    private static Set<String> collectAllowedTables() {
        Set<String> names = new LinkedHashSet<>();
        for (LookupCategory category : LookupCategory.values()) {
            names.add(category.getTableName());
        }
        return names;
    }

    public List<LookupItem> findAll(LookupCategory category) {
        return findAll(category.getTableName());
    }

    public List<LookupItem> findAll(String table) {
        guard(table);
        List<LookupItem> items = new ArrayList<>();
        String sql = "SELECT id, name FROM " + table + " ORDER BY name";
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new LookupItem(rs.getInt("id"), rs.getString("name")));
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load lookup: " + table, ex);
        }
    }

    public LookupItem add(LookupCategory category, String name) {
        guard(category.getTableName());
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalStateException("Name is required.");
        }
        String sql = "INSERT INTO " + category.getTableName() + "(name) VALUES(?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, trimmed);
            preparedStatement.executeUpdate();
            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new LookupItem(keys.getInt(1), trimmed);
                }
            }
            return new LookupItem(0, trimmed);
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) {
                throw new IllegalStateException("\"" + trimmed + "\" already exists.");
            }
            throw new IllegalStateException("Failed to add lookup item", ex);
        }
    }

    public void rename(LookupCategory category, int id, String newName) {
        guard(category.getTableName());
        String trimmed = newName == null ? "" : newName.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalStateException("Name is required.");
        }
        String sql = "UPDATE " + category.getTableName() + " SET name = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, trimmed);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) {
                throw new IllegalStateException("\"" + trimmed + "\" already exists.");
            }
            throw new IllegalStateException("Failed to rename lookup item", ex);
        }
    }

    public void delete(LookupCategory category, int id) {
        guard(category.getTableName());
        String sql = "DELETE FROM " + category.getTableName() + " WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete lookup item", ex);
        }
    }

    private static void guard(String table) {
        if (!ALLOWED_TABLES.contains(table)) {
            throw new IllegalArgumentException("Unknown lookup table: " + table);
        }
    }
}
