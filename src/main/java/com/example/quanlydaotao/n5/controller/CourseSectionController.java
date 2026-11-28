package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.service.CourseSectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/course-sections")
@CrossOrigin("*")
public class CourseSectionController {
    private final CourseSectionService service;

    public CourseSectionController(CourseSectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseSection> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive) {
        return service.findAll(keyword, semesterId, status, isActive);
    }

    @GetMapping("/{id}")
    public CourseSection getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public CourseSection create(@RequestBody CourseSection e, @RequestParam(required = false) UUID semesterId) {
        return service.save(e, semesterId);
    }

    @PutMapping("/{id}")
    public CourseSection update(@PathVariable UUID id, @RequestBody CourseSection e, @RequestParam(required = false) UUID semesterId) {
        return service.update(id, e, semesterId);
    }

    @PostMapping("/{id}/assign-lecturer")
    public CourseSection assignLecturer(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        if (body == null || body.get("employeeId") == null || String.valueOf(body.get("employeeId")).isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn giảng viên giảng dạy!");
        }
        UUID employeeId = UUID.fromString(String.valueOf(body.get("employeeId")));
        String role = body.get("role") != null ? String.valueOf(body.get("role")) : "Giảng viên chính";
        Integer hours = body.get("hours") != null && !String.valueOf(body.get("hours")).isBlank()
                ? Integer.valueOf(String.valueOf(body.get("hours")))
                : 45;
        Boolean isActive = body.get("isActive") != null ? Boolean.valueOf(String.valueOf(body.get("isActive"))) : true;
        return service.assignLecturer(id, employeeId, role, hours, isActive);
    }

    @PatchMapping("/{id}/active")
    public CourseSection updateActive(@PathVariable UUID id, @RequestBody(required = false) Map<String, Boolean> body) {
        Boolean isActive = body != null ? body.get("isActive") : null;
        return service.toggleActive(id, isActive);
    }

    @PatchMapping("/{id}/status")
    public CourseSection updateStatus(@PathVariable UUID id, @RequestParam(required = false) Boolean isActive) {
        return service.toggleActive(id, isActive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa mềm thành công", "isActive", false));
    }
}
