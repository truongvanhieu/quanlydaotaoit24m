package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.AcademicYear;
import com.example.quanlydaotao.n5.service.AcademicYearService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(
            AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @GetMapping
    public ResponseEntity<List<AcademicYear>> getAll(
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(academicYearService.getAll(isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicYear> getById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(academicYearService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AcademicYear> create(
            @RequestBody AcademicYear academicYear) {
        return ResponseEntity.ok(
                academicYearService.create(academicYear));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicYear> update(
            @PathVariable UUID id,
            @RequestBody AcademicYear academicYear) {
        return ResponseEntity.ok(
                academicYearService.update(id, academicYear));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID deletedBy) {
        academicYearService.delete(id, deletedBy);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<AcademicYear> updateActive(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {

        Boolean isActive = body.get("isActive");

        if (isActive == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                academicYearService.updateActive(id, isActive));
    }
}