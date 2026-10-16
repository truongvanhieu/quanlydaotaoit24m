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

import com.example.quanlydaotao.n2.entity.StudentStatusHistory;
import com.example.quanlydaotao.n2.service.StudentStatusHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/student-status-histories")
@CrossOrigin(origins = "*")
@Tag(name = "6. Quản lý Lịch sử Trạng thái Sinh viên (student_status_histories)", description = "APIs lưu lại toàn bộ quá trình thay đổi trạng thái sinh viên (bảo lưu, thôi học, tốt nghiệp...)")
public class StudentStatusHistoryController {

    private final StudentStatusHistoryService service;

    public StudentStatusHistoryController(StudentStatusHistoryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lấy toàn bộ lịch sử thay đổi trạng thái")
    public ResponseEntity<List<StudentStatusHistory>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết bản ghi lịch sử trạng thái theo ID")
    public ResponseEntity<StudentStatusHistory> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Lấy toàn bộ tiến trình lịch sử trạng thái của một sinh viên")
    public ResponseEntity<List<StudentStatusHistory>> getByStudentId(@PathVariable UUID studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    @GetMapping("/status/{statusId}")
    @Operation(summary = "Lấy danh sách lịch sử theo loại trạng thái")
    public ResponseEntity<List<StudentStatusHistory>> getByStatusId(@PathVariable UUID statusId) {
        return ResponseEntity.ok(service.getByStatusId(statusId));
    }

    @PostMapping
    @Operation(summary = "Ghi nhận thay đổi trạng thái mới (Tạo bản ghi lịch sử mới)")
    public ResponseEntity<StudentStatusHistory> create(@RequestBody StudentStatusHistory history) {
        return new ResponseEntity<>(service.recordNewHistory(history), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin bản ghi lịch sử trạng thái")
    public ResponseEntity<StudentStatusHistory> update(
            @PathVariable UUID id,
            @RequestBody StudentStatusHistory history) {
        return ResponseEntity.ok(service.update(id, history));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm bản ghi lịch sử trạng thái")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
