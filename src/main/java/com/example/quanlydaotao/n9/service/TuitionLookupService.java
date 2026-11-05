package com.example.quanlydaotao.n9.service;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n9.dto.LookupOption;

/**
 * Cung cấp dữ liệu cho các hộp chọn (dropdown) trong module học phí:
 * sinh viên, học kỳ và chương trình đào tạo.
 */
@Service
public class TuitionLookupService {

    private static final RowMapper<LookupOption> LOOKUP_OPTION_MAPPER = (rs, rowNum) -> new LookupOption(
            UUID.fromString(rs.getString("id")),
            rs.getString("code"),
            rs.getString("name"));

    private final JdbcTemplate jdbcTemplate;

    public TuitionLookupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Danh sách sinh viên (bảng students_) để chọn khi tạo học phí. */
    public List<LookupOption> listStudents(String keyword) {
        String sql = "SELECT id, code, full_name AS name FROM students_ WHERE deleted_at IS NULL";

        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";

            return jdbcTemplate.query(
                    sql + " AND (code LIKE ? OR full_name LIKE ?) ORDER BY full_name",
                    LOOKUP_OPTION_MAPPER,
                    like,
                    like);
        }

        return jdbcTemplate.query(sql + " ORDER BY full_name", LOOKUP_OPTION_MAPPER);
    }

    /** Danh sách học kỳ. */
    public List<LookupOption> listSemesters() {
        String sql = "SELECT id, code, name FROM semesters WHERE deleted_at IS NULL"
                + " ORDER BY start_date DESC, name";

        return jdbcTemplate.query(sql, LOOKUP_OPTION_MAPPER);
    }

    /** Danh sách chương trình đào tạo. */
    public List<LookupOption> listTrainingPrograms() {
        String sql = "SELECT id, code, name FROM training_programs WHERE deleted_at IS NULL ORDER BY name";

        return jdbcTemplate.query(sql, LOOKUP_OPTION_MAPPER);
    }
}
