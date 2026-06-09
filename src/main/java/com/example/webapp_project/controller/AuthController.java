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
        if (username == null || username.isEmpty()) return JsonResponse.badRequest("用户名不能为空");
        if (password == null || password.isEmpty()) return JsonResponse.badRequest("密码不能为空");
        if (email == null || email.isEmpty()) return JsonResponse.badRequest("邮箱不能为空");
        if (fullName == null || fullName.isEmpty()) return JsonResponse.badRequest("姓名不能为空");
        if (username.length() < 3 || username.length() > 50) return JsonResponse.badRequest("用户名长度需在 3-50 个字符之间");
        if (password.length() < 6) return JsonResponse.badRequest("密码长度不能少于 6 位");
        User user = authService.register(username, password, email, fullName);
        Map<String, Object> data = new HashMap<>(); data.put("user", user);
        return JsonResponse.success("注册成功", data);
    }

    @PostMapping("/login")
    public JsonResponse<Map<String, Object>> login(@RequestBody Map<String, String> request,
                                                    HttpServletRequest servletRequest) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || username.trim().isEmpty()) return JsonResponse.badRequest("用户名不能为空");
        if (password == null || password.trim().isEmpty()) return JsonResponse.badRequest("密码不能为空");
        User user = authService.login(username.trim(), password, servletRequest);
        Map<String, Object> data = new HashMap<>(); data.put("user", user);
        return JsonResponse.success("登录成功", data);
    }

    @PostMapping("/logout")
    public JsonResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return JsonResponse.success("已退出登录", null);
    }

    @GetMapping("/me")
    public JsonResponse<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        if (user == null) return JsonResponse.unauthorized("未登录");
        Map<String, Object> data = new HashMap<>(); data.put("user", user);
        return JsonResponse.success(data);
    }
}
