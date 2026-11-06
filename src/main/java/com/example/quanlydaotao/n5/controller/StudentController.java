package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.entity.Student;
import com.example.quanlydaotao.n5.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Student> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Student getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public Student create(@RequestBody Student s) {
        return service.save(s);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable UUID id, @RequestBody Student s) {
        return service.update(id, s);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
