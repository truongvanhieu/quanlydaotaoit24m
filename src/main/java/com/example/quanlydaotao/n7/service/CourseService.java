package com.example.quanlydaotao.n7.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.CourseRequest;
import com.example.quanlydaotao.n7.dto.response.CourseResponse;
import com.example.quanlydaotao.n7.entity.Course;
import com.example.quanlydaotao.n7.repository.CourseRepository;

@Service
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository repository;
    private final JdbcTemplate jdbc;
    public CourseService(CourseRepository repository, JdbcTemplate jdbc) { this.repository = repository; this.jdbc = jdbc; }

    public List<CourseResponse> list(String keyword, Boolean active) {
        return repository.search(blankToNull(keyword), active).stream().map(this::response).toList();
    }

    public CourseResponse get(UUID id) { return response(find(id)); }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        validate(request);
        String code = request.code().trim();
        if (repository.existsByCodeIgnoreCaseAndDeletedAtIsNull(code)) conflict("Mã học phần đã tồn tại");
        validateDepartment(request.departmentId());
        Course course = new Course();
        apply(course, request);
        return response(repository.save(course));
    }

    @Transactional
    public CourseResponse update(UUID id, CourseRequest request) {
        validate(request);
        Course course = repository.findById(id).filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> notFound("Không tìm thấy học phần"));
        String code = request.code().trim();
        if (repository.existsByCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(code, id)) conflict("Mã học phần đã tồn tại");
        validateDepartment(request.departmentId());
        apply(course, request);
        return response(repository.save(course));
    }

    @Transactional
    public void delete(UUID id) {
        Course course = repository.findById(id).filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> notFound("Không tìm thấy học phần"));
        Integer sectionCount = jdbc.queryForObject("select count(*) from course_sections where course_id = ? and deleted_at is null", Integer.class, id);
        Integer classCount = jdbc.queryForObject("select count(*) from courses_classes where course_id = ? and deleted_at is null", Integer.class, id);
        if ((sectionCount != null && sectionCount > 0) || (classCount != null && classCount > 0))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể xóa học phần đang có lớp học phần");
        course.setDeletedAt(LocalDateTime.now());
        course.setActive(false);
        repository.save(course);
    }

    private Course find(UUID id) {
        Course course = repository.findById(id).orElseThrow(() -> notFound("Không tìm thấy học phần"));
        if (course.getDeletedAt() != null) throw notFound("Không tìm thấy học phần");
        return course;
    }

    private void apply(Course c, CourseRequest r) {
        c.setDepartmentId(r.departmentId()); c.setCode(r.code().trim()); c.setName(r.name().trim());
        c.setNameEn(trimToNull(r.nameEn())); c.setCredits(r.credits()); c.setCourseType(trimToNull(r.courseType()));
        c.setTheoryHours(r.theoryHours()); c.setPracticeHours(r.practiceHours()); c.setSelfStudyHours(r.selfStudyHours());
        c.setInternshipCredits(r.internshipCredits()); c.setDescription(trimToNull(r.description()));
        c.setActive(r.active() == null || r.active());
    }

    private void validate(CourseRequest r) {
        if (r == null || blankToNull(r.code()) == null || blankToNull(r.name()) == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã và tên học phần là bắt buộc");
        if (r.code().trim().length() > 100 || r.name().trim().length() > 255
                || (r.nameEn() != null && r.nameEn().length() > 255)
                || (r.courseType() != null && r.courseType().length() > 255))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã/tên học phần vượt quá độ dài cho phép");
        for (BigDecimal number : new BigDecimal[] { r.credits(), r.theoryHours(), r.practiceHours(), r.selfStudyHours(), r.internshipCredits() }) {
            if (number != null && number.signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tín chỉ và số giờ không được âm");
        }
    }

    private void validateDepartment(UUID id) {
        if (id == null) return;
        Integer found = jdbc.queryForObject("select case when exists (select 1 from departments where id = ?) then 1 else 0 end", Integer.class, id);
        if (!Integer.valueOf(1).equals(found)) throw notFound("Không tìm thấy khoa");
    }
    private CourseResponse response(Course c) {
        return new CourseResponse(c.getId(), c.getDepartmentId(), c.getCode(), c.getName(), c.getNameEn(), c.getCredits(),
                c.getCourseType(), c.getTheoryHours(), c.getPracticeHours(), c.getSelfStudyHours(), c.getInternshipCredits(),
                c.getDescription(), c.getActive(), c.getCreatedAt(), c.getUpdatedAt());
    }
    private String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private String trimToNull(String v) { return v == null ? null : v.trim(); }
    private void conflict(String m) { throw new ResponseStatusException(HttpStatus.CONFLICT, m); }
    private ResponseStatusException notFound(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
}
