package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.dto.StudentCourseSectionDetails;
import com.example.quanlydaotao.n5.dto.CourseSectionStudentsResponse;
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
            @RequestParam(required = false) UUID courseSectionId) {
        return ResponseEntity.ok(service.getDetails(studentId, courseSectionId));
    }

    @GetMapping("/by-course-section/{courseSectionId}")
    public ResponseEntity<CourseSectionStudentsResponse> getStudentsByCourseSection(
            @PathVariable UUID courseSectionId) {
        return ResponseEntity.ok(service.getStudentsByCourseSection(courseSectionId));
    }

    @GetMapping
    public ResponseEntity<List<StudentCourseSectionDetails>> getAll(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID courseSectionId) {
        return ResponseEntity.ok(service.getDetails(studentId, courseSectionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentCourseSectionDetails> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getDetailsById(id));
    }

    @PostMapping
    public ResponseEntity<StudentCourseSectionDetails> create(@RequestBody StudentCourseSection registration) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(registration));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentCourseSectionDetails> update(
            @PathVariable UUID id,
            @RequestBody StudentCourseSection registration) {
        return ResponseEntity.ok(service.update(id, registration));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}