package com.example.webapp_project.config;

/**
 * Unified JSON response wrapper
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

    /** Success response (with data) */
    public static <T> JsonResponse<T> success(T data) {
        return new JsonResponse<>(200, "Success", data);
    }

    /** Success response (custom msg) */
    public static <T> JsonResponse<T> success(String message, T data) {
        return new JsonResponse<>(200, message, data);
    }

    /** Error response */
    public static <T> JsonResponse<T> error(int code, String message) {
        return new JsonResponse<>(code, message, null);
    }

    /** Bad request (400) */
    public static <T> JsonResponse<T> badRequest(String message) {
        return new JsonResponse<>(400, message, null);
    }

    /** Unauthorized (401) */
    public static <T> JsonResponse<T> unauthorized(String message) {
        return new JsonResponse<>(401, message, null);
    }

    /** Forbidden (403) */
    public static <T> JsonResponse<T> forbidden(String message) {
        return new JsonResponse<>(403, message, null);
    }

    /** Not found (404) */
    public static <T> JsonResponse<T> notFound(String message) {
        return new JsonResponse<>(404, message, null);
    }

    /** Server error (500) */
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
