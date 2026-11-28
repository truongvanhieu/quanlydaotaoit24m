package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public ResponseEntity<List<Semester>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID schoolYearId) {
        return ResponseEntity.ok(semesterService.getAll(keyword, schoolYearId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Semester> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(semesterService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Semester> create(@RequestBody Semester semester) {
        return ResponseEntity.status(HttpStatus.CREATED).body(semesterService.create(semester));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Semester> update(
            @PathVariable UUID id,
            @RequestBody Semester semester) {
        return ResponseEntity.ok(semesterService.update(id, semester));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        semesterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}