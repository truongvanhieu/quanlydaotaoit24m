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

import com.example.quanlydaotao.n2.entity.AdvisorClasseSection;
import com.example.quanlydaotao.n2.service.AdvisorClasseSectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/advisor-classe-sections")
@CrossOrigin(origins = "*")
@Tag(name = "4. Quản lý CVHT phụ trách Lớp (advisor_classe_sections)", description = "APIs quản lý danh sách giảng viên / CVHT phụ trách lớp hành chính")
public class AdvisorClasseSectionController {

    private final AdvisorClasseSectionService service;

    public AdvisorClasseSectionController(AdvisorClasseSectionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lấy toàn bộ danh sách phân công CVHT phụ trách lớp")
    public ResponseEntity<List<AdvisorClasseSection>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết phân công CVHT theo ID")
    public ResponseEntity<AdvisorClasseSection> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/advisor/{employeeId}")
    @Operation(summary = "Lấy danh sách các lớp do giảng viên phụ trách")
    public ResponseEntity<List<AdvisorClasseSection>> getByEmployeeId(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(service.getByEmployeeId(employeeId));
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Lấy danh sách CVHT của lớp hành chính")
    public ResponseEntity<List<AdvisorClasseSection>> getByClassId(@PathVariable UUID classId) {
        return ResponseEntity.ok(service.getByStudentClasseId(classId));
    }

    @PostMapping
    @Operation(summary = "Phân công CVHT phụ trách lớp hành chính")
    public ResponseEntity<AdvisorClasseSection> create(@RequestBody AdvisorClasseSection section) {
        return new ResponseEntity<>(service.save(section), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phân công CVHT (ngày kết thúc, trạng thái...)")
    public ResponseEntity<AdvisorClasseSection> update(
            @PathVariable UUID id,
            @RequestBody AdvisorClasseSection section) {
        section.setId(id);
        return ResponseEntity.ok(service.save(section));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm phân công CVHT")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
