package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.service.StudentPortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-portal")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    public StudentPortalController(StudentPortalService studentPortalService) {
        this.studentPortalService = studentPortalService;
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<Object>> getStudentExamScheduleAndResults(@PathVariable("identifier") String identifier) {
        ApiResponse<Object> res = studentPortalService.getStudentScheduleAndResults(identifier);
        if (!res.isSuccess()) return ResponseEntity.status(404).body(res);
        return ResponseEntity.ok(res);
    }
}
