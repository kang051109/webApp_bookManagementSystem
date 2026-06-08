package com.example.webapp_project.resource;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

/**
 * 仪表板 REST API 接口
 */
@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource {

    private final DashboardService dashboardService = DashboardService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @GET @Path("/stats")
    public JsonResponse<Map<String, Object>> getStats(@Context HttpServletRequest req) {
        if (authService.getCurrentUser(req) == null) return JsonResponse.unauthorized("请先登录");
        return JsonResponse.success(dashboardService.getStats());
    }
}
