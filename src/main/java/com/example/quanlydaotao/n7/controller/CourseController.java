package com.example.quanlydaotao.n7.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n7.dto.request.CourseRequest;
import com.example.quanlydaotao.n7.dto.response.CourseResponse;
import com.example.quanlydaotao.n7.service.CourseService;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService service;
    public CourseController(CourseService service) { this.service = service; }

    @GetMapping
    public List<CourseResponse> list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active) { return service.list(keyword, active); }
    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@RequestBody CourseRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable UUID id, @RequestBody CourseRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }
}
