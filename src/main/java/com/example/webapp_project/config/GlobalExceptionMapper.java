package com.example.webapp_project.config;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局异常映射器 - 统一处理后端异常并返回标准 JSON 格式
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {

        // 处理 WebApplicationException（包含 JAX-RS 内置异常）
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            int status = webEx.getResponse().getStatus();
            String message = webEx.getMessage() != null ? webEx.getMessage() : getDefaultMessage(status);
            return Response.status(status)
                    .entity(JsonResponse.error(status, message))
                    .build();
        }

        // 处理 IllegalArgumentException（参数校验失败）
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonResponse.badRequest(exception.getMessage()))
                    .build();
        }

        // 处理其他未捕获异常
        logger.error("服务器内部错误", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(JsonResponse.serverError("服务器内部错误: " + exception.getMessage()))
                .build();
    }

    private String getDefaultMessage(int status) {
        switch (status) {
            case 400: return "请求参数错误";
            case 401: return "未授权，请先登录";
            case 403: return "权限不足";
            case 404: return "请求的资源不存在";
            case 405: return "请求方法不允许";
            case 500: return "服务器内部错误";
            default: return "操作失败";
        }
    }
}
