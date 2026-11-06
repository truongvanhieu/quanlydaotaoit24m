package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.LecturerCourseClass;
import com.example.quanlydaotao.n5.service.LecturerCourseClassService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/lecturer-course-classes", "/api/course-class-instructors"})
@CrossOrigin("*")
public class LecturerCourseClassController {
    private final LecturerCourseClassService service;

    public LecturerCourseClassController(LecturerCourseClassService service) {
        this.service = service;
    }

    @GetMapping
    public List<LecturerCourseClass> getAll(@RequestParam(required = false) Boolean isActive) {
        return service.findAll(isActive);
    }

    @GetMapping("/{id}")
    public LecturerCourseClass getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public LecturerCourseClass create(@RequestBody LecturerCourseClass e, @RequestParam(required = false) UUID courseSectionId) {
        return service.save(e, courseSectionId);
    }

    @PutMapping("/{id}")
    public LecturerCourseClass update(@PathVariable UUID id, @RequestBody LecturerCourseClass e, @RequestParam(required = false) UUID courseSectionId) {
        return service.update(id, e, courseSectionId);
    }

    @PatchMapping("/{id}/active")
    public LecturerCourseClass updateActive(@PathVariable UUID id, @RequestBody(required = false) Map<String, Boolean> body) {
        Boolean isActive = body != null ? body.get("isActive") : null;
        return service.toggleActive(id, isActive);
    }

    @PatchMapping("/{id}/status")
    public LecturerCourseClass updateStatus(@PathVariable UUID id, @RequestParam(required = false) Boolean isActive) {
        return service.toggleActive(id, isActive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
