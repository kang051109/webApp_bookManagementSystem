package com.example.webapp_project.repository;

import com.example.webapp_project.model.Category;
import com.example.webapp_project.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoriesData access layer - JDBC Implementation (singleton)
 */
public class CategoryRepository {

    private static final CategoryRepository INSTANCE = new CategoryRepository();
    private CategoryRepository() {}
    public static CategoryRepository getInstance() { return INSTANCE; }

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM categories ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException("Failed to list categories", e); }
        return list;
    }

    public Category findById(Long id) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { throw new RuntimeException("Failed to query category", e); }
        return null;
    }

    public Category findByName(String name) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { throw new RuntimeException("Failed to query category", e); }
        return null;
    }

    public Category save(String name, String description) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO categories (name, description) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return findById(keys.getLong(1)); }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("Category name already exists");
            throw new RuntimeException("Failed to create category", e);
        }
        return null;
    }

    public Category update(Long id, String name, String description) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE categories SET name=?, description=? WHERE id=?")) {
            ps.setString(1, name); ps.setString(2, description); ps.setLong(3, id);
            if (ps.executeUpdate() > 0) return findById(id);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("Category name already exists");
            throw new RuntimeException("Failed to update category", e);
        }
        return null;
    }

    public boolean delete(Long id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Check+delete in same connection+Delete, eliminatingTOCTOU
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM books WHERE category_id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > 0)
                        throw new IllegalStateException("Cannot delete: category has books");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
                ps.setLong(1, id); return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    public long count() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("Failed to count categories", e); }
        return 0;
    }

    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id")); c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description")); return c;
    }
}
