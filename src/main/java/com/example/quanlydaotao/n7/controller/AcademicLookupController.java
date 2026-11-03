package com.example.quanlydaotao.n7.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n7.dto.response.CourseClassResponse;

@RestController
@RequestMapping("/api")
public class AcademicLookupController {
    private final JdbcTemplate jdbc;
    public AcademicLookupController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/course-classes")
    public List<CourseClassResponse> classes(@RequestParam(required = false) UUID lecturerId,
            @RequestParam(required = false) String keyword) {
        StringBuilder sql = new StringBuilder("""
                select distinct cc.id, cc.class_code, cc.course_id, c.code as course_code, c.name as course_name,
                       coalesce(lcc.lecturer_id, cc.lecturer_id) as lecturer_id,
                       coalesce(l.full_name, direct_l.full_name) as lecturer_name, cc.status
                from courses_classes cc
                left join courses c on c.id = cc.course_id
                left join lecturer_course_classes lcc on lcc.course_class_id = cc.id
                    and lcc.deleted_at is null and coalesce(lcc.is_active, 1) = 1
                left join lecturers l on l.id = lcc.lecturer_id
                left join lecturers direct_l on direct_l.id = cc.lecturer_id
                where cc.deleted_at is null and coalesce(cc.is_active, 1) = 1
                """);
        List<Object> args = new ArrayList<>();
        if (lecturerId != null) {
            sql.append(" and (cc.lecturer_id = ? or lcc.lecturer_id = ?)");
            args.add(lecturerId); args.add(lecturerId);
        }
        String term = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        if (term != null) {
            sql.append(" and (cc.class_code like ? or c.code like ? or c.name like ?)");
            args.add(term); args.add(term); args.add(term);
        }
        sql.append(" order by cc.class_code");
        return jdbc.query(sql.toString(), (rs, row) -> new CourseClassResponse(rs.getObject("id", UUID.class),
                rs.getString("class_code"), rs.getObject("course_id", UUID.class), rs.getString("course_code"),
                rs.getString("course_name"), rs.getObject("lecturer_id", UUID.class),
                rs.getString("lecturer_name"), rs.getString("status")), args.toArray());
    }
}
