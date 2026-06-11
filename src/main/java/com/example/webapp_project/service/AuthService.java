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
 * Auth service - ProcessingUserRegister、Login、会话管理（single例）
 * Auto-init on startupAdminaccount
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
     * Auto-init/FixAdminPassword - Using INSERT ON DUPLICATE KEY UPDATE singleitemsstatement
     */
    private synchronized void initAdminUser() {
        if (initialized) return;
        initialized = true;
        String hash = PasswordUtil.hash("admin123");
        System.out.println("[Auth] hash: " + hash);

        // Only when admin create if absent, never overwrite existingPassword
        String sql = "INSERT INTO users (username,password_hash,email,full_name,role) " +
                     "VALUES ('admin',?,'admin@book.com','系统Admin','admin') " +
                     "ON DUPLICATE KEY UPDATE username=username";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("[Auth] Admin account ready");
        } catch (Exception e) {
            System.err.println("[Auth] Init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public User register(String username, String password, String email, String fullName) {
        if (userRepository.findByUsername(username) != null)
            throw new IllegalArgumentException("Username already taken");
        if (userRepository.findByEmail(email) != null)
            throw new IllegalArgumentException("Email already registered");
        String passwordHash = PasswordUtil.hash(password);
        return userRepository.save(username, passwordHash, email, fullName, "user");
    }

    public User login(String username, String password, HttpServletRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("Invalid username or password");
        if (!PasswordUtil.verify(password, user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid username or password");
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

    public void changePassword(Long userId, String oldPwd, String newPwd) {
        User user = userRepository.findById(userId);
        if (user == null) throw new IllegalArgumentException("User not found");
        if (!PasswordUtil.verify(oldPwd, user.getPasswordHash()))
            throw new IllegalArgumentException("Old password is incorrect");
        userRepository.updatePassword(userId, PasswordUtil.hash(newPwd));
    }

    public Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Long) session.getAttribute(SESSION_USER_KEY);
    }
}
