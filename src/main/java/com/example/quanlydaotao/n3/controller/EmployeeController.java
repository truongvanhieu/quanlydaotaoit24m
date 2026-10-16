
package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.Employee;
import com.example.quanlydaotao.n3.service.EmployeeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/n3/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // GET /api/n3/employees
    // GET /api/n3/employees?keyword=GV
    @GetMapping
    public List<Employee> getAll(
            @RequestParam(required = false) String keyword) {

        return employeeService.getAll(keyword);
    }

    // GET /api/n3/employees/{id}
    @GetMapping("/{id}")
    public Employee getById(@PathVariable UUID id) {

        return employeeService.getById(id);
    }

    // GET /api/n3/employees/department/{departmentId}
    @GetMapping("/department/{departmentId}")
    public List<Employee> getByDepartment(
            @PathVariable UUID departmentId) {

        return employeeService.getByDepartment(departmentId);
    }

    // POST /api/n3/employees
    @PostMapping
    public ResponseEntity<Employee> create(
            @RequestBody Employee employee) {

        Employee saved = employeeService.create(employee);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    // PUT /api/n3/employees/{id}
    @PutMapping("/{id}")
    public Employee update(
            @PathVariable UUID id,
            @RequestBody Employee employee) {

        return employeeService.update(id, employee);
    }

    // DELETE /api/n3/employees/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {

        employeeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
