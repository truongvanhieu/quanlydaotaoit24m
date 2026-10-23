package com.example.quanlydaotao.n6.service;

import com.example.quanlydaotao.n6.dto.*;
import com.example.quanlydaotao.n6.entity.*;
import com.example.quanlydaotao.n6.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class N6Service {
    private final RegistrationPeriodRepository periods;
    private final CourseRegistrationRepository registrations;
    private final CourseEquivalenceRepository equivalences;
    private final JdbcTemplate db;

    public N6Service(RegistrationPeriodRepository periods, CourseRegistrationRepository registrations,
                     CourseEquivalenceRepository equivalences, JdbcTemplate db) {
        this.periods = periods; this.registrations = registrations;
        this.equivalences = equivalences; this.db = db;
    }
    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    private boolean exists(String table, UUID id) {
        // table is a hardcoded constant supplied only by server-side call sites
        Integer n = db.queryForObject("SELECT COUNT(*) FROM dbo." + table + " WHERE id = ?", Integer.class, id);
        return n != null && n > 0;
    }
    public List<RegistrationPeriod> periods() { return periods.findByDeletedAtIsNull(); }
    @Transactional
    public RegistrationPeriod savePeriod(UUID id, PeriodRequest input) {
        if (input == null || input.name() == null || input.name().isBlank() || input.semesterId() == null
                || input.startTime() == null || input.endTime() == null) throw bad("Thiếu thông tin đợt đăng ký");
        if (!input.endTime().isAfter(input.startTime())) throw bad("endTime phải sau startTime");
        if (!exists("semesters", input.semesterId())) throw bad("Học kỳ không tồn tại");
        int min = input.minCredits() == null ? 12 : input.minCredits();
        int max = input.maxCredits() == null ? 25 : input.maxCredits();
        if (min < 0 || max > 255 || max < min) throw bad("Giới hạn tín chỉ không hợp lệ");
        RegistrationPeriod p = id == null ? new RegistrationPeriod() : periods.findById(id)
                .filter(x -> x.deletedAt == null).orElseThrow(() -> bad("Không tìm thấy đợt đăng ký"));
        p.name = input.name().trim(); p.semesterId = input.semesterId();
        p.startTime = input.startTime(); p.endTime = input.endTime();
        p.targetConfig = input.targetConfig(); p.minCredits = (short) min; p.maxCredits = (short) max;
        p.allowRetake = Boolean.TRUE.equals(input.allowRetake()); p.active = Boolean.TRUE.equals(input.active());
        p.updatedAt = LocalDateTime.now();
        return periods.save(p);
    }
    @Transactional
    public void deletePeriod(UUID id) {
        RegistrationPeriod p = periods.findById(id).orElseThrow(() -> bad("Đợt đăng ký không tồn tại"));
        p.deletedAt = LocalDateTime.now(); p.active = false; periods.save(p);
    }
    public List<CourseRegistration> registrations() { return registrations.findByDeletedAtIsNull(); }
    @Transactional
    public CourseRegistration register(RegisterRequest in) {
        if (in == null || in.studentId() == null || in.courseClassId() == null || in.registrationPeriodId() == null
                || in.registrationType() == null) throw bad("Thiếu thông tin đăng ký");
        if (in.registrationType() < 1 || in.registrationType() > 3) throw bad("Loại đăng ký phải từ 1 đến 3");
        // Lock registration period for concurrent registrations within a period.
        List<Map<String,Object>> rows = db.queryForList("""
            SELECT id, semester_id, start_time, end_time, allow_retake, is_active, max_credits
            FROM dbo.registration_periods WITH (UPDLOCK, HOLDLOCK)
            WHERE id = ? AND deleted_at IS NULL
            """, in.registrationPeriodId());
        if (rows.isEmpty()) throw bad("Đợt đăng ký không tồn tại");
        Map<String,Object> period = rows.get(0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = ((java.sql.Timestamp) period.get("start_time")).toLocalDateTime();
        LocalDateTime end = ((java.sql.Timestamp) period.get("end_time")).toLocalDateTime();
        if (!Boolean.TRUE.equals(period.get("is_active")) || now.isBefore(start) || now.isAfter(end))
            throw bad("Đợt đăng ký hiện chưa mở");
        if (in.registrationType() != 1 && !Boolean.TRUE.equals(period.get("allow_retake")))
            throw bad("Đợt này không cho phép học lại/cải thiện");
        if (!exists("students_", in.studentId()) || !exists("courses_classes", in.courseClassId()))
            throw bad("Sinh viên hoặc lớp học phần không tồn tại");
        Integer sameSemester = db.queryForObject("""
            SELECT COUNT(*) FROM dbo.courses_classes
            WHERE id = ? AND semester_id = ? AND deleted_at IS NULL
            """, Integer.class, in.courseClassId(), period.get("semester_id"));
        if (sameSemester == null || sameSemester == 0) throw bad("Lớp học phần không thuộc học kỳ đăng ký");
        Integer duplicate = db.queryForObject("""
            SELECT COUNT(*) FROM dbo.course_registrations
            WHERE student_id = ? AND course_class_id = ? AND deleted_at IS NULL AND status IN (1,2)
            """, Integer.class, in.studentId(), in.courseClassId());
        if (duplicate != null && duplicate > 0) throw bad("Sinh viên đã đăng ký lớp này");
        BigDecimal courseCredits = db.queryForObject("""
            SELECT ISNULL(c.credits, 0) FROM dbo.courses_classes cc
            JOIN dbo.courses c ON c.id = cc.course_id WHERE cc.id = ?
            """, BigDecimal.class, in.courseClassId());
        BigDecimal used = db.queryForObject("""
            SELECT ISNULL(SUM(ISNULL(c.credits,0)), 0)
            FROM dbo.course_registrations r
            JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
            JOIN dbo.courses c ON c.id = cc.course_id
            WHERE r.student_id = ? AND r.registration_period_id = ?
              AND r.deleted_at IS NULL AND r.status IN (1,2)
            """, BigDecimal.class, in.studentId(), in.registrationPeriodId());
        int max = period.get("max_credits") == null ? 25 : ((Number)period.get("max_credits")).intValue();
        if ((used == null ? BigDecimal.ZERO : used).add(courseCredits == null ? BigDecimal.ZERO : courseCredits).compareTo(BigDecimal.valueOf(max)) > 0)
            throw bad("Vượt giới hạn tín chỉ của đợt đăng ký");
        // Prerequisites, timetable clashes, capacity, retake eligibility and per-student target_config
        // require integration with N1/N3/N7/grade modules before production use.
        CourseRegistration r = new CourseRegistration();
        r.studentId = in.studentId(); r.courseClassId = in.courseClassId();
        r.registrationPeriodId = in.registrationPeriodId(); r.registrationType = in.registrationType();
        r.replacedGradeId = in.replacedGradeId(); r.status = 2; r.paid = false;
        return registrations.save(r);
    }
    @Transactional
    public CourseRegistration cancel(UUID id) {
        CourseRegistration r = registrations.findById(id).filter(x -> x.deletedAt == null)
            .orElseThrow(() -> bad("Đăng ký không tồn tại"));
        if (r.status != null && r.status == 3) return r;
        r.status = 3; r.updatedAt = LocalDateTime.now(); return registrations.save(r);
    }
    public List<CourseEquivalence> equivalences() { return equivalences.findByDeletedAtIsNull(); }
    @Transactional
    public CourseEquivalence saveEquivalence(UUID id, EquivalenceRequest in) {
        if (in == null || in.originalCourseId() == null || in.equivalentCourseId() == null)
            throw bad("Thiếu mã môn học");
        if (in.originalCourseId().equals(in.equivalentCourseId())) throw bad("Không thể tự tương đương");
        if (!exists("courses", in.originalCourseId()) || !exists("courses", in.equivalentCourseId()))
            throw bad("Môn học không tồn tại");
        if (in.equivalenceType() != null && in.equivalenceType() != 1 && in.equivalenceType() != 2)
            throw bad("Loại tương đương phải là 1 hoặc 2");
        CourseEquivalence e = id == null ? new CourseEquivalence() : equivalences.findById(id)
            .filter(x -> x.deletedAt == null).orElseThrow(() -> bad("Không tìm thấy môn tương đương"));
        e.originalCourseId = in.originalCourseId(); e.equivalentCourseId = in.equivalentCourseId();
        e.equivalenceType = in.equivalenceType(); e.effectDate = in.effectDate();
        e.active = Boolean.TRUE.equals(in.active()); e.note = in.note(); e.updatedAt = LocalDateTime.now();
        return equivalences.save(e);
    }
    @Transactional
    public void deleteEquivalence(UUID id) {
        CourseEquivalence e = equivalences.findById(id).orElseThrow(() -> bad("Không tìm thấy môn tương đương"));
        e.deletedAt = LocalDateTime.now(); e.active = false; equivalences.save(e);
    }
    public List<Map<String,Object>> semesters() {
        return db.queryForList("SELECT id, code, name FROM dbo.semesters WHERE deleted_at IS NULL ORDER BY name");
    }
    public List<Map<String,Object>> classes() {
        return db.queryForList("SELECT id, class_code, course_id, semester_id FROM dbo.courses_classes WHERE deleted_at IS NULL ORDER BY class_code");
    }
    public List<Map<String,Object>> courses() {
        return db.queryForList("SELECT id, code, name, credits FROM dbo.courses WHERE deleted_at IS NULL ORDER BY name");
    }
    public List<Map<String,Object>> students() {
        return db.queryForList("SELECT id, code, full_name FROM dbo.students_ WHERE deleted_at IS NULL ORDER BY full_name");
    }
}
