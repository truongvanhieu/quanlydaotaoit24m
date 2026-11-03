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

import com.example.quanlydaotao.n7.dto.request.CourseSectionRequest;
import com.example.quanlydaotao.n7.dto.response.CourseSectionResponse;
import com.example.quanlydaotao.n7.dto.response.CourseSectionStudentResponse;
import com.example.quanlydaotao.n7.service.CourseSectionService;

@RestController
@RequestMapping("/api/course-sections")
public class CourseSectionController {
    private final CourseSectionService service;
    public CourseSectionController(CourseSectionService service) { this.service = service; }

    @GetMapping
    public List<CourseSectionResponse> list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active) { return service.list(keyword, active); }

    @GetMapping("/by-student/{studentId}")
    public List<CourseSectionResponse> byStudent(@PathVariable UUID studentId,
            @RequestParam(required = false) String keyword) { return service.byStudent(studentId, keyword); }

    @GetMapping("/by-lecturer/{lecturerId}")
    public List<CourseSectionResponse> byLecturer(@PathVariable UUID lecturerId,
            @RequestParam(required = false) String keyword) { return service.byLecturer(lecturerId, keyword); }

    @GetMapping("/{id}")
    public CourseSectionResponse get(@PathVariable UUID id) { return service.get(id); }

    @GetMapping("/{id}/students")
    public List<CourseSectionStudentResponse> students(@PathVariable UUID id) { return service.students(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseSectionResponse create(@RequestBody CourseSectionRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public CourseSectionResponse update(@PathVariable UUID id, @RequestBody CourseSectionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }
}
