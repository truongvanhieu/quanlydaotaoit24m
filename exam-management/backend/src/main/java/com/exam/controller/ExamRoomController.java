package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamRoomRequestDto;
import com.exam.service.ExamRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exam-rooms")
public class ExamRoomController {

    private final ExamRoomService examRoomService;

    public ExamRoomController(ExamRoomService examRoomService) {
        this.examRoomService = examRoomService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAll(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "exam_id", required = false) UUID examId) {
        return ResponseEntity.ok(examRoomService.getAll(page, limit, search, examId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getById(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examRoomService.getById(id);
        if (!res.isSuccess()) return ResponseEntity.status(404).body(res);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody ExamRoomRequestDto request) {
        ApiResponse<Object> res = examRoomService.create(request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable("id") UUID id, @RequestBody ExamRoomRequestDto request) {
        ApiResponse<Object> res = examRoomService.update(id, request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examRoomService.softDelete(id);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }
}
