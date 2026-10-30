package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.CourseClassInstructor;
import com.example.quanlydaotao.n5.service.CourseClassInstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/course-class-instructors")
@RequiredArgsConstructor
public class CourseClassInstructorController {

    private final CourseClassInstructorService service;

    @GetMapping
    public ResponseEntity<List<CourseClassInstructor>> getAll(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID courseClassId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String courseClassName,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(service.getAll(
                employeeId, courseClassId, employeeName, courseClassName, role, isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseClassInstructor> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<CourseClassInstructor> create(@RequestBody CourseClassInstructor instructor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(instructor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseClassInstructor> update(
            @PathVariable UUID id,
            @RequestBody CourseClassInstructor instructor) {
        return ResponseEntity.ok(service.update(id, instructor));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam Boolean isActive,
            @RequestParam(required = false) UUID updatedBy) {
        service.updateStatus(id, isActive, updatedBy);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID deletedBy) {
        service.softDelete(id, deletedBy);
        return ResponseEntity.noContent().build();
    }
}