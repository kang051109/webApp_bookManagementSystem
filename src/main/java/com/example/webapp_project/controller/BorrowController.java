package com.example.webapp_project.controller;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.BorrowRecord;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.BorrowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final BorrowService borrowService = BorrowService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @PostMapping
    public JsonResponse<Map<String, Object>> borrow(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("Please login first");
        Object bookIdObj = request.get("bookId");
        if (bookIdObj == null) return JsonResponse.badRequest("Please specify a book");
        Long bookId = Long.valueOf(bookIdObj.toString());
        return JsonResponse.success("Borrowing successful", borrowService.borrowBook(userId, bookId));
    }

    @PostMapping("/{id}/return")
    public JsonResponse<Map<String, Object>> returnBook(@PathVariable("id") Long recordId, HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("Please login first");
        return JsonResponse.success("Return successful",
                borrowService.returnBook(recordId, userId, authService.isAdmin(req)));
    }

    @GetMapping("/my")
    public JsonResponse<Map<String, Object>> myBorrows(HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("Please login first");
        List<BorrowRecord> records = borrowService.getUserHistory(userId);
        Map<String, Object> data = new HashMap<>(); data.put("records", records);
        return JsonResponse.success(data);
    }

    @GetMapping
    public JsonResponse<Map<String, Object>> allBorrows(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int size,
            HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("Admin only");
        List<BorrowRecord> all = borrowService.getAllRecords();
        int total = all.size(), from = (page-1)*size, to = Math.min(from+size, total);
        List<BorrowRecord> pageList = from < to ? all.subList(from, to) : new java.util.ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageList); data.put("page", page); data.put("size", size);
        data.put("total", total); data.put("totalPages", Math.max(1, (total+size-1)/size));
        return JsonResponse.success(data);
    }

    @GetMapping("/overdue")
    public JsonResponse<Map<String, Object>> overdueBorrows(HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("Admin only");
        List<BorrowRecord> records = borrowService.getOverdueRecords();
        Map<String, Object> data = new HashMap<>(); data.put("records", records);
        return JsonResponse.success(data);
    }
}
