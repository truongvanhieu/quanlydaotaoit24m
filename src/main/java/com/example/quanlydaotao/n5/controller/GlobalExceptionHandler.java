package com.example.quanlydaotao.n5.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getReason() != null ? ex.getReason() : ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler({NoResourceFoundException.class, NoSuchElementException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "Không tìm thấy tài nguyên yêu cầu");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Phương thức HTTP không được hỗ trợ: " + ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Dữ liệu đầu vào không hợp lệ: " + ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        String msg = ex.getMessage() != null ? ex.getMessage() : "Lỗi xử lý dữ liệu";
        response.put("message", msg);
        if (msg.toLowerCase().contains("không tìm thấy")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi hệ thống");
        return ResponseEntity.internalServerError().body(response);
    }
}
