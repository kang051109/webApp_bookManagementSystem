package com.example.webapp_project.repository;

import com.example.webapp_project.model.User;
import org.springframework.stereotype.Repository;;
import com.example.webapp_project.util.DatabaseUtil;
import org.springframework.stereotype.Repository;;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * 用户数据访问层 - JDBC 实现（单例）
 */
@Repository
public class UserRepository {
    private static final UserRepository INSTANCE = new UserRepository();
    private UserRepository() {}
    public static UserRepository getInstance() { return INSTANCE; }
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败", e);
        }
        return null;
    }
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
            ps.setString(1, email);
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
            ps.setLong(1, id);
    public User save(String username, String passwordHash, String email, String fullName, String role) {
        String sql = "INSERT INTO users (username, password_hash, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(2, passwordHash);
            ps.setString(3, email);
            ps.setString(4, fullName);
            ps.setString(5, role != null ? role : "user");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return findById(keys.getLong(1));
            throw new RuntimeException("创建用户失败", e);
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapUser(rs));
            throw new RuntimeException("查询用户列表失败", e);
        return users;
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
            if (rs.next()) return rs.getLong(1);
            throw new RuntimeException("统计用户数失败", e);
        return 0;
    /** 更新用户密码哈希 */
    public void updatePassword(Long userId, String newHash) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
            ps.setString(1, newHash);
            ps.setLong(2, userId);
            throw new RuntimeException("更新密码失败", e);
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
        return user;
}
