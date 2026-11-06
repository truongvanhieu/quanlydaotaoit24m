package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SemesterService {
    private final SemesterRepository repo;
    private final SchoolYearRepository syRepo;

    public SemesterService(SemesterRepository repo, SchoolYearRepository syRepo) {
        this.repo = repo;
        this.syRepo = syRepo;
    }

    public List<Semester> findAll(Boolean isActive) {
        if (isActive != null) {
            return repo.findByIsActiveAndDeletedAtIsNull(isActive);
        }
        return repo.findByDeletedAtIsNull();
    }

    public Semester findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ với ID: " + id));
    }

    public Semester save(Semester e, UUID syId) {
        if (syId != null) {
            SchoolYear sy = syRepo.findById(syId).orElse(null);
            if (sy != null) {
                e.setSchoolYear(sy);
                e.setSchoolYearName(sy.getName());
            }
        } else if (e.getSchoolYear() != null && e.getSchoolYear().getId() != null) {
            SchoolYear sy = syRepo.findById(e.getSchoolYear().getId()).orElse(null);
            if (sy != null) {
                e.setSchoolYear(sy);
                e.setSchoolYearName(sy.getName());
            }
        }
        return repo.save(e);
    }

    public Semester update(UUID id, Semester updateData, UUID syId) {
        Semester existing = findById(id);
        if (updateData.getCode() != null) existing.setCode(updateData.getCode());
        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getStartDate() != null) existing.setStartDate(updateData.getStartDate());
        if (updateData.getEndDate() != null) existing.setEndDate(updateData.getEndDate());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        UUID targetSyId = syId != null ? syId : (updateData.getSchoolYear() != null ? updateData.getSchoolYear().getId() : null);
        if (targetSyId != null) {
            SchoolYear sy = syRepo.findById(targetSyId).orElse(null);
            if (sy != null) {
                existing.setSchoolYear(sy);
                existing.setSchoolYearName(sy.getName());
            }
        }
        return repo.save(existing);
    }

    public Semester toggleActive(UUID id, Boolean isActive) {
        Semester existing = findById(id);
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        return repo.save(existing);
    }

    public void delete(UUID id) {
        Semester existing = findById(id);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
