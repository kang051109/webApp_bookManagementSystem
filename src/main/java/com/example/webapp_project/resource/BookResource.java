package com.example.webapp_project.resource;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.Book;
import com.example.webapp_project.service.AuthService;
import com.example.webapp_project.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * 图书 REST API 接口
 */
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookService bookService = BookService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @GET
    public JsonResponse<Map<String, Object>> list(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("keyword") String keyword,
            @QueryParam("categoryId") Long categoryId) {
        return JsonResponse.success(bookService.findPage(page, size, keyword, categoryId));
    }

    @GET @Path("/{id}")
    public JsonResponse<Map<String, Object>> getById(@PathParam("id") Long id) {
        try {
            Map<String, Object> data = new HashMap<>(); data.put("book", bookService.findById(id));
            return JsonResponse.success(data);
        } catch (IllegalArgumentException e) { return JsonResponse.notFound(e.getMessage()); }
    }

    @POST
    public JsonResponse<Map<String, Object>> create(Book book, @Context HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        try {
            Map<String, Object> data = new HashMap<>(); data.put("book", bookService.create(book));
            return JsonResponse.success("新增图书成功", data);
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @PUT @Path("/{id}")
    public JsonResponse<Map<String, Object>> update(@PathParam("id") Long id, Book book,
                                                     @Context HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        try {
            Map<String, Object> data = new HashMap<>(); data.put("book", bookService.update(id, book));
            return JsonResponse.success("更新图书成功", data);
        } catch (IllegalArgumentException e) { return JsonResponse.badRequest(e.getMessage()); }
    }

    @DELETE @Path("/{id}")
    public JsonResponse<Void> delete(@PathParam("id") Long id, @Context HttpServletRequest req) {
        if (!authService.isAdmin(req)) return JsonResponse.forbidden("仅管理员可操作");
        try { bookService.delete(id); return JsonResponse.success("删除图书成功", null); }
        catch (IllegalArgumentException | IllegalStateException e) { return JsonResponse.badRequest(e.getMessage()); }
    }
}
