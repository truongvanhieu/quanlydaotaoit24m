package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        return findAll(null, isActive);
    }

    public List<SchoolYear> findAll(String keyword, Boolean isActive) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return repo.search(kw, isActive);
    }

    public SchoolYear findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy năm học với ID: " + id));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
            throw new RuntimeException("Ngày bắt đầu phải nhỏ hơn ngày kết thúc!");
        }
    }

    public SchoolYear save(SchoolYear e) {
        if (e.getCode() == null || e.getCode().isBlank() || e.getName() == null || e.getName().isBlank()) {
            throw new IllegalArgumentException("Mã năm học và Tên năm học không được để trống!");
        }
        validateDates(e.getStartDate(), e.getEndDate());
        return repo.save(e);
    }

    public SchoolYear update(UUID id, SchoolYear updateData) {
        SchoolYear existing = findById(id);
        LocalDate start = updateData.getStartDate() != null ? updateData.getStartDate() : existing.getStartDate();
        LocalDate end = updateData.getEndDate() != null ? updateData.getEndDate() : existing.getEndDate();
        validateDates(start, end);

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
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
