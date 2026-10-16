package com.example.quanlydaotao.n2.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n2.entity.StudentClass;
import com.example.quanlydaotao.n2.service.StudentClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/student-classes")
@CrossOrigin(origins = "*")
@Tag(name = "2. Quản lý Lớp Hành chính (student_classes)", description = "APIs quản lý danh sách lớp hành chính/lớp sinh hoạt")
public class StudentClassController {

    private final StudentClassService studentClassService;

    public StudentClassController(StudentClassService studentClassService) {
        this.studentClassService = studentClassService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách hoặc tìm kiếm lớp hành chính")
    public ResponseEntity<List<StudentClass>> listClasses(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(studentClassService.search(keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết lớp hành chính theo ID")
    public ResponseEntity<StudentClass> getClassById(@PathVariable UUID id) {
        return ResponseEntity.ok(studentClassService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo mới lớp hành chính")
    public ResponseEntity<StudentClass> createClass(@RequestBody StudentClass studentClass) {
        return new ResponseEntity<>(studentClassService.save(studentClass), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật lớp hành chính")
    public ResponseEntity<StudentClass> updateClass(
            @PathVariable UUID id,
            @RequestBody StudentClass studentClass) {
        studentClass.setId(id);
        return ResponseEntity.ok(studentClassService.save(studentClass));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm lớp hành chính")
    public ResponseEntity<Void> deleteClass(@PathVariable UUID id) {
        studentClassService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
