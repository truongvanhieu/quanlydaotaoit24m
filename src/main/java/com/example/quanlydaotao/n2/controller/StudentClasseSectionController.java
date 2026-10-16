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
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n2.entity.StudentClasseSection;
import com.example.quanlydaotao.n2.service.StudentClasseSectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/student-classe-sections")
@CrossOrigin(origins = "*")
@Tag(name = "3. Quản lý Sinh viên trong Lớp Hành chính (student_classe_sections)", description = "APIs quản lý danh sách sinh viên thuộc lớp hành chính")
public class StudentClasseSectionController {

    private final StudentClasseSectionService service;

    public StudentClasseSectionController(StudentClasseSectionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lấy toàn bộ danh sách phân lớp sinh viên")
    public ResponseEntity<List<StudentClasseSection>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết phân lớp theo ID")
    public ResponseEntity<StudentClasseSection> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Lấy lịch sử phân lớp của một sinh viên")
    public ResponseEntity<List<StudentClasseSection>> getByStudentId(@PathVariable UUID studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Lấy danh sách phân lớp theo lớp hành chính")
    public ResponseEntity<List<StudentClasseSection>> getByClassId(@PathVariable UUID classId) {
        return ResponseEntity.ok(service.getByStudentClasseId(classId));
    }

    @PostMapping
    @Operation(summary = "Gán sinh viên vào lớp hành chính")
    public ResponseEntity<StudentClasseSection> create(@RequestBody StudentClasseSection section) {
        return new ResponseEntity<>(service.save(section), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phân lớp (trạng thái, ngày kết thúc...)")
    public ResponseEntity<StudentClasseSection> update(
            @PathVariable UUID id,
            @RequestBody StudentClasseSection section) {
        section.setId(id);
        return ResponseEntity.ok(service.save(section));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm phân lớp")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
