package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.dto.CourseClassInstructorDetails;
import com.example.quanlydaotao.n5.dto.CourseClassInstructorRequest;
import com.example.quanlydaotao.n5.entity.CourseClassInstructor;
import com.example.quanlydaotao.n5.entity.Instructor;
import com.example.quanlydaotao.n5.repository.CourseClassInstructorRepository;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseClassInstructorService {

    private static final String DEFAULT_ROLE = "Giảng viên chính";

    private final CourseClassInstructorRepository repository;
    private final CourseSectionRepository courseSectionRepository;
    private final InstructorRepository instructorRepository;

    public List<CourseClassInstructorDetails> getAll(
            UUID employeeId, String instructorCode, String instructorName,
            String courseClassName, String role) {
        return repository.findDetails(
                null,
                employeeId,
                normalize(instructorCode),
                normalize(instructorName),
                normalize(courseClassName),
                normalize(role));
    }

    public CourseClassInstructorDetails getDetailsById(UUID id) {
        return repository.findDetails(id, null, null, null, null, null).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin phân công giảng viên"));
    }

    @Transactional
    public CourseClassInstructorDetails create(CourseClassInstructorRequest request) {
        validate(request);
        Instructor instructor = resolveInstructor(request);
        if (repository.existsByEmployeeIdAndCourseClassId(
                instructor.getId(), request.getCourseClassId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Giảng viên đã được phân công cho lớp học phần này");
        }

        CourseClassInstructor assignment = CourseClassInstructor.builder()
                .employeeId(instructor.getId())
                .courseClassId(request.getCourseClassId())
                .role(normalizeRole(request.getRole()))
                .teachingHours(request.getTeachingHours())
                .isActive(request.getIsActive())
                .build();
        prepareForCreate(assignment);
        CourseClassInstructor saved = repository.save(assignment);
        return getDetailsById(saved.getId());
    }

    @Transactional
    public CourseClassInstructorDetails update(UUID id, CourseClassInstructorRequest request) {
        CourseClassInstructor existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin phân công giảng viên"));
        validate(request);
        Instructor instructor = resolveInstructor(request);
        boolean sameAssignment = instructor.getId().equals(existing.getEmployeeId())
                && request.getCourseClassId().equals(existing.getCourseClassId());
        if (!sameAssignment && repository.existsByEmployeeIdAndCourseClassId(
                instructor.getId(), request.getCourseClassId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Giảng viên đã được phân công cho lớp học phần này");
        }

        existing.setEmployeeId(instructor.getId());
        existing.setCourseClassId(request.getCourseClassId());
        existing.setRole(normalizeRole(request.getRole()));
        existing.setTeachingHours(request.getTeachingHours());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        CourseClassInstructor saved = repository.save(existing);
        return getDetailsById(saved.getId());
    }

    private void validate(CourseClassInstructorRequest request) {
        if (request == null || request.getCourseClassId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Lớp học phần là bắt buộc");
        }
        if (!courseSectionRepository.existsById(request.getCourseClassId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lớp học phần không tồn tại");
        }

        String code = normalize(request.getInstructorCode());
        String name = normalize(request.getInstructorName());
        if ((code == null) != (name == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mã và tên giảng viên phải được nhập cùng nhau");
        }
        if (code == null && request.getEmployeeId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mã và tên giảng viên là bắt buộc");
        }
        if (code != null && code.length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mã giảng viên không được vượt quá 50 ký tự");
        }
        if (name != null && name.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tên giảng viên không được vượt quá 255 ký tự");
        }
        if (request.getRole() != null && request.getRole().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vai trò không được vượt quá 100 ký tự");
        }
        if (request.getTeachingHours() != null && request.getTeachingHours() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Số tiết không được âm");
        }
    }

    private Instructor resolveInstructor(CourseClassInstructorRequest request) {
        String code = normalize(request.getInstructorCode());
        String name = normalize(request.getInstructorName());
        if (code == null) {
            return instructorRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));
        }

        Instructor instructor = instructorRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> Instructor.builder().code(code).name(name).build());
        instructor.setCode(code);
        instructor.setName(name);
        if (instructor.getCreatedAt() == null) {
            instructor.setCreatedAt(java.time.LocalDateTime.now());
        }
        if (instructor.getIsActive() == null) {
            instructor.setIsActive(true);
        }
        return instructorRepository.save(instructor);
    }

    private void prepareForCreate(CourseClassInstructor instructor) {
        instructor.setCreatedAt(java.time.LocalDateTime.now());
        if (instructor.getIsActive() == null) {
            instructor.setIsActive(true);
        }
    }

    private String normalizeRole(String role) {
        String normalized = normalize(role);
        return normalized == null ? DEFAULT_ROLE : normalized;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public void delete(UUID id) {
        CourseClassInstructor existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin phân công giảng viên"));
        repository.delete(existing);
    }
}
