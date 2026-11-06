package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SchoolYearService {
    private final SchoolYearRepository repo;

    public SchoolYearService(SchoolYearRepository repo) {
        this.repo = repo;
    }

    public List<SchoolYear> findAll(Boolean isActive) {
        if (isActive != null) {
            return repo.findByIsActiveAndDeletedAtIsNull(isActive);
        }
        return repo.findByDeletedAtIsNull();
    }

    public SchoolYear findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy năm học với ID: " + id));
    }

    public SchoolYear save(SchoolYear e) {
        return repo.save(e);
    }

    public SchoolYear update(UUID id, SchoolYear updateData) {
        SchoolYear existing = findById(id);
        if (updateData.getCode() != null) existing.setCode(updateData.getCode());
        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getDescription() != null) existing.setDescription(updateData.getDescription());
        if (updateData.getNote() != null) existing.setNote(updateData.getNote());
        if (updateData.getStartDate() != null) existing.setStartDate(updateData.getStartDate());
        if (updateData.getEndDate() != null) existing.setEndDate(updateData.getEndDate());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());
        return repo.save(existing);
    }

    public SchoolYear toggleActive(UUID id, Boolean isActive) {
        SchoolYear existing = findById(id);
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        return repo.save(existing);
    }

    public void delete(UUID id) {
        SchoolYear existing = findById(id);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
