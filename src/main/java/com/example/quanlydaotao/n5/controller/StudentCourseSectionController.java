package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.dto.StudentCourseSectionDetails;
import com.example.quanlydaotao.n5.service.StudentCourseSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-course-sections")
@RequiredArgsConstructor
public class StudentCourseSectionController {

    private final StudentCourseSectionService service;

    @GetMapping("/details")
    public ResponseEntity<List<StudentCourseSectionDetails>> getDetails(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID courseSectionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(service.getDetails(studentId, courseSectionId, status, isActive));
    }

    @GetMapping
    public ResponseEntity<List<StudentCourseSection>> getAll(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID courseSectionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String registrationType,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(service.getAll(studentId, courseSectionId, status, registrationType, isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentCourseSection> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<StudentCourseSection> create(@RequestBody StudentCourseSection registration) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(registration));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentCourseSection> update(
            @PathVariable UUID id,
            @RequestBody StudentCourseSection registration) {
        return ResponseEntity.ok(service.update(id, registration));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestParam(required = false) UUID updatedBy) {
        service.updateStatus(id, status, updatedBy);
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