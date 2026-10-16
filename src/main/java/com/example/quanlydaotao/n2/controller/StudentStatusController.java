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

import com.example.quanlydaotao.n2.entity.StudentStatus;
import com.example.quanlydaotao.n2.service.StudentStatusService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/student-statuses")
@CrossOrigin(origins = "*")
@Tag(name = "5. Quản lý Trạng thái Sinh viên (student_status)", description = "APIs quản lý danh mục và trạng thái sinh viên")
public class StudentStatusController {

    private final StudentStatusService service;

    public StudentStatusController(StudentStatusService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách hoặc tìm kiếm trạng thái sinh viên")
    public ResponseEntity<List<StudentStatus>> getAll(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết trạng thái theo ID")
    public ResponseEntity<StudentStatus> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Lấy danh sách trạng thái của một sinh viên")
    public ResponseEntity<List<StudentStatus>> getByStudentId(@PathVariable UUID studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    @PostMapping
    @Operation(summary = "Thêm mới trạng thái")
    public ResponseEntity<StudentStatus> create(@RequestBody StudentStatus status) {
        return new ResponseEntity<>(service.save(status), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin trạng thái")
    public ResponseEntity<StudentStatus> update(
            @PathVariable UUID id,
            @RequestBody StudentStatus status) {
        status.setId(id);
        return ResponseEntity.ok(service.save(status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm trạng thái")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
