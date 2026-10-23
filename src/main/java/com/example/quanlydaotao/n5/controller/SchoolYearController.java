package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.service.SchoolYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/school-years")
@RequiredArgsConstructor
public class SchoolYearController {

    private final SchoolYearService schoolYearService;

    @GetMapping
    public ResponseEntity<List<SchoolYear>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(schoolYearService.getAll(keyword, isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolYear> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolYearService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SchoolYear> create(@RequestBody SchoolYear schoolYear) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolYearService.create(schoolYear));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolYear> update(
            @PathVariable UUID id,
            @RequestBody SchoolYear schoolYear) {
        return ResponseEntity.ok(schoolYearService.update(id, schoolYear));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam Boolean isActive,
            @RequestParam(required = false) UUID updatedBy) {
        schoolYearService.updateStatus(id, isActive, updatedBy);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID deletedBy) {
        schoolYearService.softDelete(id, deletedBy);
        return ResponseEntity.noContent().build();
    }
}