
package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.Department;
import com.example.quanlydaotao.n3.service.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/n3/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Department> getAll(
            @RequestParam(required = false) String keyword) {
        return service.getAll(keyword);
    }

    @GetMapping("/{id}")
    public Department getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department create(@RequestBody Department department) {
        return service.create(department);
    }

    @PutMapping("/{id}")
    public Department update(
            @PathVariable UUID id,
            @RequestBody Department department) {
        return service.update(id, department);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
