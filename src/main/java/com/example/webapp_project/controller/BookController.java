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

    private final BookService bookService = BookService.getInstance();
    private final AuthService authService = AuthService.getInstance();

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
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("Admin only");
        Map<String, Object> data = new HashMap<>(); data.put("book", bookService.create(book));
        return JsonResponse.success("Book created", data);
    }

    @PutMapping("/{id}")
    public JsonResponse<Map<String, Object>> update(@PathVariable Long id,
                                                     @RequestBody Book book, HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("Admin only");
        Map<String, Object> data = new HashMap<>(); data.put("book", bookService.update(id, book));
        return JsonResponse.success("Book updated", data);
    }

    @DeleteMapping("/{id}")
    public JsonResponse<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("Admin only");
        bookService.delete(id);
        return JsonResponse.success("Book deleted", null);
    }
}
