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

import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@Tag(name = "1. Quản lý Hồ sơ Sinh viên (students)", description = "APIs quản lý hồ sơ sinh viên, tìm kiếm và phân lớp")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách hoặc tìm kiếm sinh viên theo từ khóa (Mã SV, Họ tên, CCCD, Trạng thái)")
    public ResponseEntity<List<Student>> listStudents(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(studentService.search(keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết sinh viên theo ID")
    public ResponseEntity<Student> getStudent(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Lấy danh sách sinh viên theo lớp hành chính")
    public ResponseEntity<List<Student>> getStudentsByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(studentService.getByClassId(classId));
    }

    @PostMapping
    @Operation(summary = "Thêm mới hồ sơ sinh viên")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return new ResponseEntity<>(studentService.save(student), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật hồ sơ sinh viên")
    public ResponseEntity<Student> updateStudent(
            @PathVariable UUID id,
            @RequestBody Student student) {
        student.setId(id);
        return ResponseEntity.ok(studentService.save(student));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm sinh viên (đánh dấu isActive = false)")
    public ResponseEntity<Void> deleteStudent(@PathVariable UUID id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}