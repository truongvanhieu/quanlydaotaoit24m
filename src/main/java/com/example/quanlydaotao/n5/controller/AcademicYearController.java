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
@CrossOrigin("*")
public class AcademicYearController {
    private final AcademicYearService service;

    public AcademicYearController(AcademicYearService service) {
        this.service = service;
    }

    @GetMapping
    public List<AcademicYear> getAll(@RequestParam(required = false) Boolean isActive) {
        return service.findAll(isActive);
    }

    @GetMapping("/{id}")
    public AcademicYear getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public AcademicYear create(@RequestBody AcademicYear e) {
        return service.save(e);
    }

    @PutMapping("/{id}")
    public AcademicYear update(@PathVariable UUID id, @RequestBody AcademicYear e) {
        return service.update(id, e);
    }

    @PatchMapping("/{id}/active")
    public AcademicYear updateActive(@PathVariable UUID id, @RequestBody(required = false) Map<String, Boolean> body) {
        Boolean isActive = body != null ? body.get("isActive") : null;
        return service.toggleActive(id, isActive);
    }

    @PatchMapping("/{id}/status")
    public AcademicYear updateStatus(@PathVariable UUID id, @RequestParam(required = false) Boolean isActive) {
        return service.toggleActive(id, isActive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
