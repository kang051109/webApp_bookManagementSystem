package com.example.webapp_project.model;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
public class User {

    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String role;       // user / admin
    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String username, String passwordHash, String email,
                String fullName, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * 返回不包含敏感信息的安全用户对象（用于 API 响应）
     */
    public static User safeUser(User user) {
        if (user == null) return null;
        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setEmail(user.getEmail());
        safe.setFullName(user.getFullName());
        safe.setRole(user.getRole());
        safe.setCreatedAt(user.getCreatedAt());
        return safe;
    }
}
