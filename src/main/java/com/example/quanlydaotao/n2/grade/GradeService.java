package com.example.quanlydaotao.n2.grade;

import static com.example.quanlydaotao.n2.grade.GradeModels.*;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n2.grade.GradeModels.CourseClassOption;
import com.example.quanlydaotao.n2.grade.GradeModels.GradeBookView;
import com.example.quanlydaotao.n2.grade.GradeModels.GradeComponentRequest;
import com.example.quanlydaotao.n2.grade.GradeModels.GradeComponentView;
import com.example.quanlydaotao.n2.grade.GradeModels.GradeEntryRequest;
import com.example.quanlydaotao.n2.grade.GradeModels.GradeEntryUpdateRequest;
import com.example.quanlydaotao.n2.grade.GradeModels.GradeEntryView;
import com.example.quanlydaotao.n2.grade.GradeModels.StudentGradeView;

@Service
public class GradeService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal TEN = new BigDecimal("10");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public List<GradeComponentView> getComponents(UUID courseClassId) {
        requireCourseClass(courseClassId);
        return findComponents(courseClassId);
    }

    @Transactional
    public GradeComponentView createComponent(GradeComponentRequest request) {
        validateComponent(request);
        requireCourseClass(request.courseClassId());
        ensureUniqueComponentCode(request.courseClassId(), request.componentCode(), null);

        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO grade_components
                    (id, course_class_id, component_code, component_name, weight_percentage,
                     min_score, max_score, is_required, input_order, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP)
                """,
                id,
                request.courseClassId(),
                request.componentCode().trim(),
                request.componentName().trim(),
                request.weightPercentage(),
                request.minScore(),
                request.maxScore(),
                Boolean.TRUE.equals(request.required()),
                request.inputOrder());
        refreshCourseSummaries(request.courseClassId());
        return findComponent(id);
    }

    @Transactional
    public GradeComponentView updateComponent(UUID id, GradeComponentRequest request) {
        validateComponent(request);
        GradeComponentView current = findComponent(id);
        if (!current.courseClassId().equals(request.courseClassId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Không thể chuyển thành phần điểm sang lớp học phần khác");
        }
        ensureUniqueComponentCode(request.courseClassId(), request.componentCode(), id);

        jdbcTemplate.update("""
                UPDATE grade_components
                SET component_code = ?, component_name = ?, weight_percentage = ?,
                    min_score = ?, max_score = ?, is_required = ?, input_order = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND is_active = 1 AND deleted_at IS NULL
                """,
                request.componentCode().trim(),
                request.componentName().trim(),
                request.weightPercentage(),
                request.minScore(),
                request.maxScore(),
                Boolean.TRUE.equals(request.required()),
                request.inputOrder(),
                id);
        refreshCourseSummaries(request.courseClassId());
        return findComponent(id);
    }

    @Transactional
    public void deleteComponent(UUID id) {
        GradeComponentView component = findComponent(id);
        jdbcTemplate.update("""
                UPDATE grade_components
                SET is_active = 0, deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND is_active = 1 AND deleted_at IS NULL
                """, id);
        refreshCourseSummaries(component.courseClassId());
    }

    public GradeBookView getGradeBook(UUID courseClassId) {
        requireCourseClass(courseClassId);
        List<GradeComponentView> components = findComponents(courseClassId);
        BigDecimal configuredWeight = GradeCalculator.totalWeight(components);
        boolean ready = !components.isEmpty() && configuredWeight.compareTo(ONE_HUNDRED) == 0;
        String status = components.isEmpty()
                ? "Lớp học phần chưa có thành phần điểm."
                : ready ? "Đã cấu hình đủ trọng số 100%."
                        : "Tổng trọng số đang là " + configuredWeight.stripTrailingZeros().toPlainString()
                                + "%; cần bằng 100% để tính điểm tổng kết.";

        List<RegistrationRow> registrations = jdbcTemplate.query("""
                SELECT cr.id AS registration_id, s.code AS student_code, s.full_name AS student_name, st.date_of_birth
                FROM course_registrations cr
                JOIN students_ s ON s.id = cr.student_id
                LEFT JOIN students st ON st.id = s.id
                WHERE cr.course_class_id = ? AND cr.deleted_at IS NULL
                  AND (s.is_active IS NULL OR s.is_active = 1)
                ORDER BY COALESCE(st.stt, 9999), s.code
                """,
                (rs, rowNum) -> new RegistrationRow(
                        uuid(rs, "registration_id"),
                        rs.getString("student_code"),
                        rs.getString("student_name"),
                        rs.getString("date_of_birth")),
                courseClassId);

        Map<UUID, Map<UUID, GradeEntryView>> entries = loadEntries(courseClassId);
        List<StudentGradeView> students = new ArrayList<>();
        for (RegistrationRow registration : registrations) {
            Map<UUID, GradeEntryView> studentEntries = entries.getOrDefault(registration.id(), Map.of());
            Map<UUID, BigDecimal> scores = new HashMap<>();
            List<GradeEntryView> gradeViews = new ArrayList<>();
            for (GradeComponentView component : components) {
                GradeEntryView entry = studentEntries.get(component.id());
                if (entry == null) {
                    gradeViews.add(new GradeEntryView(null, component.id(), null, false, null));
                } else {
                    gradeViews.add(entry);
                    if (entry.score() != null) {
                        scores.put(component.id(), entry.score());
                    }
                }
            }

            Optional<BigDecimal> total = ready
                    ? GradeCalculator.calculate(components, scores)
                    : Optional.empty();
            ScaleResult scale = total.map(this::findScale).orElse(null);
            students.add(new StudentGradeView(
                    registration.id(),
                    registration.studentCode(),
                    registration.studentName(),
                    registration.dateOfBirth(),
                    gradeViews,
                    total.orElse(null),
                    scale == null ? null : scale.letterGrade(),
                    scale == null ? null : scale.gpaValue(),
                    scale == null ? null : scale.isPass() ? "Đạt" : "Không đạt"));
        }

        return new GradeBookView(courseClassId, components, students, configuredWeight, ready, status);
    }

    @Transactional
    public GradeEntryView createEntry(GradeEntryRequest request) {
        validateEntry(request.componentId(), request.score());
        GradeComponentView component = findComponent(request.componentId());
        requireRegistrationInCourseClass(request.registrationId(), component.courseClassId());
        if (jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM student_component_grades
                WHERE registration_id = ? AND component_id = ? AND is_active = 1 AND deleted_at IS NULL
                """, Integer.class, request.registrationId(), request.componentId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Điểm thành phần này đã tồn tại");
        }

        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO student_component_grades
                    (id, registration_id, component_id, score, is_retake, is_locked,
                     graded_at, note, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, 0, CURRENT_TIMESTAMP, ?, 1, CURRENT_TIMESTAMP)
                """,
                id,
                request.registrationId(),
                request.componentId(),
                request.score(),
                Boolean.TRUE.equals(request.retake()),
                normalizeNote(request.note()));
        refreshSummary(request.registrationId());
        return findEntry(id);
    }

    @Transactional
    public GradeEntryView updateEntry(UUID id, GradeEntryUpdateRequest request) {
        validateEntryScore(request.score());
        GradeEntryView existing = findEntry(id);
        ensureNotLocked(id);
        GradeComponentView component = findComponent(existing.componentId());
        validateScoreRange(component, request.score());
        jdbcTemplate.update("""
                UPDATE student_component_grades
                SET score = ?, is_retake = ?, note = ?, graded_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND is_active = 1 AND deleted_at IS NULL
                """,
                request.score(),
                Boolean.TRUE.equals(request.retake()),
                normalizeNote(request.note()),
                id);
        refreshSummaryForEntry(id);
        return findEntry(id);
    }

    @Transactional
    public void deleteEntry(UUID id) {
        findEntry(id);
        ensureNotLocked(id);
        UUID registrationId = jdbcTemplate.queryForObject(
                "SELECT registration_id FROM student_component_grades WHERE id = ?",
                UUID.class,
                id);
        jdbcTemplate.update("""
                UPDATE student_component_grades
                SET is_active = 0, deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND is_active = 1 AND deleted_at IS NULL
                """, id);
        refreshSummary(registrationId);
    }

    private List<GradeComponentView> findComponents(UUID courseClassId) {
        return jdbcTemplate.query("""
                SELECT id, course_class_id, component_code, component_name, weight_percentage,
                       min_score, max_score, is_required, input_order
                FROM grade_components
                WHERE course_class_id = ? AND is_active = 1 AND deleted_at IS NULL
                ORDER BY COALESCE(input_order, 2147483647), component_name
                """, COMPONENT_MAPPER, courseClassId);
    }

    private GradeComponentView findComponent(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, course_class_id, component_code, component_name, weight_percentage,
                       min_score, max_score, is_required, input_order
                FROM grade_components
                WHERE id = ? AND is_active = 1 AND deleted_at IS NULL
                """, COMPONENT_MAPPER, id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thành phần điểm"));
    }

    private GradeEntryView findEntry(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, component_id, score, is_retake, note
                FROM student_component_grades
                WHERE id = ? AND is_active = 1 AND deleted_at IS NULL
                """, (rs, rowNum) -> new GradeEntryView(
                uuid(rs, "id"),
                uuid(rs, "component_id"),
                rs.getBigDecimal("score"),
                rs.getBoolean("is_retake"),
                rs.getString("note")), id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy điểm thành phần"));
    }

    private Map<UUID, Map<UUID, GradeEntryView>> loadEntries(UUID courseClassId) {
        List<EntryRow> rows = jdbcTemplate.query("""
                SELECT g.id, g.registration_id, g.component_id, g.score, g.is_retake, g.note
                FROM student_component_grades g
                JOIN course_registrations cr ON cr.id = g.registration_id
                WHERE cr.course_class_id = ? AND cr.deleted_at IS NULL
                  AND g.is_active = 1 AND g.deleted_at IS NULL
                """, (rs, rowNum) -> new EntryRow(
                uuid(rs, "id"),
                uuid(rs, "registration_id"),
                uuid(rs, "component_id"),
                rs.getBigDecimal("score"),
                rs.getBoolean("is_retake"),
                rs.getString("note")), courseClassId);
        Map<UUID, Map<UUID, GradeEntryView>> entries = new HashMap<>();
        for (EntryRow row : rows) {
            entries.computeIfAbsent(row.registrationId(), ignored -> new HashMap<>())
                    .put(row.componentId(), new GradeEntryView(
                            row.id(), row.componentId(), row.score(), row.retake(), row.note()));
        }
        return entries;
    }

    private ScaleResult findScale(BigDecimal total) {
        return jdbcTemplate.query("""
                SELECT id, letter_grade, gpa_value, is_pass
                FROM grade_scales
                WHERE is_active = 1 AND deleted_at IS NULL AND min_score <= ? AND max_score >= ?
                ORDER BY min_score DESC
                """, (rs, rowNum) -> new ScaleResult(
                uuid(rs, "id"),
                rs.getString("letter_grade"),
                rs.getBigDecimal("gpa_value"),
                rs.getBoolean("is_pass")), total, total).stream().findFirst().orElse(null);
    }

    private void refreshCourseSummaries(UUID courseClassId) {
        List<UUID> registrationIds = jdbcTemplate.query("""
                SELECT id FROM course_registrations
                WHERE course_class_id = ? AND deleted_at IS NULL
                """, (rs, rowNum) -> uuid(rs, "id"), courseClassId);
        registrationIds.forEach(this::refreshSummary);
    }

    private void refreshSummaryForEntry(UUID entryId) {
        UUID registrationId = jdbcTemplate.queryForObject(
                "SELECT registration_id FROM student_component_grades WHERE id = ?",
                UUID.class,
                entryId);
        refreshSummary(registrationId);
    }

    private void refreshSummary(UUID registrationId) {
        UUID courseClassId = jdbcTemplate.query("""
                SELECT course_class_id FROM course_registrations
                WHERE id = ? AND deleted_at IS NULL
                """, (rs, rowNum) -> uuid(rs, "course_class_id"), registrationId).stream()
                .findFirst()
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký học phần"));

        List<GradeComponentView> components = findComponents(courseClassId);
        Map<UUID, BigDecimal> scores = new HashMap<>();
        jdbcTemplate.query("""
                SELECT component_id, score FROM student_component_grades
                WHERE registration_id = ? AND is_active = 1 AND deleted_at IS NULL
                """, (rs, rowNum) -> {
            BigDecimal score = rs.getBigDecimal("score");
            if (score != null) {
                scores.put(uuid(rs, "component_id"), score);
            }
            return null;
        }, registrationId);

        Optional<BigDecimal> total = GradeCalculator.calculate(components, scores);
        ScaleResult scale = total.map(this::findScale).orElse(null);
        UUID scaleId = scale == null ? null : scale.id();
        String letterGrade = scale == null ? null : scale.letterGrade();
        BigDecimal gpaValue = scale == null ? null : scale.gpaValue();
        String result = scale == null ? null : scale.isPass() ? "PASS" : "FAIL";

        List<UUID> existingSummaryIds = jdbcTemplate.query("""
                SELECT id FROM student_summaries
                WHERE registration_id = ? AND is_active = 1 AND deleted_at IS NULL
                ORDER BY created_at DESC
                """, (rs, rowNum) -> uuid(rs, "id"), registrationId);
        if (existingSummaryIds.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO student_summaries
                        (id, registration_id, total_score, scale_id, letter_grade, gpa_value,
                         result, is_finalized, created_at, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 0, CURRENT_TIMESTAMP, 1)
                    """,
                    UUID.randomUUID(), registrationId, total.orElse(null), scaleId,
                    letterGrade, gpaValue, result);
        } else {
            jdbcTemplate.update("""
                    UPDATE student_summaries
                    SET total_score = ?, scale_id = ?, letter_grade = ?, gpa_value = ?,
                        result = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    total.orElse(null), scaleId, letterGrade, gpaValue, result, existingSummaryIds.get(0));
        }
    }

    private void validateComponent(GradeComponentRequest request) {
        if (request == null || request.courseClassId() == null) {
            throw badRequest("Cần chọn lớp học phần");
        }
        if (request.componentCode() == null || request.componentCode().isBlank()
                || request.componentCode().trim().length() > 20) {
            throw badRequest("Mã thành phần điểm không được trống và tối đa 20 ký tự");
        }
        if (request.componentName() == null || request.componentName().isBlank()
                || request.componentName().trim().length() > 50) {
            throw badRequest("Tên thành phần điểm không được trống và tối đa 50 ký tự");
        }
        if (request.weightPercentage() == null || request.weightPercentage().compareTo(ZERO) <= 0
                || request.weightPercentage().compareTo(ONE_HUNDRED) > 0) {
            throw badRequest("Trọng số phải lớn hơn 0 và không vượt quá 100%");
        }
        BigDecimal minimum = request.minScore() == null ? ZERO : request.minScore();
        BigDecimal maximum = request.maxScore() == null ? TEN : request.maxScore();
        if (minimum.compareTo(maximum) >= 0 || minimum.compareTo(ZERO) < 0 || maximum.compareTo(TEN) > 0) {
            throw badRequest("Khoảng điểm phải nằm trong thang 0–10 và điểm tối thiểu nhỏ hơn tối đa");
        }
    }

    private void validateEntry(UUID componentId, BigDecimal score) {
        if (componentId == null) {
            throw badRequest("Cần chọn thành phần điểm");
        }
        validateEntryScore(score);
        validateScoreRange(findComponent(componentId), score);
    }

    private void validateEntryScore(BigDecimal score) {
        if (score == null || score.scale() > 2 || score.compareTo(ZERO) < 0 || score.compareTo(TEN) > 0) {
            throw badRequest("Điểm phải nằm trong khoảng 0–10 và tối đa 2 chữ số thập phân");
        }
    }

    private void validateScoreRange(GradeComponentView component, BigDecimal score) {
        BigDecimal minimum = component.minScore() == null ? ZERO : component.minScore();
        BigDecimal maximum = component.maxScore() == null ? TEN : component.maxScore();
        if (score.compareTo(minimum) < 0 || score.compareTo(maximum) > 0) {
            throw badRequest("Điểm phải nằm trong khoảng " + minimum.toPlainString() + "–" + maximum.toPlainString());
        }
    }

    private void ensureUniqueComponentCode(UUID courseClassId, String code, UUID ignoredId) {
        String sql = """
                SELECT COUNT(*) FROM grade_components
                WHERE course_class_id = ? AND LOWER(component_code) = LOWER(?)
                  AND is_active = 1 AND deleted_at IS NULL
                """;
        Integer matches = ignoredId == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, courseClassId, code.trim())
                : jdbcTemplate.queryForObject(sql + " AND id <> ?", Integer.class, courseClassId, code.trim(),
                        ignoredId);
        if (matches != null && matches > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Mã thành phần điểm đã được sử dụng trong lớp học phần");
        }
    }

    private void ensureNotLocked(UUID entryId) {
        Boolean locked = jdbcTemplate.queryForObject("""
                SELECT is_locked FROM student_component_grades
                WHERE id = ? AND is_active = 1 AND deleted_at IS NULL
                """, Boolean.class, entryId);
        if (Boolean.TRUE.equals(locked)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Điểm đã khóa, không thể sửa hoặc xóa");
        }
    }

    private void requireCourseClass(UUID courseClassId) {

        if (courseClassId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần");
        }
    }

    private void requireRegistrationInCourseClass(UUID registrationId, UUID courseClassId) {
        if (registrationId == null) {
            throw badRequest("Cần chọn sinh viên đã đăng ký lớp học phần");
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM course_registrations
                WHERE id = ? AND course_class_id = ? AND deleted_at IS NULL
                """, Integer.class, registrationId, courseClassId);
        if (count == null || count == 0) {
            throw badRequest("Sinh viên không thuộc lớp học phần của thành phần điểm");
        }
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        String trimmed = note.trim();
        if (trimmed.length() > 255) {
            throw badRequest("Ghi chú tối đa 255 ký tự");
        }
        return trimmed;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static UUID uuid(ResultSet rs, String column) throws SQLException {
        return UUID.fromString(rs.getString(column));
    }

    private static final RowMapper<GradeComponentView> COMPONENT_MAPPER = (rs, rowNum) -> new GradeComponentView(
            uuid(rs, "id"),
            uuid(rs, "course_class_id"),
            rs.getString("component_code"),
            rs.getString("component_name"),
            rs.getBigDecimal("weight_percentage"),
            rs.getBigDecimal("min_score"),
            rs.getBigDecimal("max_score"),
            rs.getBoolean("is_required"),
            (Integer) rs.getObject("input_order"));

    private record RegistrationRow(UUID id, String studentCode, String studentName, String dateOfBirth) {
    }

    private record EntryRow(UUID id, UUID registrationId, UUID componentId, BigDecimal score, boolean retake,
            String note) {
    }

    private record ScaleResult(UUID id, String letterGrade, BigDecimal gpaValue, boolean isPass) {
    }

    public List<CourseClassOption> getCourseClasses() {
        return jdbcTemplate.query("""
                SELECT id, code, name
                FROM courses_classes
                WHERE (is_active IS NULL OR is_active = 1) AND deleted_at IS NULL
                ORDER BY code
                """, (rs, rowNum) -> {
            String code = rs.getString("code");
            String courseCode = code != null && code.contains("-") ? code.split("-")[0] : "IT24M";
            return new CourseClassOption(
                    uuid(rs, "id"),
                    code,
                    courseCode,
                    rs.getString("name"));
        });
    }

    @Transactional
    public CourseClassOption createCourseClass(CourseClassCreateRequest request) {
        if (request == null || request.code() == null || request.code().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã lớp học phần không được để trống");
        }
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên lớp học phần không được để trống");
        }

        String code = request.code().trim().toUpperCase();
        String name = request.name().trim();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM courses_classes WHERE code = ? AND deleted_at IS NULL",
                Integer.class, code);
        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã lớp học phần '" + code + "' đã tồn tại!");
        }

        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO courses_classes (id, code, name, is_active)
                VALUES (?, ?, ?, 1)
                """, id, code, name);

        if (request.initDefaultComponents() == null || request.initDefaultComponents()) {
            initDefaultComponents(id);
        }

        String courseCode = code.contains("-") ? code.split("-")[0] : code;
        return new CourseClassOption(id, code, courseCode, name);
    }

    private void initDefaultComponents(UUID courseClassId) {
        jdbcTemplate.update("""
                INSERT INTO grade_components (id, course_class_id, component_code, component_name, weight_percentage, min_score, max_score, is_required, input_order, is_active, created_at)
                VALUES 
                (NEWID(), ?, 'CC', N'Chuyên cần', 20.00, 0.00, 10.00, 1, 1, 1, CURRENT_TIMESTAMP),
                (NEWID(), ?, 'GK', N'Kiểm tra giữa kỳ', 20.00, 0.00, 10.00, 1, 2, 1, CURRENT_TIMESTAMP),
                (NEWID(), ?, 'CK', N'Thi kết thúc học phần', 60.00, 0.00, 10.00, 1, 3, 1, CURRENT_TIMESTAMP)
                """, courseClassId, courseClassId, courseClassId);
    }

    public List<AvailableStudentOption> getAvailableStudents(UUID courseClassId) {
        requireCourseClass(courseClassId);
        return jdbcTemplate.query("""
                SELECT s.id, s.student_code, s.full_name, s.class_name
                FROM students s
                WHERE s.id NOT IN (
                    SELECT cr.student_id FROM course_registrations cr
                    WHERE cr.course_class_id = ? AND cr.deleted_at IS NULL
                )
                ORDER BY COALESCE(s.stt, 9999), s.student_code
                """, (rs, rowNum) -> new AvailableStudentOption(
                uuid(rs, "id"),
                rs.getString("student_code"),
                rs.getString("full_name"),
                rs.getString("class_name")),
                courseClassId);
    }

    @Transactional
    public void registerStudent(UUID courseClassId, UUID studentId) {
        requireCourseClass(courseClassId);
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần chọn sinh viên để thêm vào lớp");
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM students WHERE id = ?", Integer.class, studentId);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên");
        }

        jdbcTemplate.update("""
                IF NOT EXISTS (SELECT 1 FROM students_ WHERE id = ?)
                BEGIN
                    INSERT INTO students_ (id, code, full_name, is_active)
                    SELECT id, student_code, full_name, 1 FROM students WHERE id = ?
                END
                """, studentId, studentId);

        Integer regCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM course_registrations WHERE course_class_id = ? AND student_id = ? AND deleted_at IS NULL",
                Integer.class, courseClassId, studentId);
        if (regCount != null && regCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sinh viên này đã có trong lớp học phần");
        }

        UUID regId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO course_registrations (id, course_class_id, student_id, is_active, created_at)
                VALUES (?, ?, ?, 1, CURRENT_TIMESTAMP)
                """, regId, courseClassId, studentId);
    }

    @Transactional
    public void removeStudentFromClass(UUID registrationId) {
        jdbcTemplate.update("""
                UPDATE course_registrations
                SET is_active = 0, deleted_at = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted_at IS NULL
                """, registrationId);
    }
}
