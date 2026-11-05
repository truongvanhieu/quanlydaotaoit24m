package com.example.quanlydaotao.n9.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n9.dto.StudentTuitionView;
import com.example.quanlydaotao.n9.entity.StudentTuition;
import com.example.quanlydaotao.n9.service.StudentTuitionService;

/**
 * Quản lý học phí sinh viên theo học kỳ (student_tuition).
 * Các API GET trả về {@link StudentTuitionView} để có sẵn mã/tên sinh viên và học kỳ.
 */
@RestController
@RequestMapping("/api/student-tuitions")
public class StudentTuitionController {

    @Autowired
    private StudentTuitionService studentTuitionService;

    @GetMapping
    public List<StudentTuitionView> listStudentTuitions(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) Integer status) {

        return studentTuitionService.searchView(studentId, semesterId, status);
    }

    @GetMapping("/{id}")
    public StudentTuitionView getStudentTuition(@PathVariable UUID id) {

        return studentTuitionService.getViewById(id);
    }

    @PostMapping
    public StudentTuition createStudentTuition(@RequestBody StudentTuition studentTuition) {

        return studentTuitionService.create(studentTuition);
    }

    @PutMapping("/{id}")
    public StudentTuition updateStudentTuition(
            @PathVariable UUID id,
            @RequestBody StudentTuition studentTuition) {

        return studentTuitionService.update(id, studentTuition);
    }

    @DeleteMapping("/{id}")
    public void deleteStudentTuition(@PathVariable UUID id) {

        studentTuitionService.delete(id);
    }
}
