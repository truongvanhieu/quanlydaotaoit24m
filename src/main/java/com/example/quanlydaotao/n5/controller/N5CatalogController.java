package com.example.quanlydaotao.n5.controller;

import com.example.quanlydaotao.n5.service.N5CatalogService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/n5/catalogs")
@CrossOrigin("*")
public class N5CatalogController {
    private final N5CatalogService catalogService;

    public N5CatalogController(N5CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> getCourses() {
        return catalogService.getCourses();
    }

    @GetMapping("/rooms")
    public List<Map<String, Object>> getRooms() {
        return catalogService.getRooms();
    }

    @GetMapping("/lecturers")
    public List<Map<String, Object>> getLecturers() {
        return catalogService.getLecturers();
    }
}
