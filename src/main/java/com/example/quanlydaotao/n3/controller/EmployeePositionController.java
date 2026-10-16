
package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.EmployeePosition;
import com.example.quanlydaotao.n3.service.EmployeePositionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // DELETE /api/n3/employee-positions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
