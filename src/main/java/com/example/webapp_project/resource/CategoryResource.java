package com.example.webapp_project.resource;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.Category;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类 REST API 接口（仅管理员可增删改）
 */
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private final CategoryService categoryService = CategoryService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @GET
    public JsonResponse<Map<String, Object>> list() {
        List<Category> categories = categoryService.findAll();
        Map<String, Object> data = new HashMap<>(); data.put("categories", categories);
        return JsonResponse.success(data);
    }

    @GET @Path("/{id}")
    public JsonResponse<Map<String, Object>> getById(@PathParam("id") Long id) {
        try {
            Map<String, Object> data = new HashMap<>(); data.put("category", categoryService.findById(id));
            return JsonResponse.success(data);
        } catch (IllegalArgumentException e) { return JsonResponse.notFound(e.getMessage()); }
    }

    @POST
    public JsonResponse<Map<String, Object>> create(Map<String, String> request, @Context HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        try {
            Category category = categoryService.create(request.get("name"), request.get("description"));
            Map<String, Object> data = new HashMap<>(); data.put("category", category);
            return JsonResponse.success("新增分类成功", data);
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @PUT @Path("/{id}")
    public JsonResponse<Map<String, Object>> update(@PathParam("id") Long id, Map<String, String> request,
                                                     @Context HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        try {
            Category category = categoryService.update(id, request.get("name"), request.get("description"));
            Map<String, Object> data = new HashMap<>(); data.put("category", category);
            return JsonResponse.success("更新分类成功", data);
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @DELETE @Path("/{id}")
    public JsonResponse<Void> delete(@PathParam("id") Long id, @Context HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        try { categoryService.delete(id); return JsonResponse.success("删除分类成功", null); }
        catch (IllegalArgumentException | IllegalStateException e) { return JsonResponse.badRequest(e.getMessage()); }
    }
}
