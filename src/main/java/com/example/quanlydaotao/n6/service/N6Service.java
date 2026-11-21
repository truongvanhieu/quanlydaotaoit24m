package com.example.quanlydaotao.n6.service;

import com.example.quanlydaotao.n6.dto.EquivalenceRequest;
import com.example.quanlydaotao.n6.dto.PeriodRequest;
import com.example.quanlydaotao.n6.dto.RegisterRequest;
import com.example.quanlydaotao.n6.dto.RegistrationEligibilityResponse;
import com.example.quanlydaotao.n6.dto.RegistrationSummaryResponse;
import com.example.quanlydaotao.n6.entity.CourseEquivalence;
import com.example.quanlydaotao.n6.entity.CourseRegistration;
import com.example.quanlydaotao.n6.entity.RegistrationPeriod;
import com.example.quanlydaotao.n6.repository.CourseEquivalenceRepository;
import com.example.quanlydaotao.n6.repository.CourseRegistrationRepository;
import com.example.quanlydaotao.n6.repository.RegistrationPeriodRepository;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class N6Service {

    private static final short TYPE_NEW = 1;
    private static final short TYPE_RETAKE = 2;
    private static final short TYPE_IMPROVE = 3;
    private static final short STATUS_CANCELLED = 3;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final RegistrationPeriodRepository periods;
    private final CourseRegistrationRepository registrations;
    private final CourseEquivalenceRepository equivalences;
    private final JdbcTemplate db;

    public N6Service(
            RegistrationPeriodRepository periods,
            CourseRegistrationRepository registrations,
            CourseEquivalenceRepository equivalences,
            JdbcTemplate db
    ) {
        this.periods = periods;
        this.registrations = registrations;
        this.equivalences = equivalences;
        this.db = db;
    }

    // =====================================================
    // COMMON HELPERS
    // =====================================================
    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(BUSINESS_ZONE);
    }

    private UUID asUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        return UUID.fromString(value.toString());
    }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime time) return time;
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        throw new IllegalStateException("Không hỗ trợ kiểu ngày giờ từ SQL Server: " + value.getClass().getName());
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(value.toString());
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) return booleanValue;
        if (value instanceof Number number) return number.intValue() != 0;
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private boolean containsIgnoreCase(Object value, String kw) {
        return value != null && value.toString().toLowerCase(Locale.ROOT).contains(kw);
    }

    private boolean existsActive(String table, UUID id) {
        if (id == null) return false;
        String sql = switch (table) {
            case "semesters" -> "SELECT COUNT(*) FROM dbo.semesters WHERE id = ? AND deleted_at IS NULL AND (is_active IS NULL OR is_active = 1)";
            case "students_" -> "SELECT COUNT(*) FROM dbo.students_ WHERE id = ? AND deleted_at IS NULL AND (is_active IS NULL OR is_active = 1)";
            case "courses" -> "SELECT COUNT(*) FROM dbo.courses WHERE id = ? AND deleted_at IS NULL AND (is_active IS NULL OR is_active = 1)";
            default -> throw new IllegalArgumentException("Bảng chưa được cho phép: " + table);
        };
        Integer count = db.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    // =====================================================
    // REGISTRATION PERIODS
    // =====================================================
    public List<RegistrationPeriod> periods(UUID semesterId, Boolean active) {
        return periods.findByDeletedAtIsNull().stream()
                .filter(p -> semesterId == null || semesterId.equals(p.semesterId))
                .filter(p -> active == null || active.equals(p.active))
                .sorted(Comparator.comparing(p -> p.startTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<RegistrationPeriod> searchPeriods(String keyword, UUID semesterId, Boolean active) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (kw.isEmpty()) {
            return periods(semesterId, active);
        }
        Map<UUID, String> semMap = new HashMap<>();
        for (Map<String, Object> sem : semesters()) {
            UUID sid = asUuid(sem.get("id"));
            if (sid != null) {
                semMap.put(sid, (String.valueOf(sem.getOrDefault("code", "")) + " " + String.valueOf(sem.getOrDefault("name", ""))).toLowerCase(Locale.ROOT));
            }
        }
        return periods(semesterId, active).stream()
                .filter(p -> (p.name != null && p.name.toLowerCase(Locale.ROOT).contains(kw))
                        || (p.targetConfig != null && p.targetConfig.toLowerCase(Locale.ROOT).contains(kw))
                        || (p.semesterId != null && semMap.getOrDefault(p.semesterId, "").contains(kw)))
                .toList();
    }

    public RegistrationPeriod getPeriod(UUID id) {
        if (id == null) throw bad("Mã đợt đăng ký không hợp lệ");
        return periods.findById(id)
                .filter(p -> p.deletedAt == null)
                .orElseThrow(() -> notFound("Không tìm thấy đợt đăng ký"));
    }

    @Transactional
    public RegistrationPeriod savePeriod(UUID id, PeriodRequest input) {
        if (input == null || input.name() == null || input.name().isBlank()) {
            throw bad("Tên đợt đăng ký không được để trống");
        }
        if (input.name().trim().length() > 200) {
            throw bad("Tên đợt đăng ký tối đa 200 ký tự");
        }
        if (input.semesterId() == null || input.startTime() == null || input.endTime() == null) {
            throw bad("Cần cung cấp học kỳ, thời gian bắt đầu và thời gian kết thúc");
        }
        if (!input.endTime().isAfter(input.startTime())) {
            throw bad("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (!existsActive("semesters", input.semesterId())) {
            throw bad("Học kỳ không tồn tại hoặc đã ngừng hoạt động");
        }

        int minCredits = input.minCredits() == null ? 12 : input.minCredits();
        int maxCredits = input.maxCredits() == null ? 25 : input.maxCredits();
        if (minCredits < 0 || maxCredits < 1 || minCredits > 255 || maxCredits > 255 || maxCredits < minCredits) {
            throw bad("Giới hạn tín chỉ không hợp lệ (0 <= tối thiểu <= tối đa <= 255)");
        }

        String targetConfig = input.targetConfig();
        if (targetConfig != null && !targetConfig.isBlank()) {
            Integer jsonObject = db.queryForObject(
                    "SELECT CASE WHEN ISJSON(?) = 1 AND LEFT(LTRIM(?), 1) = '{' THEN 1 ELSE 0 END",
                    Integer.class, targetConfig, targetConfig);
            if (jsonObject == null || jsonObject != 1) {
                throw bad("targetConfig phải là JSON object hợp lệ");
            }
        }

        RegistrationPeriod period;
        if (id == null) {
            period = new RegistrationPeriod();
            period.active = input.active() == null || input.active();
            period.allowRetake = Boolean.TRUE.equals(input.allowRetake());
        } else {
            period = getPeriod(id);
            if (input.active() != null) period.active = input.active();
            if (input.allowRetake() != null) period.allowRetake = input.allowRetake();
        }

        period.name = input.name().trim();
        period.semesterId = input.semesterId();
        period.startTime = input.startTime();
        period.endTime = input.endTime();
        period.targetConfig = targetConfig;
        period.minCredits = (short) minCredits;
        period.maxCredits = (short) maxCredits;
        period.updatedAt = now();

        return periods.save(period);
    }

    @Transactional
    public void deletePeriod(UUID id) {
        RegistrationPeriod period = getPeriod(id);
        Integer activeCount = db.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.course_registrations
                WHERE registration_period_id = ?
                  AND deleted_at IS NULL
                  AND status IN (1, 2)
                """, Integer.class, id);
        if (activeCount != null && activeCount > 0) {
            throw conflict("Không thể xóa đợt có đăng ký học phần đang hoạt động; hãy đóng đợt hoặc xử lý đăng ký trước");
        }
        period.deletedAt = now();
        period.active = false;
        period.updatedAt = now();
        periods.save(period);
    }

    // =====================================================
    // COURSE REGISTRATIONS
    // =====================================================
    public List<CourseRegistration> registrations(UUID studentId, UUID periodId, UUID courseClassId, Short status) {
        return registrations.findByDeletedAtIsNull().stream()
                .filter(r -> studentId == null || studentId.equals(r.studentId))
                .filter(r -> periodId == null || periodId.equals(r.registrationPeriodId))
                .filter(r -> courseClassId == null || courseClassId.equals(r.courseClassId))
                .filter(r -> status == null || status.equals(r.status))
                .sorted(Comparator.comparing(r -> r.registeredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List< Map< String, Object > > registrationDetails(UUID studentId, UUID periodId, UUID courseClassId, Short status) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    r.id AS id,
                    r.id AS registrationId,
                    r.student_id AS studentId,
                    s.code AS studentCode,
                    s.full_name AS studentName,
                    r.registration_period_id AS registrationPeriodId,
                    rp.name AS periodName,
                    rp.name AS registrationPeriodName,
                    rp.semester_id AS semesterId,
                    sem.code AS semesterCode,
                    sem.name AS semesterName,
                    r.course_class_id AS courseClassId,
                    cc.class_code AS classCode,
                    cc.class_code AS courseClassCode,
                    c.id AS courseId,
                    c.code AS courseCode,
                    c.name AS courseName,
                    c.credits AS credits,
                    r.registration_type AS registrationType,
                    CASE r.registration_type
                        WHEN 1 THEN N'Học mới'
                        WHEN 2 THEN N'Học lại'
                        WHEN 3 THEN N'Cải thiện'
                        ELSE N'Không xác định'
                    END AS registrationTypeName,
                    r.status AS status,
                    CASE r.status
                        WHEN 1 THEN N'Chờ xử lý'
                        WHEN 2 THEN N'Đã đăng ký'
                        WHEN 3 THEN N'Đã hủy'
                        ELSE N'Không xác định'
                    END AS statusName,
                    r.registered_at AS registeredAt,
                    r.is_paid AS paid,
                    r.replaced_grade_id AS replacedGradeId,
                    r.updated_at AS updatedAt
                FROM dbo.course_registrations r
                INNER JOIN dbo.students_ s ON s.id = r.student_id
                INNER JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
                INNER JOIN dbo.courses c ON c.id = cc.course_id
                INNER JOIN dbo.registration_periods rp ON rp.id = r.registration_period_id
                LEFT JOIN dbo.semesters sem ON sem.id = rp.semester_id
                WHERE r.deleted_at IS NULL
                  AND s.deleted_at IS NULL
                  AND cc.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND rp.deleted_at IS NULL
                """);

        List<Object> params = new ArrayList<>();
        if (studentId != null) {
            sql.append(" AND r.student_id = ?");
            params.add(studentId);
        }
        if (periodId != null) {
            sql.append(" AND r.registration_period_id = ?");
            params.add(periodId);
        }
        if (courseClassId != null) {
            sql.append(" AND r.course_class_id = ?");
            params.add(courseClassId);
        }
        if (status != null) {
            sql.append(" AND r.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY r.registered_at DESC, r.id DESC");
        return db.queryForList(sql.toString(), params.toArray());
    }

    public List< Map< String, Object > > searchRegistrationDetails(
            String keyword, UUID studentId, UUID periodId, UUID courseClassId, Short status) {
        List< Map< String, Object > > rows = registrationDetails(studentId, periodId, courseClassId, status);
        if (keyword == null || keyword.isBlank()) return rows;
        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        return rows.stream().filter(r ->
                containsIgnoreCase(r.get("studentCode"), kw)
                        || containsIgnoreCase(r.get("studentName"), kw)
                        || containsIgnoreCase(r.get("courseClassCode"), kw)
                        || containsIgnoreCase(r.get("courseCode"), kw)
                        || containsIgnoreCase(r.get("courseName"), kw)
                        || containsIgnoreCase(r.get("registrationPeriodName"), kw)
        ).toList();
    }

    public List< Map< String, Object > > studentCourseHistory(UUID studentId) {
        if (studentId == null) return List.of();
        return db.queryForList("""
                SELECT
                    c.id AS courseId,
                    c.code AS courseCode,
                    c.name AS courseName,
                    c.credits AS credits,
                    cc.class_code AS classCode,
                    cc.semester_id AS semesterId,
                    sem.code AS semesterCode,
                    sem.name AS semesterName,
                    r.registration_type AS registrationType,
                    r.status AS status,
                    r.registered_at AS registeredAt
                FROM dbo.course_registrations r
                INNER JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
                INNER JOIN dbo.courses c ON c.id = cc.course_id
                LEFT JOIN dbo.semesters sem ON sem.id = cc.semester_id
                WHERE r.student_id = ?
                  AND r.deleted_at IS NULL
                  AND r.status IN (1, 2)
                ORDER BY r.registered_at DESC
                """, studentId);
    }

    public CourseRegistration getRegistration(UUID id) {
        if (id == null) throw bad("Mã đăng ký không hợp lệ");
        return registrations.findById(id)
                .filter(r -> r.deletedAt == null)
                .orElseThrow(() -> notFound("Không tìm thấy đăng ký học phần"));
    }

    @Transactional
    public CourseRegistration register(RegisterRequest input) {
        RegistrationCheck check = validateRegistration(input, true);
        CourseRegistration registration = new CourseRegistration();
        registration.studentId = input.studentId();
        registration.courseClassId = input.courseClassId();
        registration.registrationPeriodId = input.registrationPeriodId();
        registration.registrationType = input.registrationType();
        registration.replacedGradeId = check.replacedGradeId();
        registration.status = (short) 2;
        registration.paid = false;
        registration.updatedAt = null;
        return registrations.save(registration);
    }

    public RegistrationEligibilityResponse eligibility(RegisterRequest input) {
        try {
            RegistrationCheck check = validateRegistration(input, false);
            return new RegistrationEligibilityResponse(
                    true,
                    "Sinh viên đủ điều kiện đăng ký",
                    check.replacedGradeId(),
                    check.courseCredits(),
                    check.totalCreditsAfter(),
                    check.minCredits(),
                    check.maxCredits()
            );
        } catch (ResponseStatusException exception) {
            return new RegistrationEligibilityResponse(
                    false,
                    exception.getReason() == null ? "Không đủ điều kiện đăng ký" : exception.getReason(),
                    null, null, null, null, null
            );
        }
    }

    private RegistrationCheck validateRegistration(RegisterRequest input, boolean lockPeriod) {
        if (input == null || input.studentId() == null || input.courseClassId() == null
                || input.registrationPeriodId() == null || input.registrationType() == null) {
            throw bad("Cần cung cấp studentId, courseClassId, registrationPeriodId và registrationType");
        }
        if (input.registrationType() != TYPE_NEW && input.registrationType() != TYPE_RETAKE
                && input.registrationType() != TYPE_IMPROVE) {
            throw bad("registrationType phải là 1 (học mới), 2 (học lại) hoặc 3 (cải thiện)");
        }
        if (!existsActive("students_", input.studentId())) {
            throw bad("Sinh viên không tồn tại, đã bị xóa hoặc không hoạt động");
        }

        PeriodSnapshot period = loadPeriodSnapshot(input.registrationPeriodId(), lockPeriod);
        LocalDateTime currentTime = now();
        if (!period.active() || period.startTime() == null || period.endTime() == null
                || currentTime.isBefore(period.startTime()) || !currentTime.isBefore(period.endTime())) {
            throw conflict("Đợt đăng ký chưa mở, đã đóng hoặc đang bị vô hiệu hóa");
        }
        if (input.registrationType() != TYPE_NEW && !period.allowRetake()) {
            throw conflict("Đợt đăng ký này không cho phép học lại/cải thiện");
        }
        if (!matchesTargetConfig(period.targetConfig(), input.studentId())) {
            throw conflict("Sinh viên không thuộc đối tượng được phép đăng ký trong đợt này");
        }

        Map<String, Object> courseClass = loadCourseClass(input.courseClassId());
        UUID classSemesterId = asUuid(courseClass.get("semester_id"));
        UUID courseId = asUuid(courseClass.get("course_id"));
        if (classSemesterId == null || courseId == null) {
            throw bad("Lớp học phần thiếu thông tin môn học hoặc học kỳ");
        }
        if (!period.semesterId().equals(classSemesterId)) {
            throw bad("Lớp học phần không thuộc học kỳ của đợt đăng ký");
        }
        if (hasActiveRegistrationForClass(input.studentId(), input.courseClassId())) {
            throw conflict("Sinh viên đã đăng ký lớp học phần này");
        }
        if (hasEquivalentCourseInSemester(input.studentId(), courseId, classSemesterId)) {
            throw conflict("Sinh viên đã đăng ký môn học này hoặc môn tương đương trong cùng học kỳ");
        }

        UUID replacedGradeId = null;
        if (input.registrationType() == TYPE_NEW) {
            if (hasAnyActiveHistoryForCourse(input.studentId(), courseId)) {
                throw conflict("Sinh viên đã có lịch sử đăng ký môn học này hoặc môn tương đương. Hãy chọn Học lại hoặc Cải thiện nếu phù hợp.");
            }
            checkPrerequisites(input.studentId(), courseId, classSemesterId);
        } else {
            if (!hasPreviousRegistrationForCourse(input.studentId(), courseId, classSemesterId)) {
                throw conflict("Sinh viên chưa có lịch sử đăng ký môn học này ở học kỳ trước; không thể xác nhận điều kiện học lại/cải thiện.");
            }
            replacedGradeId = null;
        }

        Object maxStudentsValue = courseClass.get("max_students");
        if (maxStudentsValue instanceof Number maxStudents && maxStudents.intValue() > 0) {
            Integer registeredCount = db.queryForObject("""
                    SELECT COUNT(*)
                    FROM dbo.course_registrations
                    WHERE course_class_id = ?
                      AND deleted_at IS NULL
                      AND status IN (1, 2)
                    """, Integer.class, input.courseClassId());
            if (registeredCount != null && registeredCount >= maxStudents.intValue()) {
                throw conflict("Lớp học phần đã đủ số lượng sinh viên");
            }
        }

        if (hasScheduleConflict(input.studentId(), input.courseClassId())) {
            throw conflict("Lịch học của lớp bị trùng với một lớp sinh viên đã đăng ký trong cùng học kỳ");
        }

        BigDecimal courseCredits = asBigDecimal(courseClass.get("credits"));
        BigDecimal selectedCredits = selectedCredits(input.studentId(), input.registrationPeriodId());
        BigDecimal totalAfter = selectedCredits.add(courseCredits);
        if (totalAfter.compareTo(BigDecimal.valueOf(period.maxCredits())) > 0) {
            throw conflict("Đăng ký này làm vượt giới hạn tối đa " + period.maxCredits() + " tín chỉ của đợt");
        }

        return new RegistrationCheck(replacedGradeId, courseCredits, totalAfter, period.minCredits(), period.maxCredits());
    }

    private PeriodSnapshot loadPeriodSnapshot(UUID periodId, boolean lock) {
        String hint = lock ? " WITH (UPDLOCK, HOLDLOCK)" : "";
        List< Map< String, Object > > rows = db.queryForList("""
                SELECT id, semester_id, start_time, end_time, allow_retake,
                       is_active, min_credits, max_credits, target_config
                FROM dbo.registration_periods
                """ + hint + " WHERE id = ? AND deleted_at IS NULL", periodId);
        if (rows.isEmpty()) throw notFound("Đợt đăng ký không tồn tại");
        Map<String, Object> row = rows.get(0);
        return new PeriodSnapshot(
                asUuid(row.get("id")),
                asUuid(row.get("semester_id")),
                asLocalDateTime(row.get("start_time")),
                asLocalDateTime(row.get("end_time")),
                asBoolean(row.get("allow_retake")),
                asBoolean(row.get("is_active")),
                row.get("min_credits") == null ? 12 : ((Number) row.get("min_credits")).intValue(),
                row.get("max_credits") == null ? 25 : ((Number) row.get("max_credits")).intValue(),
                (String) row.get("target_config")
        );
    }

    private Map<String, Object> loadCourseClass(UUID courseClassId) {
        List< Map< String, Object > > rows = db.queryForList("""
                SELECT cc.id, cc.course_id, cc.semester_id, cc.max_students, c.credits
                FROM dbo.courses_classes cc
                INNER JOIN dbo.courses c ON c.id = cc.course_id
                WHERE cc.id = ?
                  AND cc.deleted_at IS NULL
                  AND (cc.is_active IS NULL OR cc.is_active = 1)
                  AND c.deleted_at IS NULL
                  AND (c.is_active IS NULL OR c.is_active = 1)
                """, courseClassId);
        if (rows.isEmpty()) throw notFound("Lớp học phần hoặc môn học không tồn tại/đã ngừng hoạt động");
        return rows.get(0);
    }

    private boolean matchesTargetConfig(String targetConfig, UUID studentId) {
        if (targetConfig == null || targetConfig.isBlank()) return true;
        Integer result = db.queryForObject("""
                WITH cfg AS (SELECT CAST(? AS NVARCHAR(MAX)) AS j)
                SELECT CASE
                  WHEN ISJSON(cfg.j) <> 1 THEN 0
                  WHEN EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.studentIds'))
                   AND NOT EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.studentIds') x WHERE TRY_CONVERT(uniqueidentifier, x.[value]) = s.id) THEN 0
                  WHEN EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.departmentIds'))
                   AND NOT EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.departmentIds') x WHERE TRY_CONVERT(uniqueidentifier, x.[value]) = s.department_id) THEN 0
                  WHEN EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.majorIds'))
                   AND NOT EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.majorIds') x WHERE TRY_CONVERT(uniqueidentifier, x.[value]) = s.major_id) THEN 0
                  WHEN EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.trainingProgramIds'))
                   AND NOT EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.trainingProgramIds') x WHERE TRY_CONVERT(uniqueidentifier, x.[value]) = s.training_program_id) THEN 0
                  WHEN EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.studentClassIds'))
                   AND NOT EXISTS (SELECT 1 FROM OPENJSON(cfg.j, '$.studentClassIds') x WHERE TRY_CONVERT(uniqueidentifier, x.[value]) = s.student_classess_id) THEN 0
                  ELSE 1
                END
                FROM dbo.students_ s
                CROSS JOIN cfg
                WHERE s.id = ? AND s.deleted_at IS NULL
                """, Integer.class, targetConfig, studentId);
        return result != null && result == 1;
    }

    private boolean hasActiveRegistrationForClass(UUID studentId, UUID courseClassId) {
        Integer count = db.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.course_registrations
                WHERE student_id = ? AND course_class_id = ? AND deleted_at IS NULL AND status IN (1, 2)
                """, Integer.class, studentId, courseClassId);
        return count != null && count > 0;
    }

    private boolean hasEquivalentCourseInSemester(UUID studentId, UUID courseId, UUID semesterId) {
        Integer count = db.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.course_registrations r
                INNER JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
                WHERE r.student_id = ?
                  AND r.deleted_at IS NULL
                  AND r.status IN (1, 2)
                  AND cc.deleted_at IS NULL
                  AND cc.semester_id = ?
                  AND (
                    cc.course_id = ?
                    OR EXISTS (
                        SELECT 1 FROM dbo.course_equivalences e
                        WHERE e.deleted_at IS NULL AND e.is_active = 1
                          AND (e.effect_date IS NULL OR e.effect_date <= CONVERT(date, GETDATE()))
                          AND ((e.original_course_id = ? AND e.equivalent_course_id = cc.course_id)
                            OR (e.equivalent_course_id = ? AND e.original_course_id = cc.course_id))
                    )
                  )
                """, Integer.class, studentId, semesterId, courseId, courseId, courseId);
        return count != null && count > 0;
    }

    private boolean hasAnyActiveHistoryForCourse(UUID studentId, UUID courseId) {
        Integer count = db.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.course_registrations r
                INNER JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
                WHERE r.student_id = ?
                  AND r.deleted_at IS NULL
                  AND r.status IN (1, 2)
                  AND cc.deleted_at IS NULL
                  AND (
                    cc.course_id = ?
                    OR EXISTS (
                        SELECT 1 FROM dbo.course_equivalences e
                        WHERE e.deleted_at IS NULL AND e.is_active = 1
                          AND (e.effect_date IS NULL OR e.effect_date <= CONVERT(date, GETDATE()))
                          AND ((e.original_course_id = ? AND e.equivalent_course_id = cc.course_id)
                            OR (e.equivalent_course_id = ? AND e.original_course_id = cc.course_id))
                    )
                  )
                """, Integer.class, studentId, courseId, courseId, courseId);
        return count != null && count > 0;
    }

    private boolean hasPreviousRegistrationForCourse(UUID studentId, UUID courseId, UUID currentSemesterId) {
        Integer count = db.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.course_registrations r
                INNER JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
                WHERE r.student_id = ?
                  AND r.deleted_at IS NULL
                  AND r.status IN (1, 2)
                  AND cc.deleted_at IS NULL
                  AND (cc.is_active IS NULL OR cc.is_active = 1)
                  AND cc.semester_id <> ?
                  AND (
                    cc.course_id = ?
                    OR EXISTS (
                        SELECT 1 FROM dbo.course_equivalences e
                        WHERE e.deleted_at IS NULL AND e.is_active = 1
                          AND (e.effect_date IS NULL OR e.effect_date <= CONVERT(date, GETDATE()))
                          AND ((e.original_course_id = ? AND e.equivalent_course_id = cc.course_id)
                            OR (e.equivalent_course_id = ? AND e.original_course_id = cc.course_id))
                    )
                  )
                """, Integer.class, studentId, currentSemesterId, courseId, courseId, courseId);
        return count != null && count > 0;
    }

    private void checkPrerequisites(UUID studentId, UUID courseId, UUID currentSemesterId) {
        List< Map< String, Object > > prerequisites = db.queryForList("""
                SELECT cp.prerequisite_course_id AS id, c.code AS code, c.name AS name
                FROM dbo.course_prerequisites cp
                LEFT JOIN dbo.courses c ON c.id = cp.prerequisite_course_id
                WHERE cp.course_id = ? AND cp.deleted_at IS NULL AND cp.is_active = 1
                """, courseId);

        for (Map<String, Object> pre : prerequisites) {
            UUID prerequisiteId = asUuid(pre.get("id"));
            if (prerequisiteId == null) continue;
            boolean hasHistory = hasPreviousRegistrationForCourse(studentId, prerequisiteId, currentSemesterId);
            if (!hasHistory) {
                String label = pre.get("code") != null || pre.get("name") != null
                        ? (String.valueOf(pre.getOrDefault("code", "")) + " - " + String.valueOf(pre.getOrDefault("name", ""))).trim()
                        : prerequisiteId.toString();
                throw conflict("Sinh viên chưa có lịch sử đăng ký môn tiên quyết (" + label + ").");
            }
        }
    }

    private boolean hasScheduleConflict(UUID studentId, UUID courseClassId) {
        Integer count = db.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.schedules targetSchedule
                INNER JOIN dbo.course_sections targetSection ON targetSection.id = targetSchedule.course_section_id
                INNER JOIN dbo.courses_classes targetClass
                    ON targetClass.id = ?
                   AND targetClass.class_code = targetSection.code
                   AND targetClass.course_id = targetSection.course_id
                   AND targetClass.semester_id = targetSection.semester_id
                INNER JOIN dbo.course_registrations r
                    ON r.student_id = ? AND r.deleted_at IS NULL AND r.status IN (1, 2)
                INNER JOIN dbo.courses_classes enrolledClass
                    ON enrolledClass.id = r.course_class_id
                   AND enrolledClass.semester_id = targetClass.semester_id
                   AND enrolledClass.id <> targetClass.id
                   AND enrolledClass.deleted_at IS NULL
                   AND (enrolledClass.is_active IS NULL OR enrolledClass.is_active = 1)
                INNER JOIN dbo.course_sections enrolledSection
                    ON enrolledSection.code = enrolledClass.class_code
                   AND enrolledSection.course_id = enrolledClass.course_id
                   AND enrolledSection.semester_id = enrolledClass.semester_id
                INNER JOIN dbo.schedules enrolledSchedule ON enrolledSchedule.course_section_id = enrolledSection.id
                WHERE targetSchedule.deleted_at IS NULL
                  AND (targetSchedule.is_active IS NULL OR targetSchedule.is_active = 1)
                  AND targetSection.deleted_at IS NULL
                  AND (targetSection.is_active IS NULL OR targetSection.is_active = 1)
                  AND enrolledSchedule.deleted_at IS NULL
                  AND (enrolledSchedule.is_active IS NULL OR enrolledSchedule.is_active = 1)
                  AND targetSchedule.day_of_week = enrolledSchedule.day_of_week
                  AND targetSchedule.start_period <= enrolledSchedule.end_period
                  AND enrolledSchedule.start_period <= targetSchedule.end_period
                  AND (targetSchedule.start_date IS NULL OR enrolledSchedule.end_date IS NULL OR targetSchedule.start_date <= enrolledSchedule.end_date)
                  AND (enrolledSchedule.start_date IS NULL OR targetSchedule.end_date IS NULL OR enrolledSchedule.start_date <= targetSchedule.end_date)
                """, Integer.class, courseClassId, studentId);
        return count != null && count > 0;
    }

    private BigDecimal selectedCredits(UUID studentId, UUID periodId) {
        BigDecimal credits = db.queryForObject("""
                SELECT COALESCE(SUM(COALESCE(c.credits, 0)), 0)
                FROM dbo.course_registrations r
                INNER JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
                INNER JOIN dbo.courses c ON c.id = cc.course_id
                WHERE r.student_id = ? AND r.registration_period_id = ?
                  AND r.deleted_at IS NULL AND r.status IN (1, 2)
                """, BigDecimal.class, studentId, periodId);
        return credits == null ? BigDecimal.ZERO : credits;
    }

    public RegistrationSummaryResponse registrationSummary(UUID studentId, UUID periodId) {
        if (studentId == null || periodId == null) throw bad("Cần cung cấp studentId và registrationPeriodId");
        if (!existsActive("students_", studentId)) throw notFound("Không tìm thấy sinh viên đang hoạt động");
        RegistrationPeriod period = getPeriod(periodId);

        Map<String, Object> values = db.queryForMap("""
                SELECT COUNT(r.id) AS registration_count,
                       COALESCE(SUM(COALESCE(c.credits, 0)), 0) AS selected_credits
                FROM dbo.course_registrations r
                INNER JOIN dbo.courses_classes cc ON cc.id = r.course_class_id
                INNER JOIN dbo.courses c ON c.id = cc.course_id
                WHERE r.student_id = ? AND r.registration_period_id = ?
                  AND r.deleted_at IS NULL AND r.status IN (1, 2)
                """, studentId, periodId);

        int count = ((Number) values.get("registration_count")).intValue();
        BigDecimal selected = asBigDecimal(values.get("selected_credits"));
        int min = period.minCredits == null ? 12 : period.minCredits;
        int max = period.maxCredits == null ? 25 : period.maxCredits;
        BigDecimal remaining = BigDecimal.valueOf(max).subtract(selected);

        return new RegistrationSummaryResponse(
                studentId, periodId, count, selected, min, max, remaining,
                selected.compareTo(BigDecimal.valueOf(min)) >= 0,
                selected.compareTo(BigDecimal.valueOf(max)) > 0
        );
    }

    @Transactional
    public CourseRegistration cancel(UUID id) {
        CourseRegistration registration = getRegistration(id);
        if (registration.status != null && registration.status == STATUS_CANCELLED) return registration;
        registration.status = STATUS_CANCELLED;
        registration.updatedAt = now();
        return registrations.save(registration);
    }

    // =====================================================
    // COURSE EQUIVALENCES
    // =====================================================
    public List<CourseEquivalence> equivalences(UUID courseId, Boolean active) {
        return equivalences.findByDeletedAtIsNull().stream()
                .filter(e -> courseId == null || courseId.equals(e.originalCourseId) || courseId.equals(e.equivalentCourseId))
                .filter(e -> active == null || e.active == active)
                .sorted(Comparator.comparing(e -> e.createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List< Map< String, Object > > searchEquivalenceDetails(String keyword, UUID courseId, Boolean active) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    e.id AS id,
                    e.original_course_id AS originalCourseId,
                    c1.code AS originalCourseCode,
                    c1.name AS originalCourseName,
                    c1.credits AS originalCredits,
                    e.equivalent_course_id AS equivalentCourseId,
                    c2.code AS equivalentCourseCode,
                    c2.name AS equivalentCourseName,
                    c2.credits AS equivalentCredits,
                    e.equivalence_type AS equivalenceType,
                    e.effect_date AS effectDate,
                    e.is_active AS active,
                    e.note AS note,
                    e.created_at AS createdAt
                FROM dbo.course_equivalences e
                LEFT JOIN dbo.courses c1 ON c1.id = e.original_course_id
                LEFT JOIN dbo.courses c2 ON c2.id = e.equivalent_course_id
                WHERE e.deleted_at IS NULL
                """);
        List<Object> params = new ArrayList<>();
        if (courseId != null) {
            sql.append(" AND (e.original_course_id = ? OR e.equivalent_course_id = ?)");
            params.add(courseId);
            params.add(courseId);
        }
        if (active != null) {
            sql.append(" AND e.is_active = ?");
            params.add(active ? 1 : 0);
        }
        sql.append(" ORDER BY e.created_at DESC");

        List< Map< String, Object > > rows = db.queryForList(sql.toString(), params.toArray());
        if (keyword == null || keyword.isBlank()) return rows;
        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        return rows.stream().filter(r ->
                containsIgnoreCase(r.get("originalCourseCode"), kw)
                        || containsIgnoreCase(r.get("originalCourseName"), kw)
                        || containsIgnoreCase(r.get("equivalentCourseCode"), kw)
                        || containsIgnoreCase(r.get("equivalentCourseName"), kw)
                        || containsIgnoreCase(r.get("note"), kw)
        ).toList();
    }

    public CourseEquivalence getEquivalence(UUID id) {
        if (id == null) throw bad("Mã môn tương đương không hợp lệ");
        return equivalences.findById(id)
                .filter(e -> e.deletedAt == null)
                .orElseThrow(() -> notFound("Không tìm thấy môn tương đương"));
    }

    @Transactional
    public CourseEquivalence saveEquivalence(UUID id, EquivalenceRequest input) {
        if (input == null || input.originalCourseId() == null || input.equivalentCourseId() == null) {
            throw bad("Cần cung cấp originalCourseId và equivalentCourseId");
        }
        if (input.originalCourseId().equals(input.equivalentCourseId())) {
            throw bad("Một môn học không thể tương đương với chính nó");
        }
        if (!existsActive("courses", input.originalCourseId()) || !existsActive("courses", input.equivalentCourseId())) {
            throw bad("Môn học không tồn tại hoặc đã ngừng hoạt động");
        }
        if (input.equivalenceType() != null && input.equivalenceType() != 1 && input.equivalenceType() != 2) {
            throw bad("equivalenceType phải là 1 (thay thế hoàn toàn) hoặc 2 (tương đương song song)");
        }

        CourseEquivalence equivalence = id == null ? new CourseEquivalence() : getEquivalence(id);
        boolean active = input.active() != null ? input.active() : (id == null || equivalence.active);

        if (active) {
            boolean duplicate = equivalences.findByDeletedAtIsNull().stream()
                    .filter(existing -> id == null || !id.equals(existing.id))
                    .filter(existing -> existing.active)
                    .anyMatch(existing ->
                            (input.originalCourseId().equals(existing.originalCourseId) && input.equivalentCourseId().equals(existing.equivalentCourseId))
                            || (input.originalCourseId().equals(existing.equivalentCourseId) && input.equivalentCourseId().equals(existing.originalCourseId)));
            if (duplicate) {
                throw conflict("Quan hệ tương đương giữa hai môn này đã tồn tại");
            }
        }

        equivalence.originalCourseId = input.originalCourseId();
        equivalence.equivalentCourseId = input.equivalentCourseId();
        equivalence.equivalenceType = input.equivalenceType() == null ? 1 : input.equivalenceType();
        equivalence.effectDate = input.effectDate();
        equivalence.active = active;
        equivalence.note = input.note();
        equivalence.updatedAt = now();
        return equivalences.save(equivalence);
    }

    @Transactional
    public void deleteEquivalence(UUID id) {
        CourseEquivalence equivalence = getEquivalence(id);
        equivalence.deletedAt = now();
        equivalence.active = false;
        equivalence.updatedAt = now();
        equivalences.save(equivalence);
    }

    // =====================================================
    // SHARED LOOKUP APIs & CLASS STUDENT ROSTER
    // =====================================================
    public List< Map< String, Object > > semesters() {
        return db.queryForList("""
                SELECT id, code, name
                FROM dbo.semesters
                WHERE deleted_at IS NULL AND (is_active IS NULL OR is_active = 1)
                ORDER BY name
                """);
    }

    public List< Map< String, Object > > classes(UUID semesterId) {
        String sql = """
                SELECT cc.id, cc.class_code AS code, cc.course_id AS courseId,
                       cc.semester_id AS semesterId, cc.max_students AS maxStudents,
                       c.code AS courseCode, c.name AS courseName, c.credits AS credits
                FROM dbo.courses_classes cc
                INNER JOIN dbo.courses c ON c.id = cc.course_id
                WHERE cc.deleted_at IS NULL AND (cc.is_active IS NULL OR cc.is_active = 1)
                  AND c.deleted_at IS NULL AND (c.is_active IS NULL OR c.is_active = 1)
                """ + (semesterId == null ? "" : " AND cc.semester_id = ? ") + " ORDER BY cc.class_code";
        return semesterId == null ? db.queryForList(sql) : db.queryForList(sql, semesterId);
    }

    public List< Map< String, Object > > courseClassesWithSummary(UUID semesterId, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    cc.id AS id,
                    cc.class_code AS code,
                    cc.course_id AS courseId,
                    c.code AS courseCode,
                    c.name AS courseName,
                    c.credits AS credits,
                    cc.semester_id AS semesterId,
                    sem.code AS semesterCode,
                    sem.name AS semesterName,
                    cc.max_students AS maxStudents,
                    COUNT(CASE WHEN r.deleted_at IS NULL AND r.status IN (1, 2) THEN 1 END) AS registeredCount
                FROM dbo.courses_classes cc
                INNER JOIN dbo.courses c ON c.id = cc.course_id
                LEFT JOIN dbo.semesters sem ON sem.id = cc.semester_id
                LEFT JOIN dbo.course_registrations r ON r.course_class_id = cc.id
                WHERE cc.deleted_at IS NULL
                  AND (cc.is_active IS NULL OR cc.is_active = 1)
                  AND c.deleted_at IS NULL
                  AND (c.is_active IS NULL OR c.is_active = 1)
                """);
        List<Object> params = new ArrayList<>();
        if (semesterId != null) {
            sql.append(" AND cc.semester_id = ?");
            params.add(semesterId);
        }
        sql.append(" GROUP BY cc.id, cc.class_code, cc.course_id, c.code, c.name, c.credits, cc.semester_id, sem.code, sem.name, cc.max_students");
        sql.append(" ORDER BY cc.class_code");

        List< Map< String, Object > > rows = db.queryForList(sql.toString(), params.toArray());
        if (keyword == null || keyword.isBlank()) return rows;
        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        return rows.stream().filter(r ->
                containsIgnoreCase(r.get("code"), kw)
                        || containsIgnoreCase(r.get("courseCode"), kw)
                        || containsIgnoreCase(r.get("courseName"), kw)
                        || containsIgnoreCase(r.get("semesterName"), kw)
        ).toList();
    }

    public List< Map< String, Object > > studentsInCourseClass(UUID courseClassId, String keyword) {
        if (courseClassId == null) throw bad("Mã lớp học phần không hợp lệ");
        List< Map< String, Object > > rows = db.queryForList("""
                SELECT
                    r.id AS registrationId,
                    s.id AS studentId,
                    s.code AS studentCode,
                    s.full_name AS studentName,
                    sc.code AS studentClassCode,
                    sc.name AS studentClassName,
                    r.registration_type AS registrationType,
                    CASE r.registration_type
                        WHEN 1 THEN N'Học mới'
                        WHEN 2 THEN N'Học lại'
                        WHEN 3 THEN N'Cải thiện'
                        ELSE N'Không xác định'
                    END AS registrationTypeName,
                    r.status AS status,
                    CASE r.status
                        WHEN 1 THEN N'Chờ xử lý'
                        WHEN 2 THEN N'Đã đăng ký'
                        WHEN 3 THEN N'Đã hủy'
                        ELSE N'Không xác định'
                    END AS statusName,
                    r.is_paid AS paid,
                    r.registered_at AS registeredAt
                FROM dbo.course_registrations r
                INNER JOIN dbo.students_ s ON s.id = r.student_id
                LEFT JOIN dbo.student_classes sc ON sc.id = s.student_classess_id
                WHERE r.course_class_id = ?
                  AND r.deleted_at IS NULL
                  AND r.status IN (1, 2)
                  AND s.deleted_at IS NULL
                ORDER BY s.code, s.full_name
                """, courseClassId);

        if (keyword == null || keyword.isBlank()) return rows;
        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        return rows.stream().filter(r ->
                containsIgnoreCase(r.get("studentCode"), kw)
                        || containsIgnoreCase(r.get("studentName"), kw)
                        || containsIgnoreCase(r.get("studentClassCode"), kw)
                        || containsIgnoreCase(r.get("studentClassName"), kw)
        ).toList();
    }

    public List< Map< String, Object > > courses() {
        return db.queryForList("""
                SELECT id, code, name, credits
                FROM dbo.courses
                WHERE deleted_at IS NULL AND (is_active IS NULL OR is_active = 1)
                ORDER BY name
                """);
    }

    public List< Map< String, Object > > students() {
        return db.queryForList("""
                SELECT id, code, full_name AS fullName
                FROM dbo.students_
                WHERE deleted_at IS NULL AND (is_active IS NULL OR is_active = 1)
                ORDER BY full_name
                """);
    }

    private record PeriodSnapshot(
            UUID id, UUID semesterId, LocalDateTime startTime, LocalDateTime endTime,
            boolean allowRetake, boolean active, int minCredits, int maxCredits, String targetConfig
    ) {}

    private record RegistrationCheck(
            UUID replacedGradeId, BigDecimal courseCredits, BigDecimal totalCreditsAfter,
            int minCredits, int maxCredits
    ) {}
}