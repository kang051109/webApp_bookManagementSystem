package com.example.webapp_project.repository;

import com.example.webapp_project.model.Category;
import org.springframework.stereotype.Repository;;
import com.example.webapp_project.util.DatabaseUtil;
import org.springframework.stereotype.Repository;;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * 分类数据访问层 - JDBC 实现（单例）
 */
@Repository
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
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { throw new RuntimeException("查询分类失败", e); }
        return null;
    public Category findByName(String name) {
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE name=?")) {
            ps.setString(1, name);
    public Category save(String name, String description) {
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO categories (name, description) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return findById(keys.getLong(1)); }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("分类名称已存在");
            throw new RuntimeException("新增分类失败", e);
        }
    public Category update(Long id, String name, String description) {
             PreparedStatement ps = conn.prepareStatement("UPDATE categories SET name=?, description=? WHERE id=?")) {
            ps.setString(1, name); ps.setString(2, description); ps.setLong(3, id);
            if (ps.executeUpdate() > 0) return findById(id);
            throw new RuntimeException("更新分类失败", e);
    public boolean delete(Long id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 同一连接内完成检查+删除，消除TOCTOU
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM books WHERE category_id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > 0)
                        throw new IllegalStateException("该分类下有图书，无法删除");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
                ps.setLong(1, id); return ps.executeUpdate() > 0;
            throw new RuntimeException("删除分类失败", e);
    public long count() {
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { throw new RuntimeException("统计分类数失败", e); }
        return 0;
    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id")); c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description")); return c;
}
