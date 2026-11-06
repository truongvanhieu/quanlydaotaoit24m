package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.AcademicYear;
import com.example.quanlydaotao.n5.repository.AcademicYearRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AcademicYearService {
    private final AcademicYearRepository repo;

    public AcademicYearService(AcademicYearRepository repo) {
        this.repo = repo;
    }

    public List<AcademicYear> findAll(Boolean isActive) {
        if (isActive != null) {
            return repo.findByIsActiveAndDeletedAtIsNull(isActive);
        }
        return repo.findByDeletedAtIsNull();
    }

    public AcademicYear findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy niên khóa với ID: " + id));
    }

    public AcademicYear save(AcademicYear e) {
        return repo.save(e);
    }

    public AcademicYear update(UUID id, AcademicYear updateData) {
        AcademicYear existing = findById(id);
        if (updateData.getCode() != null) existing.setCode(updateData.getCode());
        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getYear() != null) existing.setYear(updateData.getYear());
        if (updateData.getDescription() != null) existing.setDescription(updateData.getDescription());
        if (updateData.getStartDate() != null) existing.setStartDate(updateData.getStartDate());
        if (updateData.getEndDate() != null) existing.setEndDate(updateData.getEndDate());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());
        return repo.save(existing);
    }

    public AcademicYear toggleActive(UUID id, Boolean isActive) {
        AcademicYear existing = findById(id);
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        return repo.save(existing);
    }

    public void delete(UUID id) {
        AcademicYear existing = findById(id);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
