package com.exam.service;

import jakarta.annotation.PostConstruct;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Danh mục cán bộ coi thi; không thay đổi các bảng và API cũ. */
@Service
public class InvigilatorService {
    private final JdbcTemplate jdbc;

    public InvigilatorService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Dùng câu lệnh có điều kiện: chỉ tạo bảng mới khi chưa tồn tại. */
    @PostConstruct
    public void ensureSchema() {
        jdbc.execute("""
            IF OBJECT_ID(N'dbo.invigilators', N'U') IS NULL
            BEGIN
                CREATE TABLE dbo.invigilators (
                    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_invigilators_id DEFAULT NEWID() CONSTRAINT PK_invigilators PRIMARY KEY,
                    staff_code NVARCHAR(30) NOT NULL,
                    full_name NVARCHAR(150) NOT NULL,
                    department NVARCHAR(150) NULL,
                    phone NVARCHAR(20) NULL,
                    is_active BIT NOT NULL CONSTRAINT DF_invigilators_active DEFAULT (1),
                    created_at DATETIME2 NOT NULL CONSTRAINT DF_invigilators_created DEFAULT SYSDATETIME(),
                    CONSTRAINT UQ_invigilators_staff_code UNIQUE (staff_code)
                );
            END
            """);
        jdbc.execute("""
            IF OBJECT_ID(N'dbo.exam_invigilators', N'U') IS NULL
            BEGIN
                CREATE TABLE dbo.exam_invigilators (
                    exam_id UNIQUEIDENTIFIER NOT NULL,
                    invigilator_id UNIQUEIDENTIFIER NOT NULL,
                    assigned_at DATETIME2 NOT NULL CONSTRAINT DF_exam_invigilators_created DEFAULT SYSDATETIME(),
                    CONSTRAINT PK_exam_invigilators PRIMARY KEY (exam_id, invigilator_id),
                    CONSTRAINT FK_exam_invigilators_exam FOREIGN KEY (exam_id) REFERENCES dbo.exams(id),
                    CONSTRAINT FK_exam_invigilators_staff FOREIGN KEY (invigilator_id) REFERENCES dbo.invigilators(id)
                );
            END
            """);
    }

    public List<Map<String, Object>> list() {
        return jdbc.query("""
            SELECT id, staff_code, full_name, department, phone
            FROM dbo.invigilators WHERE is_active = 1
            ORDER BY full_name, staff_code
            """, (rs, n) -> asMap(rs.getString("id"), rs.getString("staff_code"),
                    rs.getString("full_name"), rs.getString("department"), rs.getString("phone")));
    }

    public Map<String, Object> create(String code, String name, String department, String phone) {
        String fullName = check(name, 150, true, "Họ tên cán bộ");
        String staffCode = check(code, 30, false, "Mã cán bộ");
        if (staffCode.isEmpty()) staffCode = "CB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String dept = check(department, 150, false, "Khoa / Bộ môn");
        String tel = check(phone, 20, false, "Số điện thoại");
        UUID id = UUID.randomUUID();
        final String codeToSave = staffCode;
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO dbo.invigilators (id, staff_code, full_name, department, phone)
                VALUES (CONVERT(uniqueidentifier, ?), ?, ?, ?, ?)
                """);
            ps.setString(1, id.toString());
            ps.setNString(2, codeToSave);
            ps.setNString(3, fullName);
            ps.setNString(4, dept.isEmpty() ? null : dept);
            ps.setNString(5, tel.isEmpty() ? null : tel);
            return ps;
        });
        return asMap(id.toString(), staffCode, fullName, dept.isEmpty() ? null : dept, tel.isEmpty() ? null : tel);
    }

    private String check(String value, int max, boolean required, String field) {
        String safe = value == null ? "" : value.trim();
        if (required && safe.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập " + field);
        if (safe.length() > max) throw new IllegalArgumentException(field + " không quá " + max + " ký tự");
        return safe;
    }

    private Map<String, Object> asMap(String id, String code, String name, String dept, String phone) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("staff_code", code);
        m.put("full_name", name);
        m.put("department", dept);
        m.put("phone", phone);
        return m;
    }

    public List<Map<String, Object>> listAssigned(UUID examId) {
        return jdbc.query("""
            SELECT i.id, i.staff_code, i.full_name, i.department, i.phone
            FROM dbo.exam_invigilators a
            JOIN dbo.invigilators i ON i.id = a.invigilator_id
            WHERE a.exam_id = CONVERT(uniqueidentifier, ?)
            ORDER BY i.full_name, i.staff_code
            """, (rs, row) -> asMap(rs.getString("id"), rs.getString("staff_code"),
                    rs.getString("full_name"), rs.getString("department"), rs.getString("phone")), examId.toString());
    }

    /** Chỉ ghi đè các cán bộ đã chọn khi request truyền invigilator_ids (kể cả []). */
    public void saveAssignments(UUID examId, List<UUID> invigilatorIds, Short supervisorCount) {
        if (invigilatorIds == null) return;
        if (supervisorCount == null || supervisorCount < 1 || supervisorCount > 10) {
            throw new IllegalArgumentException("Số lượng cán bộ coi thi phải từ 1 đến 10");
        }
        if (invigilatorIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("Danh sách cán bộ có ID không hợp lệ");
        }
        Set<UUID> unique = new LinkedHashSet<>(invigilatorIds);
        if (unique.size() != invigilatorIds.size()) {
            throw new IllegalArgumentException("Không được chọn trùng cán bộ coi thi");
        }
        if (unique.size() > supervisorCount) {
            throw new IllegalArgumentException("Số cán bộ đã chọn vượt quá số lượng cán bộ coi thi cần có");
        }
        for (UUID id : unique) {
            Integer found = jdbc.queryForObject("""
                SELECT COUNT(*) FROM dbo.invigilators
                WHERE id = CONVERT(uniqueidentifier, ?) AND is_active = 1
                """, Integer.class, id.toString());
            if (found == null || found != 1) {
                throw new IllegalArgumentException("Có cán bộ coi thi không tồn tại hoặc đã bị ngừng sử dụng");
            }
        }
        jdbc.update("DELETE FROM dbo.exam_invigilators WHERE exam_id = CONVERT(uniqueidentifier, ?)", examId.toString());
        for (UUID id : unique) {
            jdbc.update("""
                INSERT INTO dbo.exam_invigilators (exam_id, invigilator_id)
                VALUES (CONVERT(uniqueidentifier, ?), CONVERT(uniqueidentifier, ?))
                """, examId.toString(), id.toString());
        }
    }
}
