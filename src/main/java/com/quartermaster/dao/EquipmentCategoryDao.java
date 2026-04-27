package com.quartermaster.dao;

import com.quartermaster.db.DatabaseManager;
import com.quartermaster.model.EquipmentCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentCategoryDao {
    private static final String CATEGORY_CTE = """
            WITH RECURSIVE category_tree AS (
                SELECT category_id,
                       name,
                       parent_category_id,
                       system_key,
                       CAST(name AS CHAR(500)) AS category_path,
                       system_key AS branch_key
                FROM equipment_categories
                WHERE parent_category_id IS NULL
                UNION ALL
                SELECT c.category_id,
                       c.name,
                       c.parent_category_id,
                       c.system_key,
                       CONCAT(ct.category_path, ' / ', c.name) AS category_path,
                       COALESCE(c.system_key, ct.branch_key) AS branch_key
                FROM equipment_categories c
                JOIN category_tree ct ON c.parent_category_id = ct.category_id
            )
            """;

    public List<EquipmentCategory> findAll() {
        String sql = CATEGORY_CTE + """
                SELECT category_id, name, parent_category_id, system_key, branch_key, category_path
                FROM category_tree
                ORDER BY category_path
                """;

        List<EquipmentCategory> categories = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
            return categories;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load categories", ex);
        }
    }

    public void createSubcategory(int parentCategoryId, String name) {
        String sql = "INSERT INTO equipment_categories(name, parent_category_id, system_key) VALUES(?, ?, NULL)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, parentCategoryId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create subcategory", ex);
        }
    }

    public void deleteCategory(int categoryId) {
        String guardSql = "SELECT system_key FROM equipment_categories WHERE category_id = ?";
        String childCountSql = "SELECT COUNT(*) FROM equipment_categories WHERE parent_category_id = ?";
        String itemCountSql = "SELECT COUNT(*) FROM equipment_items WHERE category_id = ?";
        String deleteSql = "DELETE FROM equipment_categories WHERE category_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            String systemKey;
            try (PreparedStatement preparedStatement = connection.prepareStatement(guardSql)) {
                preparedStatement.setInt(1, categoryId);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Category not found.");
                    }
                    systemKey = rs.getString("system_key");
                }
            }

            if (systemKey != null && !systemKey.isBlank()) {
                throw new IllegalStateException("System categories cannot be removed.");
            }

            if (count(connection, childCountSql, categoryId) > 0) {
                throw new IllegalStateException("Remove child subcategories first.");
            }

            if (count(connection, itemCountSql, categoryId) > 0) {
                throw new IllegalStateException("Move or delete equipment in this category first.");
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
                preparedStatement.setInt(1, categoryId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete category", ex);
        }
    }

    private int count(Connection connection, String sql, int categoryId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, categoryId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public EquipmentCategory mapCategory(ResultSet rs) throws SQLException {
        return new EquipmentCategory(
                rs.getInt("category_id"),
                rs.getString("name"),
                (Integer) rs.getObject("parent_category_id"),
                rs.getString("system_key"),
                rs.getString("branch_key"),
                rs.getString("category_path")
        );
    }
}