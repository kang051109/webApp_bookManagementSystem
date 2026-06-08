package com.example.webapp_project.config;

/**
 * 统一 JSON 响应包装类
 */
public class JsonResponse<T> {

    private int code;
    private String message;
    private T data;

    public JsonResponse() {}

    public JsonResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功响应（带数据） */
    public static <T> JsonResponse<T> success(T data) {
        return new JsonResponse<>(200, "操作成功", data);
    }

    /** 成功响应（自定义消息） */
    public static <T> JsonResponse<T> success(String message, T data) {
        return new JsonResponse<>(200, message, data);
    }

    /** 失败响应 */
    public static <T> JsonResponse<T> error(int code, String message) {
        return new JsonResponse<>(code, message, null);
    }

    /** 请求错误 (400) */
    public static <T> JsonResponse<T> badRequest(String message) {
        return new JsonResponse<>(400, message, null);
    }

    /** 未授权 (401) */
    public static <T> JsonResponse<T> unauthorized(String message) {
        return new JsonResponse<>(401, message, null);
    }

    /** 禁止访问 (403) */
    public static <T> JsonResponse<T> forbidden(String message) {
        return new JsonResponse<>(403, message, null);
    }

    /** 未找到 (404) */
    public static <T> JsonResponse<T> notFound(String message) {
        return new JsonResponse<>(404, message, null);
    }

    /** 服务器错误 (500) */
    public static <T> JsonResponse<T> serverError(String message) {
        return new JsonResponse<>(500, message, null);
    }

    // Getters & Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
