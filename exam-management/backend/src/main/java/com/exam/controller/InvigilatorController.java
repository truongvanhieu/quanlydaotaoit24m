package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.service.InvigilatorService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invigilators")
public class InvigilatorController {
    private final InvigilatorService service;

    public InvigilatorController(InvigilatorService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> list() {
        return ResponseEntity.ok(ApiResponse.successData(service.list()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody Map<String, String> input) {
        try {
            Map<String, Object> item = service.create(input.get("staff_code"), input.get("full_name"),
                    input.get("department"), input.get("phone"));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Đã lưu cán bộ coi thi vào SQL Server", item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không lưu được cán bộ: mã cán bộ trùng hoặc dữ liệu không hợp lệ"));
        }
    }
}
