package com.example.webapp_project.controller;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService = DashboardService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping("/stats")
    public JsonResponse<Map<String, Object>> getStats(HttpServletRequest req) {
        if (authService.getCurrentUser(req) == null) return JsonResponse.unauthorized("请先登录");
        return JsonResponse.success(dashboardService.getStats());
    }
}
