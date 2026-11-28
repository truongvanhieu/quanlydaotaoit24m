package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.entity.ExamType;
import com.exam.service.ExamTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exam-types")
public class ExamTypeController {

    private final ExamTypeService examTypeService;

    public ExamTypeController(ExamTypeService examTypeService) {
        this.examTypeService = examTypeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAll(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(examTypeService.getAll(page, limit, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getById(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examTypeService.getById(id);
        if (!res.isSuccess()) return ResponseEntity.status(404).body(res);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody ExamType request) {
        ApiResponse<Object> res = examTypeService.create(request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable("id") UUID id, @RequestBody ExamType request) {
        ApiResponse<Object> res = examTypeService.update(id, request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examTypeService.softDelete(id);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }
}
