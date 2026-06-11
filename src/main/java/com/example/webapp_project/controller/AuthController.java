package com.example.webapp_project.controller;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.User;
import com.example.webapp_project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService = AuthService.getInstance();

    @PostMapping("/register")
    public JsonResponse<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");
        String fullName = request.get("fullName");
        username = username != null ? username.trim() : null;
        password = password != null ? password.trim() : null;
        email = email != null ? email.trim() : null;
        fullName = fullName != null ? fullName.trim() : null;
        if (username == null || username.isEmpty()) return JsonResponse.badRequest("Username is required");
        if (password == null || password.isEmpty()) return JsonResponse.badRequest("Password is required");
        if (email == null || email.isEmpty()) return JsonResponse.badRequest("Email is required");
        if (fullName == null || fullName.isEmpty()) return JsonResponse.badRequest("Full name is required");
        if (username.length() < 3 || username.length() > 50) return JsonResponse.badRequest("Username must be 3-50 characters");
        if (password.length() < 6) return JsonResponse.badRequest("Password must be at least 6 characters");
        User user = authService.register(username, password, email, fullName);
        Map<String, Object> data = new HashMap<>(); data.put("user", user);
        return JsonResponse.success("Registration successful", data);
    }

    @PostMapping("/login")
    public JsonResponse<Map<String, Object>> login(@RequestBody Map<String, String> request,
                                                    HttpServletRequest servletRequest) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || username.trim().isEmpty()) return JsonResponse.badRequest("Username is required");
        if (password == null || password.trim().isEmpty()) return JsonResponse.badRequest("Password is required");
        User user = authService.login(username.trim(), password, servletRequest);
        Map<String, Object> data = new HashMap<>(); data.put("user", user);
        return JsonResponse.success("Login successful", data);
    }

    @PostMapping("/logout")
    public JsonResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return JsonResponse.success("Logged out", null);
    }

    @GetMapping("/me")
    public JsonResponse<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        if (user == null) return JsonResponse.unauthorized("Please login first");
        Map<String, Object> data = new HashMap<>(); data.put("user", user);
        return JsonResponse.success(data);
    }

    @PostMapping("/change-password")
    public JsonResponse<Void> changePassword(@RequestBody Map<String, String> req, HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        if (user == null) return JsonResponse.unauthorized("Please login first");
        String oldPwd = req.get("oldPassword");
        String newPwd = req.get("newPassword");
        if (oldPwd == null || oldPwd.isEmpty()) return JsonResponse.badRequest("Old password is required");
        if (newPwd == null || newPwd.length() < 6) return JsonResponse.badRequest("New password must be at least 6 chars");
        try {
            authService.changePassword(user.getId(), oldPwd, newPwd);
            return JsonResponse.success("Password changed", null);
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }
}
