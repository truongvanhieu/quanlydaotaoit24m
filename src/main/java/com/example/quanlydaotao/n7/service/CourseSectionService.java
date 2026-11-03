package com.example.quanlydaotao.n7.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.CourseSectionRequest;
import com.example.quanlydaotao.n7.dto.response.CourseSectionResponse;
import com.example.quanlydaotao.n7.dto.response.CourseSectionStudentResponse;
import com.example.quanlydaotao.n7.entity.Course;
import com.example.quanlydaotao.n7.entity.CourseSection;
import com.example.quanlydaotao.n7.repository.CourseSectionRepository;

import jakarta.persistence.EntityManager;

@Service
@Transactional(readOnly = true)
public class CourseSectionService {
    private final CourseSectionRepository repository;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public CourseSectionService(CourseSectionRepository repository,
            EntityManager entityManager, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    public CourseSection find(UUID id) {
        CourseSection section = repository.findById(id).orElseThrow(() -> notFound("Không tìm thấy lớp học phần"));
        if (section.getDeletedAt() != null || Boolean.FALSE.equals(section.getActive())) {
            throw notFound("Không tìm thấy lớp học phần");
        }
        return section;
    }

    public List<CourseSectionResponse> list(String keyword, Boolean active) {
        return repository.search(blankToNull(keyword), active).stream().map(this::response).toList();
    }

    public CourseSectionResponse get(UUID id) { return response(find(id)); }

    public List<CourseSectionResponse> byStudent(UUID studentId, String keyword) {
        Integer found = jdbcTemplate.queryForObject("select case when exists (select 1 from students_ where id = ?) then 1 else 0 end",
                Integer.class, studentId);
        if (!Integer.valueOf(1).equals(found)) throw notFound("Không tìm thấy sinh viên");
        return repository.findForStudent(studentId, blankToNull(keyword)).stream().map(this::response).toList();
    }

    public List<CourseSectionResponse> byLecturer(UUID lecturerId, String keyword) {
        Integer found = jdbcTemplate.queryForObject("select case when exists (select 1 from lecturers where id = ?) then 1 else 0 end",
                Integer.class, lecturerId);
        if (!Integer.valueOf(1).equals(found)) throw notFound("Không tìm thấy giảng viên");
        return repository.findForLecturer(lecturerId, blankToNull(keyword)).stream().map(this::response).toList();
    }

    public List<CourseSectionStudentResponse> students(UUID sectionId) {
        find(sectionId);
        return jdbcTemplate.query("""
                select scs.student_id, s.code as student_code, s.full_name, u.email, scs.status, scs.registration_type
                from student_course_sections scs
                join students_ s on s.id = scs.student_id
                left join users u on u.id = s.user_id
                where scs.course_section_id = ? and scs.deleted_at is null
                  and coalesce(scs.is_active, 1) = 1
                order by s.student_code
                """, (rs, row) -> new CourseSectionStudentResponse(
                rs.getObject("student_id", UUID.class), rs.getString("student_code"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("status"), rs.getString("registration_type")), sectionId);
    }

    @Transactional
    public CourseSectionResponse create(CourseSectionRequest request) {
        validate(request);
        String code = request.code().trim();
        if (repository.existsByCodeIgnoreCaseAndDeletedAtIsNull(code)) throw conflict("Mã lớp học phần đã tồn tại");
        CourseSection section = new CourseSection();
        apply(section, request);
        return response(repository.save(section));
    }

    @Transactional
    public CourseSectionResponse update(UUID id, CourseSectionRequest request) {
        validate(request);
        CourseSection section = repository.findById(id).filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> notFound("Không tìm thấy lớp học phần"));
        String code = request.code().trim();
        if (repository.existsByCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(code, id)) throw conflict("Mã lớp học phần đã tồn tại");
        apply(section, request);
        return response(repository.save(section));
    }

    @Transactional
    public void delete(UUID id) {
        CourseSection section = repository.findById(id).filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> notFound("Không tìm thấy lớp học phần"));
        LocalDateTime deletedAt = LocalDateTime.now();
        jdbcTemplate.update("""
                update schedules
                set deleted_at = ?, is_active = 0, updated_at = ?
                where course_section_id = ? and deleted_at is null
                """, deletedAt, deletedAt, id);
        jdbcTemplate.update("""
                update student_course_sections
                set deleted_at = ?, is_active = 0, updated_at = ?
                where course_section_id = ? and deleted_at is null
                """, deletedAt, deletedAt, id);
        section.setDeletedAt(deletedAt);
        section.setActive(false);
        repository.save(section);
    }

    private void apply(CourseSection section, CourseSectionRequest request) {
        Course course = entityManager.find(Course.class, request.courseId());
        if (course == null) throw notFound("Không tìm thấy học phần gốc");
        section.setCode(request.code().trim());
        section.setName(request.name().trim());
        section.setCourse(course);
        section.setSemesterId(request.semesterId());
        section.setRoomId(request.roomId());
        section.setBuildingId(request.buildingId());
        section.setMaxStudents(request.maxStudents());
        section.setMinStudents(request.minStudents());
        section.setClassType(trimToNull(request.classType()));
        section.setStatus(trimToNull(request.status()));
        section.setRegistrationStart(request.registrationStart());
        section.setRegistrationEnd(request.registrationEnd());
        section.setNote(trimToNull(request.note()));
        section.setActive(request.active() == null || request.active());
    }

    private void validate(CourseSectionRequest request) {
        if (request == null || blankToNull(request.code()) == null || blankToNull(request.name()) == null
                || request.courseId() == null) throw badRequest("Mã lớp, tên lớp và học phần gốc là bắt buộc");
        if (request.code().trim().length() > 100 || request.name().trim().length() > 255)
            throw badRequest("Mã lớp tối đa 100 ký tự, tên lớp tối đa 255 ký tự");
        if (request.maxStudents() != null && request.maxStudents() < 0
                || request.minStudents() != null && request.minStudents() < 0)
            throw badRequest("Sĩ số không được âm");
        if (request.maxStudents() != null && request.minStudents() != null
                && request.minStudents() > request.maxStudents()) throw badRequest("Sĩ số tối thiểu không thể lớn hơn tối đa");
        if (request.registrationStart() != null && request.registrationEnd() != null
                && request.registrationStart().isAfter(request.registrationEnd()))
            throw badRequest("Thời gian bắt đầu đăng ký phải trước thời gian kết thúc");
    }

    private CourseSectionResponse response(CourseSection s) {
        Course c = s.getCourse();
        return new CourseSectionResponse(s.getId(), s.getCode(), s.getName(), c == null ? null : c.getId(),
                c == null ? null : c.getCode(), c == null ? null : c.getName(), s.getSemesterId(), s.getRoomId(),
                s.getBuildingId(), s.getMaxStudents(), s.getMinStudents(), s.getClassType(), s.getStatus(),
                s.getRegistrationStart(), s.getRegistrationEnd(), s.getNote(), s.getActive(), s.getCreatedAt(), s.getUpdatedAt());
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String trimToNull(String value) { return value == null ? null : value.trim(); }
    private ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    private ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
}
