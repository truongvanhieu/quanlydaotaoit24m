package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamRegistrationRequestDto;
import com.exam.service.ExamRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exam-registrations")
public class ExamRegistrationController {

    private final ExamRegistrationService examRegistrationService;

    public ExamRegistrationController(ExamRegistrationService examRegistrationService) {
        this.examRegistrationService = examRegistrationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAll(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "exam_id", required = false) UUID examId,
            @RequestParam(name = "exam_room_id", required = false) UUID examRoomId,
            @RequestParam(name = "student_id", required = false) UUID studentId,
            @RequestParam(name = "group_type", required = false) String groupType,
            @RequestParam(name = "group_id", required = false) UUID groupId,
            @RequestParam(name = "student_class", required = false) String studentClass) {
        return ResponseEntity.ok(examRegistrationService.getAll(page, limit, search, examId, examRoomId, studentId, groupType, groupId, studentClass));
    }

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<Object>> groups(@RequestParam(name = "exam_id", required = false) UUID examId) {
        return ResponseEntity.ok(examRegistrationService.groups(examId));
    }

    @PostMapping("/classify-group")
    public ResponseEntity<ApiResponse<Object>> classifyGroup(@RequestBody ExamRegistrationRequestDto request) {
        ApiResponse<Object> res = examRegistrationService.classifyGroup(request);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    @GetMapping("/{id}/transfer-rooms")
    public ResponseEntity<ApiResponse<Object>> transferRooms(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examRegistrationService.transferRooms(id);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Object>> bulk(@RequestBody ExamRegistrationRequestDto request) {
        ApiResponse<Object> res = examRegistrationService.bulkCreate(request);
        return res.isSuccess() ? ResponseEntity.status(201).body(res) : ResponseEntity.badRequest().body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getById(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examRegistrationService.getById(id);
        if (!res.isSuccess()) return ResponseEntity.status(404).body(res);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody ExamRegistrationRequestDto request) {
        ApiResponse<Object> res = examRegistrationService.create(request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable("id") UUID id, @RequestBody ExamRegistrationRequestDto request) {
        ApiResponse<Object> res = examRegistrationService.update(id, request);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(@PathVariable("id") UUID id) {
        ApiResponse<Object> res = examRegistrationService.softDelete(id);
        if (!res.isSuccess()) return ResponseEntity.badRequest().body(res);
        return ResponseEntity.ok(res);
    }
}
