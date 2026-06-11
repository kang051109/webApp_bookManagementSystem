package com.example.webapp_project.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<JsonResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(409)
                .body(JsonResponse.error(409, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonResponse<Void>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400)
                .body(JsonResponse.badRequest(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResponse<Void>> handleGeneral(Exception e) {
        logger.log(Level.SEVERE, "Internal server error", e);
        return ResponseEntity.status(500)
                .body(JsonResponse.serverError("Internal server error: " + e.getMessage()));
    }
}
