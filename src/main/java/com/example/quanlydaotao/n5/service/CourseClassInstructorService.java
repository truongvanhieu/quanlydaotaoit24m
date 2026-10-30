package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.CourseClassInstructor;
import com.example.quanlydaotao.n5.repository.CourseClassInstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseClassInstructorService {

    private final CourseClassInstructorRepository repository;

    public List<CourseClassInstructor> getAll(
            UUID employeeId, UUID courseClassId, String employeeName, String courseClassName,
            String role, Boolean isActive) {
        if (employeeId != null || courseClassId != null || employeeName != null
                || courseClassName != null || role != null || isActive != null) {
            return repository.search(employeeId, courseClassId, employeeName, courseClassName, role, isActive);
        }
        return repository.findAllByDeletedAtIsNull();
    }

    public CourseClassInstructor getById(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin phân công giảng viên"));
    }

    public CourseClassInstructor create(CourseClassInstructor instructor) {
        if (instructor.getEmployeeId() != null && instructor.getCourseClassId() != null) {
            boolean exists = repository.existsByEmployeeIdAndCourseClassIdAndDeletedAtIsNull(
                    instructor.getEmployeeId(), instructor.getCourseClassId()
            );
            if (exists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Giảng viên đã được phân công cho lớp học phần này");
            }
        }
        return repository.save(instructor);
    }

    public CourseClassInstructor update(UUID id, CourseClassInstructor request) {
        CourseClassInstructor existing = getById(id);

        if (request.getEmployeeId() != null && request.getCourseClassId() != null
                && (!request.getEmployeeId().equals(existing.getEmployeeId())
                || !request.getCourseClassId().equals(existing.getCourseClassId()))
                && repository.existsByEmployeeIdAndCourseClassIdAndDeletedAtIsNull(
                request.getEmployeeId(), request.getCourseClassId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Giảng viên đã được phân công cho lớp học phần này");
        }
        existing.setEmployeeId(request.getEmployeeId());
        existing.setCourseClassId(request.getCourseClassId());
        existing.setEmployeeName(request.getEmployeeName());
        existing.setCourseClassName(request.getCourseClassName());
        existing.setRole(request.getRole());
        existing.setUpdatedBy(request.getUpdatedBy());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        return repository.save(existing);
    }

    public void updateStatus(UUID id, Boolean isActive, UUID updatedBy) {
        CourseClassInstructor existing = getById(id);
        existing.setIsActive(isActive);
        existing.setUpdatedBy(updatedBy);
        repository.save(existing);
    }

    public void softDelete(UUID id, UUID deletedBy) {
        CourseClassInstructor existing = getById(id);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(deletedBy);
        repository.save(existing);
    }
}