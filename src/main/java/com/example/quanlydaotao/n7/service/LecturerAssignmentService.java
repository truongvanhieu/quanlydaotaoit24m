package com.example.quanlydaotao.n7.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.LecturerAssignmentRequest;
import com.example.quanlydaotao.n7.dto.response.LecturerAssignmentResponse;

@Service
@Transactional(readOnly = true)
public class LecturerAssignmentService {
    private final JdbcTemplate jdbc;
    public LecturerAssignmentService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<LecturerAssignmentResponse> list(UUID lecturerId, UUID courseClassId) {
        StringBuilder sql = new StringBuilder("""
                select lcc.id, lcc.course_class_id, cc.class_code, c.code as course_code,
                       c.name as course_name, lcc.lecturer_id, l.lecturer_code, l.full_name,
                       lcc.role, lcc.is_active
                from lecturer_course_classes lcc
                join courses_classes cc on cc.id = lcc.course_class_id
                left join courses c on c.id = cc.course_id
                join lecturers l on l.id = lcc.lecturer_id
                where lcc.deleted_at is null and coalesce(lcc.is_active, 1) = 1
                """);
        List<Object> args = new ArrayList<>();
        if (lecturerId != null) { sql.append(" and lcc.lecturer_id = ?"); args.add(lecturerId); }
        if (courseClassId != null) { sql.append(" and lcc.course_class_id = ?"); args.add(courseClassId); }
        sql.append(" order by cc.class_code, l.full_name");
        return jdbc.query(sql.toString(), (rs, row) -> new LecturerAssignmentResponse(
                rs.getObject("id", UUID.class), rs.getObject("course_class_id", UUID.class),
                rs.getString("class_code"), rs.getString("course_code"), rs.getString("course_name"),
                rs.getObject("lecturer_id", UUID.class),
                rs.getString("lecturer_code"), rs.getString("full_name"), rs.getString("role"),
                rs.getObject("is_active", Boolean.class)), args.toArray());
    }

    @Transactional
    public LecturerAssignmentResponse assign(LecturerAssignmentRequest request) {
        if (request == null || request.courseClassId() == null || request.lecturerId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lớp học phần và giảng viên là bắt buộc");
        if (!exists("courses_classes", request.courseClassId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần");
        if (!exists("lecturers", request.lecturerId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên");
        Integer count = jdbc.queryForObject("""
                select count(*) from lecturer_course_classes
                where course_class_id = ? and lecturer_id = ? and deleted_at is null and coalesce(is_active, 1) = 1
                """, Integer.class, request.courseClassId(), request.lecturerId());
        if (count != null && count > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Giảng viên đã được phân công vào lớp này");
        UUID id = UUID.randomUUID();
        jdbc.update("""
                insert into lecturer_course_classes
                  (id, lecturer_id, course_class_id, role, created_at, is_active)
                values (?, ?, ?, ?, sysdatetime(), 1)
                """, id, request.lecturerId(), request.courseClassId(), blankToNull(request.role()));
        return list(request.lecturerId(), request.courseClassId()).stream()
                .filter(assignment -> assignment.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional
    public void remove(UUID id) {
        int changed = jdbc.update("update lecturer_course_classes set deleted_at = sysdatetime(), is_active = 0 where id = ? and deleted_at is null", id);
        if (changed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công");
    }

    private boolean exists(String table, UUID id) {
        Integer found = jdbc.queryForObject("select case when exists (select 1 from " + table + " where id = ?) then 1 else 0 end", Integer.class, id);
        return Integer.valueOf(1).equals(found);
    }
    private String blankToNull(String text) { return text == null || text.isBlank() ? null : text.trim(); }
}
