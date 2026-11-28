package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.service.CourseSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/course-sections")
@RequiredArgsConstructor
public class CourseSectionController {

    private final CourseSectionService service;

    @GetMapping
    public ResponseEntity<List<CourseSection>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID semesterId) {
        return ResponseEntity.ok(service.getAll(keyword, semesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseSection> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<CourseSection> create(@RequestBody CourseSection request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseSection> update(@PathVariable UUID id, @RequestBody CourseSection request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
