package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.service.StudentCourseSectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/student-course-sections", "/api/course-registrations"})
@CrossOrigin("*")
public class StudentCourseSectionController {
    private final StudentCourseSectionService service;

    public StudentCourseSectionController(StudentCourseSectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentCourseSection> getAll(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID courseSectionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive) {
        return service.findAll(studentId, courseSectionId, status, isActive);
    }

    @GetMapping("/details")
    public List<StudentCourseSection> getDetails(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID courseSectionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive) {
        return service.findAll(studentId, courseSectionId, status, isActive);
    }

    @GetMapping("/by-section/{sectionId}")
    public Map<String, Object> getBySection(@PathVariable UUID sectionId) {
        return service.getBySection(sectionId);
    }

    @GetMapping("/{id}")
    public StudentCourseSection getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public StudentCourseSection create(@RequestBody StudentCourseSection e, @RequestParam(required = false) UUID courseSectionId) {
        return service.save(e, courseSectionId);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createBatch(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("courseSectionId") == null) {
            throw new IllegalArgumentException("Lớp học phần không được để trống!");
        }
        UUID courseSectionId = UUID.fromString(String.valueOf(body.get("courseSectionId")));
        Object rawIds = body.get("studentIds");
        if (!(rawIds instanceof List<?> idList) || idList.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 sinh viên!");
        }
        List<UUID> studentIds = idList.stream()
                .filter(item -> item != null && !String.valueOf(item).isBlank())
                .map(item -> UUID.fromString(String.valueOf(item)))
                .toList();
        String registrationType = body.get("registrationType") != null ? String.valueOf(body.get("registrationType")) : "Học lần đầu";
        String status = body.get("status") != null ? String.valueOf(body.get("status")) : "Đang học";
        String note = body.get("note") != null ? String.valueOf(body.get("note")) : null;
        java.time.LocalDateTime regDate = null;
        if (body.get("registrationDate") != null && !String.valueOf(body.get("registrationDate")).isBlank()) {
            String rawDate = String.valueOf(body.get("registrationDate")).trim();
            if (rawDate.length() == 16) rawDate = rawDate + ":00";
            regDate = java.time.LocalDateTime.parse(rawDate);
        }
        return ResponseEntity.ok(service.saveBatch(courseSectionId, studentIds, registrationType, status, note, regDate));
    }

    @PutMapping("/{id}")
    public StudentCourseSection update(@PathVariable UUID id, @RequestBody StudentCourseSection e, @RequestParam(required = false) UUID courseSectionId) {
        return service.update(id, e, courseSectionId);
    }

    @PatchMapping("/{id}/active")
    public StudentCourseSection updateActive(@PathVariable UUID id, @RequestBody(required = false) Map<String, Boolean> body) {
        Boolean isActive = body != null ? body.get("isActive") : null;
        return service.toggleActive(id, isActive);
    }

    @PatchMapping("/{id}/status")
    public StudentCourseSection updateStatus(@PathVariable UUID id, @RequestParam(required = false) Boolean isActive) {
        return service.toggleActive(id, isActive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa mềm thành công", "isActive", false));
    }
}
