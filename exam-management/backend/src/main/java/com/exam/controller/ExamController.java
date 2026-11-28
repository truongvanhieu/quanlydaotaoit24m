package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamRequestDto;
import com.exam.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAll(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "semester_id", required = false) UUID semesterId,
            @RequestParam(name = "exam_status", required = false) String examStatus,
            @RequestParam(name = "exam_type_id", required = false) UUID examTypeId) {
        return ResponseEntity.ok(examService.getAll(page, limit, search, semesterId, examStatus, examTypeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getById(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examService.getById(id);
        if (!res.isSuccess()) return ResponseEntity.status(404).body(res);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody ExamRequestDto request) {
        ApiResponse<Object> res = examService.create(request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable("id") UUID id, @RequestBody ExamRequestDto request) {
        ApiResponse<Object> res = examService.update(id, request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examService.softDelete(id);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> invalidAssignment(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

}
