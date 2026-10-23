package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository repository;

    public List<Semester> getAll(String keyword, UUID schoolYearId, Boolean isActive) {
        if (keyword != null || schoolYearId != null || isActive != null) {
            return repository.search(keyword, schoolYearId, isActive);
        }
        return repository.findAllByDeletedAtIsNull();
    }

    public Semester getById(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin học kỳ"));
    }

    public Semester create(Semester semester) {
        if (semester.getCode() != null && repository.existsByCodeAndDeletedAtIsNull(semester.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã học kỳ đã tồn tại");
        }
        return repository.save(semester);
    }

    public Semester update(UUID id, Semester request) {
        Semester existing = getById(id);

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
        Semester existing = getById(id);
        existing.setIsActive(isActive);
        existing.setUpdatedBy(updatedBy);
        repository.save(existing);
    }

    public void softDelete(UUID id, UUID deletedBy) {
        Semester existing = getById(id);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(deletedBy);
        repository.save(existing);
    }
}