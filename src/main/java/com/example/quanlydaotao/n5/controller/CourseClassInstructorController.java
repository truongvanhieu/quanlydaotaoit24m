package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.dto.CourseClassInstructorRequest;
import com.example.quanlydaotao.n5.dto.CourseClassInstructorDetails;
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
    public ResponseEntity<List<CourseClassInstructorDetails>> getAll(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) String instructorCode,
            @RequestParam(required = false) String instructorName,
            @RequestParam(required = false) String courseClassName,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(service.getAll(
                employeeId, instructorCode, instructorName, courseClassName, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseClassInstructorDetails> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getDetailsById(id));
    }

    @PostMapping
    public ResponseEntity<CourseClassInstructorDetails> create(@RequestBody CourseClassInstructorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseClassInstructorDetails> update(
            @PathVariable UUID id,
            @RequestBody CourseClassInstructorRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}