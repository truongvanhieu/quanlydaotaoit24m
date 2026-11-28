package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamResultRequestDto;
import com.exam.service.ExamResultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exam-results")
public class ExamResultController {

    private final ExamResultService examResultService;

    public ExamResultController(ExamResultService examResultService) {
        this.examResultService = examResultService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAll(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "exam_id", required = false) UUID examId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "is_locked", required = false) Boolean isLocked,
            @RequestParam(name = "appeal_status", required = false) String appealStatus) {
        return ResponseEntity.ok(examResultService.getAll(page, limit, search, examId, status, isLocked, appealStatus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getById(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examResultService.getById(id);
        if (!res.isSuccess()) return ResponseEntity.status(404).body(res);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody ExamResultRequestDto request) {
        ApiResponse<Object> res = examResultService.create(request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable("id") UUID id, @RequestBody ExamResultRequestDto request) {
        ApiResponse<Object> res = examResultService.update(id, request);
        if (!res.isSuccess()) {
            if (res.getMessage() != null && res.getMessage().contains("is_locked")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }
            return ResponseEntity.badRequest().body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/toggle-lock")
    public ResponseEntity<ApiResponse<Object>> toggleLock(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) ExamResultRequestDto request) {
        Boolean targetLock = request != null ? request.getIsLocked() : null;
        ApiResponse<Object> res = examResultService.toggleLock(id, targetLock);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/appeal")
    public ResponseEntity<ApiResponse<Object>> updateAppeal(
            @PathVariable("id") UUID id,
            @RequestBody ExamResultRequestDto request) {
        ApiResponse<Object> res = examResultService.updateAppeal(id, request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examResultService.softDelete(id);
        if (!res.isSuccess()) {
            if (res.getMessage() != null && res.getMessage().contains("is_locked")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }
            return ResponseEntity.badRequest().body(res);
        }
        return ResponseEntity.ok(res);
    }
}
