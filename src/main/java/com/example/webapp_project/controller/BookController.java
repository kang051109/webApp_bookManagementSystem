package com.example.webapp_project.controller;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.Book;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final AuthService authService;

    public BookController(BookService bookService, AuthService authService) {
        this.bookService = bookService;
        this.authService = authService;
    }

    @GetMapping
    public JsonResponse<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long categoryId) {
        return JsonResponse.success(bookService.findPage(page, size, keyword, categoryId));
    }

    @GetMapping("/{id}")
    public JsonResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> data = new HashMap<>(); data.put("book", bookService.findById(id));
        return JsonResponse.success(data);
    }

    @PostMapping
    public JsonResponse<Map<String, Object>> create(@RequestBody Book book, HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        Map<String, Object> data = new HashMap<>(); data.put("book", bookService.create(book));
        return JsonResponse.success("新增图书成功", data);
    }

    @PutMapping("/{id}")
    public JsonResponse<Map<String, Object>> update(@PathVariable Long id,
                                                     @RequestBody Book book,
                                                     HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        Map<String, Object> data = new HashMap<>(); data.put("book", bookService.update(id, book));
        return JsonResponse.success("更新图书成功", data);
    }

    @DeleteMapping("/{id}")
    public JsonResponse<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        bookService.delete(id);
        return JsonResponse.success("删除图书成功", null);
    }
}
