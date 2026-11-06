package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.service.StudentCourseSectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-course-sections")
@CrossOrigin("*")
public class StudentCourseSectionController {
    private final StudentCourseSectionService service;

    public StudentCourseSectionController(StudentCourseSectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentCourseSection> getAll(@RequestParam(required = false) Boolean isActive) {
        return service.findAll(isActive);
    }

    @GetMapping("/{id}")
    public StudentCourseSection getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public StudentCourseSection create(@RequestBody StudentCourseSection e, @RequestParam(required = false) UUID courseSectionId) {
        return service.save(e, courseSectionId);
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
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
