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

    private final BorrowService borrowService;
    private final AuthService authService;

    public BorrowController(BorrowService borrowService, AuthService authService) {
        this.borrowService = borrowService;
        this.authService = authService;
    }

    @PostMapping
    public JsonResponse<Map<String, Object>> borrow(@RequestBody Map<String, Object> request,
                                                     HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("请先登录");
        Object bookIdObj = request.get("bookId");
        if (bookIdObj == null) return JsonResponse.badRequest("请指定图书");
        Long bookId = Long.valueOf(bookIdObj.toString());
        return JsonResponse.success("借阅成功", borrowService.borrowBook(userId, bookId));
    }

    @PostMapping("/{id}/return")
    public JsonResponse<Map<String, Object>> returnBook(@PathVariable("id") Long recordId,
                                                         HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("请先登录");
        return JsonResponse.success("归还成功",
                borrowService.returnBook(recordId, userId, authService.isAdmin(req)));
    }

    @GetMapping("/my")
    public JsonResponse<Map<String, Object>> myBorrows(HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("请先登录");
        List<BorrowRecord> records = borrowService.getUserHistory(userId);
        Map<String, Object> data = new HashMap<>(); data.put("records", records);
        return JsonResponse.success(data);
    }

    @GetMapping
    public JsonResponse<Map<String, Object>> allBorrows(HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可查看全部记录");
        List<BorrowRecord> records = borrowService.getAllRecords();
        Map<String, Object> data = new HashMap<>(); data.put("records", records);
        return JsonResponse.success(data);
    }
}
