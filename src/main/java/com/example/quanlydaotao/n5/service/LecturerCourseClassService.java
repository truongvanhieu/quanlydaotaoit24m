package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.LecturerCourseClass;
import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.repository.LecturerCourseClassRepository;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LecturerCourseClassService {
    private final LecturerCourseClassRepository repo;
    private final CourseSectionRepository csRepo;

    public LecturerCourseClassService(LecturerCourseClassRepository repo, CourseSectionRepository csRepo) {
        this.repo = repo;
        this.csRepo = csRepo;
    }

    public List<LecturerCourseClass> findAll(Boolean isActive) {
        if (isActive != null) {
            return repo.findByIsActiveAndDeletedAtIsNull(isActive);
        }
        return repo.findByDeletedAtIsNull();
    }

    public LecturerCourseClass findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phân công với ID: " + id));
    }

    public LecturerCourseClass save(LecturerCourseClass e, UUID csId) {
        UUID targetCsId = csId;
        if (targetCsId == null && e.getCourseSection() != null) {
            targetCsId = e.getCourseSection().getId();
        }
        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId).orElse(null);
            if (cs != null) {
                e.setCourseSection(cs);
                if (e.getCourseClassName() == null || e.getCourseClassName().isBlank()) {
                    e.setCourseClassName(cs.getName());
                }
            }
        }
        return repo.save(e);
    }

    public LecturerCourseClass update(UUID id, LecturerCourseClass updateData, UUID csId) {
        LecturerCourseClass existing = findById(id);
        if (updateData.getEmployeeId() != null) existing.setEmployeeId(updateData.getEmployeeId());
        if (updateData.getEmployeeName() != null) existing.setEmployeeName(updateData.getEmployeeName());
        if (updateData.getRole() != null) existing.setRole(updateData.getRole());
        if (updateData.getNote() != null) existing.setNote(updateData.getNote());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        UUID targetCsId = csId != null ? csId : (updateData.getCourseSection() != null ? updateData.getCourseSection().getId() : null);
        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId).orElse(null);
            if (cs != null) {
                existing.setCourseSection(cs);
                existing.setCourseClassName(cs.getName());
            }
        }
        return repo.save(existing);
    }

    public LecturerCourseClass toggleActive(UUID id, Boolean isActive) {
        LecturerCourseClass existing = findById(id);
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        return repo.save(existing);
    }

    public void delete(UUID id) {
        LecturerCourseClass existing = findById(id);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
