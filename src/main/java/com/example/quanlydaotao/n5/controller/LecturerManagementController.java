package com.example.quanlydaotao.n5.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/lecturers")
@CrossOrigin("*")
public class LecturerManagementController {
    private final JdbcTemplate jdbcTemplate;

    public LecturerManagementController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Map<String, Object> mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("code", rs.getString("code"));
        m.put("fullName", rs.getString("full_name"));
        m.put("email", rs.getString("email"));
        m.put("phone", rs.getString("phone"));
        m.put("academicDegree", rs.getString("academic_degree"));
        m.put("specialization", rs.getString("specialization"));
        Object activeObj = rs.getObject("is_active");
        boolean active = activeObj == null || Boolean.TRUE.equals(activeObj)
                || "1".equals(String.valueOf(activeObj)) || "true".equalsIgnoreCase(String.valueOf(activeObj));
        m.put("isActive", active);
        return m;
    }

    @GetMapping
    public List<Map<String, Object>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, code, full_name, email, phone, academic_degree, specialization, is_active " +
                "FROM employees WHERE deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            sql.append(" AND (code LIKE ? OR full_name LIKE ? OR email LIKE ? OR phone LIKE ? OR specialization LIKE ?)");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (isActive != null) {
            sql.append(" AND is_active = ?");
            params.add(isActive ? 1 : 0);
        }
        sql.append(" ORDER BY code ASC");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable UUID id) {
        List<Map<String, Object>> list = jdbcTemplate.query(
                "SELECT id, code, full_name, email, phone, academic_degree, specialization, is_active " +
                "FROM employees WHERE id = ? AND deleted_at IS NULL",
                (rs, rowNum) -> mapRow(rs),
                id.toString()
        );
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên với ID: " + id);
        }
        return list.get(0);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        String code = body.get("code") != null ? String.valueOf(body.get("code")).trim() : "";
        String fullName = body.get("fullName") != null ? String.valueOf(body.get("fullName")).trim() : "";
        if (code.isBlank() || fullName.isBlank()) {
            throw new IllegalArgumentException("Mã giảng viên và Họ và tên không được để trống!");
        }

        Integer dup = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees WHERE code = ? AND deleted_at IS NULL",
                Integer.class,
                code
        );
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("Mã giảng viên '" + code + "' đã tồn tại!");
        }

        String id = UUID.randomUUID().toString();
        String email = body.get("email") != null ? String.valueOf(body.get("email")).trim() : null;
        String phone = body.get("phone") != null ? String.valueOf(body.get("phone")).trim() : null;
        String academicDegree = body.get("academicDegree") != null ? String.valueOf(body.get("academicDegree")).trim() : null;
        String specialization = body.get("specialization") != null ? String.valueOf(body.get("specialization")).trim() : null;
        boolean isActive = body.get("isActive") == null || Boolean.parseBoolean(String.valueOf(body.get("isActive")));

        jdbcTemplate.update(
                "INSERT INTO employees (id, code, full_name, email, phone, academic_degree, specialization, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())",
                id, code, fullName, email, phone, academicDegree, specialization, isActive ? 1 : 0
        );
        return getById(UUID.fromString(id));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        getById(id);
        String code = body.get("code") != null ? String.valueOf(body.get("code")).trim() : "";
        String fullName = body.get("fullName") != null ? String.valueOf(body.get("fullName")).trim() : "";
        if (code.isBlank() || fullName.isBlank()) {
            throw new IllegalArgumentException("Mã giảng viên và Họ và tên không được để trống!");
        }

        Integer dup = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees WHERE code = ? AND id <> ? AND deleted_at IS NULL",
                Integer.class,
                code, id.toString()
        );
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("Mã giảng viên '" + code + "' đã tồn tại!");
        }

        String email = body.get("email") != null ? String.valueOf(body.get("email")).trim() : null;
        String phone = body.get("phone") != null ? String.valueOf(body.get("phone")).trim() : null;
        String academicDegree = body.get("academicDegree") != null ? String.valueOf(body.get("academicDegree")).trim() : null;
        String specialization = body.get("specialization") != null ? String.valueOf(body.get("specialization")).trim() : null;
        boolean isActive = body.get("isActive") == null || Boolean.parseBoolean(String.valueOf(body.get("isActive")));

        jdbcTemplate.update(
                "UPDATE employees SET code = ?, full_name = ?, email = ?, phone = ?, academic_degree = ?, specialization = ?, is_active = ?, updated_at = GETDATE() " +
                "WHERE id = ?",
                code, fullName, email, phone, academicDegree, specialization, isActive ? 1 : 0, id.toString()
        );
        return getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        getById(id);
        jdbcTemplate.update(
                "UPDATE employees SET is_active = 0, deleted_at = GETDATE(), updated_at = GETDATE() WHERE id = ?",
                id.toString()
        );
        return ResponseEntity.ok(Map.of("message", "Đã xóa mềm giảng viên thành công", "isActive", false));
    }
}
