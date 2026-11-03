package com.example.quanlydaotao.n7.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n7.dto.request.LecturerAssignmentRequest;
import com.example.quanlydaotao.n7.dto.response.LecturerAssignmentResponse;
import com.example.quanlydaotao.n7.service.LecturerAssignmentService;

@RestController
@RequestMapping("/api/lecturer-assignments")
public class LecturerAssignmentController {
    private final LecturerAssignmentService service;
    public LecturerAssignmentController(LecturerAssignmentService service) { this.service = service; }

    @GetMapping
    public List<LecturerAssignmentResponse> list(@RequestParam(required = false) UUID lecturerId,
            @RequestParam(required = false) UUID courseClassId) { return service.list(lecturerId, courseClassId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LecturerAssignmentResponse assign(@RequestBody LecturerAssignmentRequest request) { return service.assign(request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID id) { service.remove(id); }
}
