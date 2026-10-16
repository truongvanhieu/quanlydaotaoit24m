
package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.Lecturer;
import com.example.quanlydaotao.n3.service.LecturerService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/n3/lecturers")
public class LecturerController {

    private final LecturerService service;

    public LecturerController(LecturerService service) {
        this.service = service;
    }

    @GetMapping
    public List<Lecturer> getAll(
            @RequestParam(required = false) String keyword) {
        return service.getAll(keyword);
    }

    @GetMapping("/{id}")
    public Lecturer getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Lecturer create(@RequestBody Lecturer lecturer) {
        return service.create(lecturer);
    }

    @PutMapping("/{id}")
    public Lecturer update(
            @PathVariable UUID id,
            @RequestBody Lecturer lecturer) {
        return service.update(id, lecturer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
