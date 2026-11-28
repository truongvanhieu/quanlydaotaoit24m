package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.LecturerPositionHistory;
import com.example.quanlydaotao.n3.service.LecturerPositionHistoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/n3/lecturer-positions-history")
public class LecturerPositionHistoryController {

    private final LecturerPositionHistoryService service;

    public LecturerPositionHistoryController(LecturerPositionHistoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<LecturerPositionHistory> getAll() {
        return service.getAll();
    }

    @GetMapping("/deleted")
    public List<LecturerPositionHistory> getDeleted() {
        return service.getDeleted();
    }

    @GetMapping("/{id}")
    public LecturerPositionHistory getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping("/lecturer/{lecturerId}")
    public List<LecturerPositionHistory> getByLecturer(
            @PathVariable UUID lecturerId,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return service.getByLecturer(lecturerId, includeDeleted);
    }

    @PostMapping
    public ResponseEntity<LecturerPositionHistory> create(
            @RequestBody LecturerPositionHistory item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(item));
    }

    @PutMapping("/{id}")
    public LecturerPositionHistory update(
            @PathVariable UUID id,
            @RequestBody LecturerPositionHistory item) {
        return service.update(id, item);
    }

    @PutMapping("/{id}/end")
    public LecturerPositionHistory endTerm(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        LocalDate endDate = null;
        if (body != null && body.get("endDate") != null && !body.get("endDate").isBlank()) {
            endDate = LocalDate.parse(body.get("endDate").trim());
        }
        return service.endTerm(id, endDate);
    }

    @PutMapping("/{id}/restore")
    public LecturerPositionHistory restore(@PathVariable UUID id) {
        return service.restore(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
