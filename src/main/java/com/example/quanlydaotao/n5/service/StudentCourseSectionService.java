package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.dto.CourseSectionStudent;
import com.example.quanlydaotao.n5.dto.CourseSectionStudentsResponse;
import com.example.quanlydaotao.n5.dto.StudentCourseSectionDetails;
import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.StudentCourseSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentCourseSectionService {

    private final StudentCourseSectionRepository repository;
    private final CourseSectionRepository courseSectionRepository;

    public List<StudentCourseSectionDetails> getDetails(UUID studentId, UUID courseSectionId) {
        return repository.findDetails(studentId, courseSectionId, null);
    }

    public StudentCourseSectionDetails getDetailsById(UUID id) {
        return repository.findDetails(null, null, id).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin đăng ký học phần"));
    }

    public CourseSectionStudentsResponse getStudentsByCourseSection(UUID courseSectionId) {
        CourseSection courseSection = courseSectionRepository.findById(courseSectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
        List<CourseSectionStudent> students = repository.findStudentsByCourseSectionId(courseSectionId);
        return new CourseSectionStudentsResponse(
                courseSection.getId(),
                courseSection.getCode(),
                courseSection.getName(),
                students.size(),
                students);
    }

    public StudentCourseSectionDetails create(StudentCourseSection registration) {
        CourseSection courseSection = validateTarget(registration);
        if (Boolean.TRUE.equals(registration.getIsActive())
                && repository.existsByStudentIdAndCourseSectionIdAndIsActiveTrue(
                registration.getStudentId(), registration.getCourseSectionId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Sinh viên đã đăng ký lớp học phần này");
        }
        if (Boolean.TRUE.equals(registration.getIsActive())) {
            validateCapacity(courseSection, null);
        }
        StudentCourseSection saved = repository.save(registration);
        return getDetailsById(saved.getId());
    }

    public StudentCourseSectionDetails update(UUID id, StudentCourseSection request) {
        StudentCourseSection existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin đăng ký học phần"));
        CourseSection courseSection = validateTarget(request);
        if (Boolean.TRUE.equals(request.getIsActive())
                && repository.existsByStudentIdAndCourseSectionIdAndIsActiveTrueAndIdNot(
                request.getStudentId(), request.getCourseSectionId(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Sinh viên đã đăng ký lớp học phần này");
        }
        if (Boolean.TRUE.equals(request.getIsActive())
                && (!Objects.equals(existing.getCourseSectionId(), request.getCourseSectionId())
                || !Boolean.TRUE.equals(existing.getIsActive()))) {
            validateCapacity(courseSection, id);
        }

        existing.setStudentId(request.getStudentId());
        existing.setCourseSectionId(request.getCourseSectionId());
        existing.setRegistrationDate(request.getRegistrationDate());
        existing.setRegistrationType(request.getRegistrationType());
        existing.setStatus(request.getStatus());
        existing.setAttemptNumber(request.getAttemptNumber());
        existing.setNote(request.getNote());
        existing.setIsActive(request.getIsActive());
        StudentCourseSection saved = repository.save(existing);
        return getDetailsById(saved.getId());
    }

    private CourseSection validateTarget(StudentCourseSection registration) {
        if (registration.getStudentId() == null || registration.getCourseSectionId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mã UUID sinh viên và lớp học phần là bắt buộc");
        }
        CourseSection courseSection = courseSectionRepository.findById(registration.getCourseSectionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Lớp học phần không tồn tại"));
        if (registration.getRegistrationDate() == null) {
            registration.setRegistrationDate(java.time.LocalDateTime.now());
        }
        if (registration.getCreatedAt() == null) {
            registration.setCreatedAt(java.time.LocalDateTime.now());
        }
        if (registration.getIsActive() == null) {
            registration.setIsActive(true);
        }
        return courseSection;
    }

    private void validateCapacity(CourseSection courseSection, UUID excludedRegistrationId) {
        if (courseSection.getMaxStudents() == null) {
            return;
        }
        long registrations = excludedRegistrationId == null
                ? repository.countByCourseSectionIdAndIsActiveTrue(courseSection.getId())
                : repository.countByCourseSectionIdAndIsActiveTrueAndIdNot(
                        courseSection.getId(), excludedRegistrationId);
        if (registrations >= courseSection.getMaxStudents()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lớp học phần đã đủ sĩ số");
        }
    }

    public void delete(UUID id) {
        StudentCourseSection existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin đăng ký học phần"));
        repository.delete(existing);
    }
}
