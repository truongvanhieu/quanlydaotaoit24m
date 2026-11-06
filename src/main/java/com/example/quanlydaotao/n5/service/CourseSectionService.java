package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CourseSectionService {
    private final CourseSectionRepository repo;
    private final SemesterRepository semRepo;

    public CourseSectionService(CourseSectionRepository repo, SemesterRepository semRepo) {
        this.repo = repo;
        this.semRepo = semRepo;
    }

    public List<CourseSection> findAll(Boolean isActive) {
        if (isActive != null) {
            return repo.findByIsActiveAndDeletedAtIsNull(isActive);
        }
        return repo.findByDeletedAtIsNull();
    }

    public CourseSection findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần với ID: " + id));
    }

    public CourseSection save(CourseSection e, UUID semId) {
        if (semId != null) {
            Semester sem = semRepo.findById(semId).orElse(null);
            if (sem != null) e.setSemester(sem);
        } else if (e.getSemester() != null && e.getSemester().getId() != null) {
            Semester sem = semRepo.findById(e.getSemester().getId()).orElse(null);
            if (sem != null) e.setSemester(sem);
        }
        return repo.save(e);
    }

    public CourseSection update(UUID id, CourseSection updateData, UUID semId) {
        CourseSection existing = findById(id);
        if (updateData.getCode() != null) existing.setCode(updateData.getCode());
        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getCourseId() != null) existing.setCourseId(updateData.getCourseId());
        if (updateData.getRoomId() != null) existing.setRoomId(updateData.getRoomId());
        if (updateData.getBuildingId() != null) existing.setBuildingId(updateData.getBuildingId());
        if (updateData.getMaxStudents() != null) existing.setMaxStudents(updateData.getMaxStudents());
        if (updateData.getMinStudents() != null) existing.setMinStudents(updateData.getMinStudents());
        if (updateData.getClassType() != null) existing.setClassType(updateData.getClassType());
        if (updateData.getStatus() != null) existing.setStatus(updateData.getStatus());
        if (updateData.getRegistrationStart() != null) existing.setRegistrationStart(updateData.getRegistrationStart());
        if (updateData.getRegistrationEnd() != null) existing.setRegistrationEnd(updateData.getRegistrationEnd());
        if (updateData.getNote() != null) existing.setNote(updateData.getNote());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        UUID targetSemId = semId != null ? semId : (updateData.getSemester() != null ? updateData.getSemester().getId() : null);
        if (targetSemId != null) {
            Semester sem = semRepo.findById(targetSemId).orElse(null);
            if (sem != null) existing.setSemester(sem);
        }
        return repo.save(existing);
    }

    public CourseSection toggleActive(UUID id, Boolean isActive) {
        CourseSection existing = findById(id);
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        return repo.save(existing);
    }

    public void delete(UUID id) {
        CourseSection existing = findById(id);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
