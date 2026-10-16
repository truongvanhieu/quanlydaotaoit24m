
package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.Position;
import com.example.quanlydaotao.n3.service.PositionService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/n3/positions")
public class PositionController {

    private final PositionService service;

    public PositionController(PositionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Position> getAll(
            @RequestParam(required = false) String keyword) {
        return service.getAll(keyword);
    }

    @GetMapping("/{id}")
    public Position getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Position create(@RequestBody Position position) {
        return service.create(position);
    }

    @PutMapping("/{id}")
    public Position update(
            @PathVariable UUID id,
            @RequestBody Position position) {
        return service.update(id, position);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
