
package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.EmployeePosition;
import com.example.quanlydaotao.n3.service.EmployeePositionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/n3/employee-positions")
public class EmployeePositionController {

    private final EmployeePositionService service;

    public EmployeePositionController(
            EmployeePositionService service) {
        this.service = service;
    }

    // GET /api/n3/employee-positions
    @GetMapping
    public List<EmployeePosition> getAll() {

        return service.getAll();
    }

    // GET /api/n3/employee-positions/deleted
    @GetMapping("/deleted")
    public List<EmployeePosition> getDeleted() {
        return service.getDeleted();
    }

    // GET /api/n3/employee-positions/{id}
    @GetMapping("/{id}")
    public EmployeePosition getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    // GET /api/n3/employee-positions/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public List<EmployeePosition> getByEmployee(
            @PathVariable UUID employeeId) {

        return service.getByEmployee(employeeId);
    }

    // GET /api/n3/employee-positions/employee/{employeeId}/history
    @GetMapping("/employee/{employeeId}/history")
    public List<EmployeePosition> getHistoryByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        return service.getHistoryByEmployee(employeeId, includeDeleted);
    }

    // POST /api/n3/employee-positions
    @PostMapping
    public ResponseEntity<EmployeePosition> create(
            @RequestBody EmployeePosition item) {

        EmployeePosition saved = service.create(item);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    // PUT /api/n3/employee-positions/{id}
    @PutMapping("/{id}")
    public EmployeePosition update(
            @PathVariable UUID id,
            @RequestBody EmployeePosition item) {

        return service.update(id, item);
    }

    // PUT /api/n3/employee-positions/{id}/end
    @PutMapping("/{id}/end")
    public EmployeePosition endTerm(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        LocalDateTime endDate = null;
        if (body != null && body.get("endDate") != null && !body.get("endDate").isBlank()) {
            String raw = body.get("endDate").trim();
            if (raw.length() == 16) {
                raw += ":00";
            }
            endDate = LocalDateTime.parse(raw);
        }
        return service.endTerm(id, endDate);
    }

    // PUT /api/n3/employee-positions/{id}/restore
    @PutMapping("/{id}/restore")
    public EmployeePosition restore(@PathVariable UUID id) {
        return service.restore(id);
    }

    // DELETE /api/n3/employee-positions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
