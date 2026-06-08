package com.example.webapp_project.resource;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.User;
import com.example.webapp_project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证 REST API 接口
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService = AuthService.getInstance();

    @POST @Path("/register")
    public JsonResponse<Map<String, Object>> register(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");
        String fullName = request.get("fullName");

        if (username == null || username.trim().isEmpty()) return JsonResponse.badRequest("用户名不能为空");
        if (password == null || password.trim().isEmpty()) return JsonResponse.badRequest("密码不能为空");
        if (email == null || email.trim().isEmpty()) return JsonResponse.badRequest("邮箱不能为空");
        if (fullName == null || fullName.trim().isEmpty()) return JsonResponse.badRequest("姓名不能为空");
        if (username.length() < 3 || username.length() > 50) return JsonResponse.badRequest("用户名长度需在 3-50 个字符之间");
        if (password.length() < 6) return JsonResponse.badRequest("密码长度不能少于 6 位");

        try {
            User user = authService.register(username.trim(), password, email.trim(), fullName.trim());
            Map<String, Object> data = new HashMap<>(); data.put("user", user);
            return JsonResponse.success("注册成功", data);
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @POST @Path("/login")
    public JsonResponse<Map<String, Object>> login(Map<String, String> request,
                                                    @Context HttpServletRequest servletRequest) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || username.trim().isEmpty()) return JsonResponse.badRequest("用户名不能为空");
        if (password == null || password.trim().isEmpty()) return JsonResponse.badRequest("密码不能为空");
        try {
            User user = authService.login(username.trim(), password, servletRequest);
            Map<String, Object> data = new HashMap<>(); data.put("user", user);
            return JsonResponse.success("登录成功", data);
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @POST @Path("/logout")
    public JsonResponse<Void> logout(@Context HttpServletRequest request) {
        authService.logout(request);
        return JsonResponse.success("已退出登录", null);
    }

    @GET @Path("/me")
    public JsonResponse<Map<String, Object>> getCurrentUser(@Context HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        if (user == null) return JsonResponse.unauthorized("未登录");
        Map<String, Object> data = new HashMap<>(); data.put("user", user);
        return JsonResponse.success(data);
    }
}
