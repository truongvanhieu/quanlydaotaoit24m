package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.dto.StudentCourseSectionDetails;
import com.example.quanlydaotao.n5.repository.StudentCourseSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentCourseSectionService {

    private final StudentCourseSectionRepository repository;
    private static final List<String> ALLOWED_STATUSES =
            List.of("REGISTERED", "STUDYING", "COMPLETED", "CANCELLED");

    public List<StudentCourseSectionDetails> getDetails(
            UUID studentId, UUID courseSectionId, String status, Boolean isActive) {
        String normalizedStatus = status == null || status.isBlank()
                ? null
                : normalizeStatus(status);
        return repository.findDetails(studentId, courseSectionId, normalizedStatus, isActive);
    }

    public List<StudentCourseSection> getAll(UUID studentId, UUID courseSectionId, String status, String registrationType, Boolean isActive) {
        if (studentId != null || courseSectionId != null || status != null || registrationType != null || isActive != null) {
            return repository.search(studentId, courseSectionId, status, registrationType, isActive);
        }
        return repository.findAllByDeletedAtIsNull();
    }

    public StudentCourseSection getById(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin đăng ký học phần"));
    }

    public StudentCourseSection create(StudentCourseSection registration) {
        registration.setStatus(normalizeStatus(registration.getStatus()));
        if (registration.getStudentId() != null && registration.getCourseSectionId() != null) {
            boolean exists = repository.existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(
                    registration.getStudentId(), registration.getCourseSectionId()
            );
            if (exists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sinh viên đã đăng ký lớp học phần này trước đó");
            }
        }
        return repository.save(registration);
    }

    public StudentCourseSection update(UUID id, StudentCourseSection request) {
        StudentCourseSection existing = getById(id);

        existing.setStudentId(request.getStudentId());
        existing.setCourseSectionId(request.getCourseSectionId());
        existing.setRegistrationDate(request.getRegistrationDate());
        existing.setRegistrationType(request.getRegistrationType());
        if (request.getStatus() != null) {
            existing.setStatus(normalizeStatus(request.getStatus()));
        }
        existing.setAttemptNumber(request.getAttemptNumber());
        existing.setNote(request.getNote());
        existing.setUpdatedBy(request.getUpdatedBy());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        return repository.save(existing);
    }

    public void updateStatus(UUID id, String status, UUID updatedBy) {
        StudentCourseSection existing = getById(id);
        existing.setStatus(normalizeStatus(status));
        existing.setUpdatedBy(updatedBy);
        repository.save(existing);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "REGISTERED";
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái phải là REGISTERED, STUDYING, COMPLETED hoặc CANCELLED");
        }
        return normalized;
    }

    public void softDelete(UUID id, UUID deletedBy) {
        StudentCourseSection existing = getById(id);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(deletedBy);
        repository.save(existing);
    }
}