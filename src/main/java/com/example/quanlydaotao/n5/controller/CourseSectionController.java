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
    public List<CourseSection> getAll(@RequestParam(required = false) Boolean isActive) {
        return service.findAll(isActive);
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
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
