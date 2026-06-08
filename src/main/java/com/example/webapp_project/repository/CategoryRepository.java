package com.example.webapp_project.repository;

import com.example.webapp_project.model.Category;
import com.example.webapp_project.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类数据访问层 - JDBC 实现（单例）
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
        } catch (SQLException e) { throw new RuntimeException("查询分类列表失败", e); }
        return list;
    }

    public Category findById(Long id) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { throw new RuntimeException("查询分类失败", e); }
        return null;
    }

    public Category findByName(String name) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { throw new RuntimeException("查询分类失败", e); }
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
            if (e.getErrorCode() == 1062) throw new RuntimeException("分类名称已存在");
            throw new RuntimeException("新增分类失败", e);
        }
        return null;
    }

    public Category update(Long id, String name, String description) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE categories SET name=?, description=? WHERE id=?")) {
            ps.setString(1, name); ps.setString(2, description); ps.setLong(3, id);
            if (ps.executeUpdate() > 0) return findById(id);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("分类名称已存在");
            throw new RuntimeException("更新分类失败", e);
        }
        return null;
    }

    public boolean delete(Long id) {
        String check = "SELECT COUNT(*) FROM books WHERE category_id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next() && rs.getLong(1) > 0)
                throw new RuntimeException("该分类下有图书，无法删除"); }
        } catch (SQLException e) {
            if (e.getMessage().contains("无法删除")) throw new RuntimeException(e.getMessage());
            throw new RuntimeException("检查分类关联失败", e);
        }
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
            ps.setLong(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException("删除分类失败", e); }
    }

    public long count() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("统计分类数失败", e); }
        return 0;
    }

    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id")); c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description")); return c;
    }
}
