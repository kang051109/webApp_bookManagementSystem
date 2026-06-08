package com.example.webapp_project.service;

import com.example.webapp_project.model.User;
import com.example.webapp_project.repository.UserRepository;
import com.example.webapp_project.util.DatabaseUtil;
import com.example.webapp_project.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 认证服务 - 处理用户注册、登录、会话管理（单例）
 * 启动时自动初始化管理员账号
 */
public class AuthService {

    private static final AuthService INSTANCE = new AuthService();

    public static AuthService getInstance() { return INSTANCE; }

    private final UserRepository userRepository = UserRepository.getInstance();
    private static final String SESSION_USER_KEY = "currentUser";
    private boolean initialized = false;

    private AuthService() {
        initAdminUser();
    }

    /**
     * 自动初始化/修复管理员密码 - 使用 INSERT ON DUPLICATE KEY UPDATE 单条语句
     */
    private synchronized void initAdminUser() {
        if (initialized) return;
        initialized = true;
        String hash = PasswordUtil.hash("admin123");
        System.out.println("[Auth] 哈希: " + hash);

        String sql = "INSERT INTO users (username,password_hash,email,full_name,role) " +
                     "VALUES ('admin',?,'admin@book.com','系统管理员','admin') " +
                     "ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.executeUpdate();
            System.out.println("[Auth] 管理员密码已确保正确 (admin/admin123)");
        } catch (Exception e) {
            System.err.println("[Auth] 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public User register(String username, String password, String email, String fullName) {
        if (userRepository.findByUsername(username) != null)
            throw new IllegalArgumentException("用户名已被注册");
        if (userRepository.findByEmail(email) != null)
            throw new IllegalArgumentException("邮箱已被注册");
        String passwordHash = PasswordUtil.hash(password);
        return userRepository.save(username, passwordHash, email, fullName, "user");
    }

    public User login(String username, String password, HttpServletRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("用户名或密码错误");
        if (!PasswordUtil.verify(password, user.getPasswordHash()))
            throw new IllegalArgumentException("用户名或密码错误");
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, user.getId());
        session.setMaxInactiveInterval(3600);
        return User.safeUser(user);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) return null;
        User user = userRepository.findById(userId);
        return User.safeUser(user);
    }

    public boolean isAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null && "admin".equals(user.getRole());
    }

    public Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Long) session.getAttribute(SESSION_USER_KEY);
    }
}
