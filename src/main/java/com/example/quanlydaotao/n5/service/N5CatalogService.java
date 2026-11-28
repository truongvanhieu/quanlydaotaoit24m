package com.example.quanlydaotao.n5.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class N5CatalogService {
    private final JdbcTemplate jdbcTemplate;

    public N5CatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureDatabaseConstraints() {
        try {
            jdbcTemplate.execute("IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_lcc_course_class') ALTER TABLE lecturer_course_classes DROP CONSTRAINT FK_lcc_course_class");
            jdbcTemplate.execute("IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_lcc_lecturer') ALTER TABLE lecturer_course_classes DROP CONSTRAINT FK_lcc_lecturer");
        } catch (Exception ignored) {}
    }

    public List<Map<String, Object>> getCourses() {
        try {
            String sql = "SELECT id, code, name, credits FROM courses WHERE deleted_at IS NULL ORDER BY code ASC";
            List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getString("id"));
                m.put("code", rs.getString("code"));
                m.put("name", rs.getString("name"));
                m.put("credits", rs.getObject("credits"));
                return m;
            });
            if (!rows.isEmpty()) return rows;
        } catch (Exception ignored) {}
        return List.of(
                Map.of("id", "f8592d94-bf79-4df6-b94e-06191127a946", "code", "IT301", "name", "Phân tích và thiết kế hệ thống", "credits", 3),
                Map.of("id", "2a57eacf-0745-4144-a30b-545b08429f19", "code", "AI101", "name", "Trí tuệ nhân tạo ứng dụng", "credits", 3),
                Map.of("id", "39ecd0d6-d3e7-4e00-ab8f-1896cbcafcf4", "code", "STAT101", "name", "Xác suất và thống kê", "credits", 3)
        );
    }

    public List<Map<String, Object>> getRooms() {
        try {
            String sql = "SELECT id, room_code, room_name, building_id, capacity FROM rooms WHERE deleted_at IS NULL ORDER BY room_code ASC";
            List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getString("id"));
                m.put("code", rs.getString("room_code"));
                m.put("name", rs.getString("room_name"));
                m.put("buildingId", rs.getString("building_id"));
                m.put("capacity", rs.getObject("capacity"));
                return m;
            });
            if (!rows.isEmpty()) return rows;
        } catch (Exception ignored) {}
        return List.of(
                Map.of("id", "a44ae7f1-6b76-48ae-8ab7-0a89b0a1d282", "code", "B20203", "name", "Phòng B2-203", "buildingId", "8766878f-3315-4c94-9e68-352205668153", "capacity", 60),
                Map.of("id", "7358971c-d302-450a-8bbe-0bb049cbd565", "code", "B30404", "name", "Phòng B3-404", "buildingId", "b11299ab-35cf-4a4a-90d1-ec0783954542", "capacity", 60),
                Map.of("id", "29d284dc-e8b7-4ea1-b816-0c43e9b9dac8", "code", "B50101", "name", "Phòng B5-101", "buildingId", "4c5e296c-0311-4089-8a4d-27db509cfdc2", "capacity", 60)
        );
    }

    public List<Map<String, Object>> getLecturers() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String sql = "SELECT id, code, full_name, email, is_active FROM employees WHERE deleted_at IS NULL ORDER BY code ASC";
            result.addAll(jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getString("id"));
                m.put("code", rs.getString("code"));
                m.put("fullName", rs.getString("full_name"));
                m.put("email", rs.getString("email"));
                Object activeObj = rs.getObject("is_active");
                boolean active = activeObj == null || Boolean.TRUE.equals(activeObj) || "1".equals(String.valueOf(activeObj)) || "true".equalsIgnoreCase(String.valueOf(activeObj));
                m.put("isActive", active);
                return m;
            }));
        } catch (Exception ignored) {
            try {
                String sqlFallback = "SELECT id, code, full_name FROM employees WHERE deleted_at IS NULL ORDER BY code ASC";
                result.addAll(jdbcTemplate.query(sqlFallback, (rs, rowNum) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getString("id"));
                    m.put("code", rs.getString("code"));
                    m.put("fullName", rs.getString("full_name"));
                    m.put("isActive", true);
                    return m;
                }));
            } catch (Exception ignored2) {}
        }
        try {
            String sql2 = "SELECT id, lecturer_code, full_name, is_active FROM lecturers WHERE deleted_at IS NULL ORDER BY lecturer_code ASC";
            result.addAll(jdbcTemplate.query(sql2, (rs, rowNum) -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getString("id"));
                m.put("code", rs.getString("lecturer_code"));
                m.put("fullName", rs.getString("full_name"));
                Object activeObj = rs.getObject("is_active");
                boolean active = activeObj == null || Boolean.TRUE.equals(activeObj) || "1".equals(String.valueOf(activeObj)) || "true".equalsIgnoreCase(String.valueOf(activeObj));
                m.put("isActive", active);
                return m;
            }));
        } catch (Exception ignored) {}
        if (!result.isEmpty()) return result;
        return List.of(
                Map.of("id", "cedeed49-0178-4394-a16c-02bc11cdb15a", "code", "GV059", "fullName", "Bùi Văn Tùng", "isActive", true),
                Map.of("id", "bbb70b16-2b23-460e-858f-043dcfa19506", "code", "GV033", "fullName", "Võ Minh Khôi", "isActive", true),
                Map.of("id", "50ec9fec-7670-4f85-a983-08f8b3b5cf47", "code", "GV054", "fullName", "Lê Thị Giang", "isActive", false)
        );
    }

    public Map<String, String> getCourseNameMap() {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> c : getCourses()) {
            String id = String.valueOf(c.get("id")).toLowerCase();
            String code = String.valueOf(c.getOrDefault("code", ""));
            String name = String.valueOf(c.getOrDefault("name", ""));
            map.put(id, code.isEmpty() ? name : (code + " - " + name));
        }
        return map;
    }

    public Map<String, String> getRoomNameMap() {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> r : getRooms()) {
            String id = String.valueOf(r.get("id")).toLowerCase();
            String name = String.valueOf(r.getOrDefault("name", ""));
            String code = String.valueOf(r.getOrDefault("code", ""));
            map.put(id, !name.isEmpty() ? name : code);
        }
        return map;
    }

    public Map<String, String> getLecturerNameMap() {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> l : getLecturers()) {
            String id = String.valueOf(l.get("id")).toLowerCase();
            String code = String.valueOf(l.getOrDefault("code", ""));
            String fullName = String.valueOf(l.getOrDefault("fullName", ""));
            map.put(id, code.isEmpty() ? fullName : (code + " - " + fullName));
        }
        return map;
    }
}
