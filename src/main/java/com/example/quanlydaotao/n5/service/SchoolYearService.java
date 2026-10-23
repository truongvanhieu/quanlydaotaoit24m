package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolYearService {

    private final SchoolYearRepository repository;

    public List<SchoolYear> getAll(String keyword, Boolean isActive) {
        if (keyword != null || isActive != null) {
            return repository.search(keyword, isActive);
        }
        return repository.findAllByDeletedAtIsNull();
    }

    public SchoolYear getById(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy năm học"));
    }

    public SchoolYear create(SchoolYear schoolYear) {
        if (schoolYear.getCode() != null && repository.existsByCodeAndDeletedAtIsNull(schoolYear.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã năm học đã tồn tại");
        }
        return repository.save(schoolYear);
    }

    public SchoolYear update(UUID id, SchoolYear request) {
        SchoolYear existing = getById(id);

        existing.setCode(request.getCode());
        existing.setName(request.getName());
        existing.setSchoolYearId(request.getSchoolYearId());
        existing.setSchoolYearName(request.getSchoolYearName());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setUpdatedBy(request.getUpdatedBy());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        return repository.save(existing);
    }

    public void updateStatus(UUID id, Boolean isActive, UUID updatedBy) {
        SchoolYear existing = getById(id);
        existing.setIsActive(isActive);
        existing.setUpdatedBy(updatedBy);
        repository.save(existing);
    }

    public void softDelete(UUID id, UUID deletedBy) {
        SchoolYear existing = getById(id);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(deletedBy);
        repository.save(existing);
    }
}