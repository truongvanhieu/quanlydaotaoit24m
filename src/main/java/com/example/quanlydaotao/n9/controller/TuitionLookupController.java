package com.example.quanlydaotao.n9.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n9.dto.LookupOption;
import com.example.quanlydaotao.n9.service.TuitionLookupService;

/**
 * Dữ liệu cho các hộp chọn (dropdown) của module học phí.
 */
@RestController
@RequestMapping("/api/tuition-lookups")
public class TuitionLookupController {

    @Autowired
    private TuitionLookupService tuitionLookupService;

    @GetMapping("/students")
    public List<LookupOption> listStudents(
            @RequestParam(required = false) String keyword) {

        return tuitionLookupService.listStudents(keyword);
    }

    @GetMapping("/semesters")
    public List<LookupOption> listSemesters() {

        return tuitionLookupService.listSemesters();
    }

    @GetMapping("/training-programs")
    public List<LookupOption> listTrainingPrograms() {

        return tuitionLookupService.listTrainingPrograms();
    }
}
