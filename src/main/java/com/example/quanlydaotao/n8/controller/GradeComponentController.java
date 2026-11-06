package com.example.quanlydaotao.n8.controller;

import com.example.quanlydaotao.n8.entity.GradeComponent;
import com.example.quanlydaotao.n8.service.GradeComponentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/n8/grade-components")
public class GradeComponentController {
    private final GradeComponentService service;

    public GradeComponentController(GradeComponentService service) {
        this.service = service;
    }

    @GetMapping
    public List<GradeComponent> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public GradeComponent getById(@PathVariable UUID id) { return service.getById(id); }

    @PostMapping
    public GradeComponent create(@RequestBody GradeComponent g) { return service.save(g); }

    @PutMapping("/{id}")
    public GradeComponent update(@PathVariable UUID id, @RequestBody GradeComponent g) {
        g.setId(id);
        return service.save(g);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) { service.delete(id); }
}