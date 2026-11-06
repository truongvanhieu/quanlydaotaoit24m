package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.service.SchoolYearService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/school-years")
@CrossOrigin("*")
public class SchoolYearController {
    private final SchoolYearService service;

    public SchoolYearController(SchoolYearService service) {
        this.service = service;
    }

    @GetMapping
    public List<SchoolYear> getAll(@RequestParam(required = false) Boolean isActive) {
        return service.findAll(isActive);
    }

    @GetMapping("/{id}")
    public SchoolYear getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public SchoolYear create(@RequestBody SchoolYear e) {
        return service.save(e);
    }

    @PutMapping("/{id}")
    public SchoolYear update(@PathVariable UUID id, @RequestBody SchoolYear e) {
        return service.update(id, e);
    }

    @PatchMapping("/{id}/active")
    public SchoolYear updateActive(@PathVariable UUID id, @RequestBody(required = false) Map<String, Boolean> body) {
        Boolean isActive = body != null ? body.get("isActive") : null;
        return service.toggleActive(id, isActive);
    }

    @PatchMapping("/{id}/status")
    public SchoolYear updateStatus(@PathVariable UUID id, @RequestParam(required = false) Boolean isActive) {
        return service.toggleActive(id, isActive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
