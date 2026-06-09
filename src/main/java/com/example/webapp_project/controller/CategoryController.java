package com.example.webapp_project.controller;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.Category;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService = CategoryService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping
    public JsonResponse<Map<String, Object>> list() {
        List<Category> categories = categoryService.findAll();
        Map<String, Object> data = new HashMap<>(); data.put("categories", categories);
        return JsonResponse.success(data);
    }

    @GetMapping("/{id}")
    public JsonResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Category category = categoryService.findById(id);
        Map<String, Object> data = new HashMap<>(); data.put("category", category);
        return JsonResponse.success(data);
    }

    @PostMapping
    public JsonResponse<Map<String, Object>> create(@RequestBody Map<String, String> request, HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        Category category = categoryService.create(request.get("name"), request.get("description"));
        Map<String, Object> data = new HashMap<>(); data.put("category", category);
        return JsonResponse.success("新增分类成功", data);
    }

    @PutMapping("/{id}")
    public JsonResponse<Map<String, Object>> update(@PathVariable Long id,
                                                     @RequestBody Map<String, String> request, HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        Category category = categoryService.update(id, request.get("name"), request.get("description"));
        Map<String, Object> data = new HashMap<>(); data.put("category", category);
        return JsonResponse.success("更新分类成功", data);
    }

    @DeleteMapping("/{id}")
    public JsonResponse<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        categoryService.delete(id);
        return JsonResponse.success("删除分类成功", null);
    }
}
