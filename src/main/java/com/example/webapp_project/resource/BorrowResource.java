package com.example.webapp_project.resource;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.BorrowRecord;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.BorrowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅 REST API 接口
 */
@Path("/borrow")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BorrowResource {

    private final BorrowService borrowService = BorrowService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @POST
    public JsonResponse<Map<String, Object>> borrow(Map<String, Object> request, @Context HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("请先登录");
        Object bookIdObj = request.get("bookId");
        if (bookIdObj == null) return JsonResponse.badRequest("请指定图书");
        try {
            Long bookId = Long.valueOf(bookIdObj.toString());
            return JsonResponse.success("借阅成功", borrowService.borrowBook(userId, bookId));
        } catch (NumberFormatException e) { return JsonResponse.badRequest("无效的图书ID"); }
        catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @POST @Path("/{id}/return")
    public JsonResponse<Map<String, Object>> returnBook(@PathParam("id") Long recordId, @Context HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("请先登录");
        try {
            return JsonResponse.success("归还成功", borrowService.returnBook(recordId, userId, authService.isAdmin(req)));
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @GET @Path("/my")
    public JsonResponse<Map<String, Object>> myBorrows(@Context HttpServletRequest req) {
        Long userId = authService.getCurrentUserId(req);
        if (userId == null) return JsonResponse.unauthorized("请先登录");
        List<BorrowRecord> records = borrowService.getUserHistory(userId);
        Map<String, Object> data = new HashMap<>(); data.put("records", records);
        return JsonResponse.success(data);
    }

    @GET
    public JsonResponse<Map<String, Object>> allBorrows(@Context HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可查看全部记录");
        List<BorrowRecord> records = borrowService.getAllRecords();
        Map<String, Object> data = new HashMap<>(); data.put("records", records);
        return JsonResponse.success(data);
    }
}
