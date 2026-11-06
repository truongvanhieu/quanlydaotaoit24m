package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.entity.Student;
import com.example.quanlydaotao.n5.repository.StudentCourseSectionRepository;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StudentCourseSectionService {
    private final StudentCourseSectionRepository repo;
    private final CourseSectionRepository csRepo;
    private final StudentRepository studentRepo;

    public StudentCourseSectionService(StudentCourseSectionRepository repo,
                                      CourseSectionRepository csRepo,
                                      StudentRepository studentRepo) {
        this.repo = repo;
        this.csRepo = csRepo;
        this.studentRepo = studentRepo;
    }

    public List<StudentCourseSection> findAll(Boolean isActive) {
        if (isActive != null) {
            return repo.findByIsActiveAndDeletedAtIsNull(isActive);
        }
        return repo.findByDeletedAtIsNull();
    }

    public StudentCourseSection findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký với ID: " + id));
    }

    public StudentCourseSection save(StudentCourseSection e, UUID csId) {
        UUID targetCsId = csId;
        if (targetCsId == null && e.getCourseSection() != null) {
            targetCsId = e.getCourseSection().getId();
        }

        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần!"));

            if (cs.getStatus() != null) {
                String st = cs.getStatus().toLowerCase();
                if (st.contains("đóng") || st.equals("closed") || st.equals("canceled") || st.contains("hủy")) {
                    throw new RuntimeException("Lớp học phần này hiện không mở đăng ký!");
                }
            }

            if (e.getStudentId() != null && repo.existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(e.getStudentId(), targetCsId)) {
                throw new RuntimeException("Sinh viên đã đăng ký lớp học phần này rồi!");
            }

            long currentCount = repo.countByCourseSectionIdAndDeletedAtIsNull(targetCsId);
            if (cs.getMaxStudents() != null && currentCount >= cs.getMaxStudents()) {
                throw new RuntimeException("Lớp học phần đã đầy (Tối đa: " + cs.getMaxStudents() + " sinh viên)!");
            }

            e.setCourseSection(cs);
        }

        // Cache student information if studentId exists
        if (e.getStudentId() != null) {
            studentRepo.findById(e.getStudentId()).ifPresent(st -> {
                if (e.getStudentName() == null || e.getStudentName().isBlank()) {
                    e.setStudentName(st.getFullName());
                }
                if (e.getStudentCode() == null || e.getStudentCode().isBlank()) {
                    e.setStudentCode(st.getStudentCode());
                }
            });
        }

        return repo.save(e);
    }

    public StudentCourseSection update(UUID id, StudentCourseSection updateData, UUID csId) {
        StudentCourseSection existing = findById(id);
        if (updateData.getStudentId() != null) existing.setStudentId(updateData.getStudentId());
        if (updateData.getStudentCode() != null) existing.setStudentCode(updateData.getStudentCode());
        if (updateData.getStudentName() != null) existing.setStudentName(updateData.getStudentName());
        if (updateData.getRegistrationDate() != null) existing.setRegistrationDate(updateData.getRegistrationDate());
        if (updateData.getRegistrationType() != null) existing.setRegistrationType(updateData.getRegistrationType());
        if (updateData.getStatus() != null) existing.setStatus(updateData.getStatus());
        if (updateData.getAttemptNumber() != null) existing.setAttemptNumber(updateData.getAttemptNumber());
        if (updateData.getNote() != null) existing.setNote(updateData.getNote());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        UUID targetCsId = csId != null ? csId : (updateData.getCourseSection() != null ? updateData.getCourseSection().getId() : null);
        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId).orElse(null);
            if (cs != null) existing.setCourseSection(cs);
        }

        return repo.save(existing);
    }

    public StudentCourseSection toggleActive(UUID id, Boolean isActive) {
        StudentCourseSection existing = findById(id);
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        return repo.save(existing);
    }

    public void delete(UUID id) {
        StudentCourseSection existing = findById(id);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
