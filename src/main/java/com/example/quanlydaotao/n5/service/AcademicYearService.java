package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.AcademicYear;
import com.example.quanlydaotao.n5.repository.AcademicYearRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AcademicYearService {

    private final AcademicYearRepository repository;

    public AcademicYearService(AcademicYearRepository repository) {
        this.repository = repository;
    }

    public List<AcademicYear> getAll() {
        return repository.findAllByOrderByStartDateDesc();
    }

    public List<AcademicYear> getAll(Boolean isActive) {
        if (isActive == null) {
            return getAll();
        }
        return repository.findByIsActiveOrderByStartDateDesc(isActive);
    }

    public AcademicYear getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy năm học"));
    }

    public AcademicYear create(AcademicYear academicYear) {
        validate(academicYear);
        if (repository.existsByCode(academicYear.getCode().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mã năm học đã tồn tại");
        }
        academicYear.setCode(academicYear.getCode().trim());
        academicYear.setName(academicYear.getName().trim());
        academicYear.setYear(academicYear.getYear().trim());
        academicYear.setCreatedAt(LocalDateTime.now());
        if (academicYear.getIsActive() == null) {
            academicYear.setIsActive(true);
        }
        return repository.save(academicYear);
    }

    public AcademicYear update(UUID id, AcademicYear request) {
        AcademicYear existing = getById(id);
        validate(request);
        String code = request.getCode().trim();
        if (!code.equals(existing.getCode()) && repository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mã năm học đã tồn tại");
        }
        existing.setCode(code);
        existing.setName(request.getName().trim());
        existing.setYear(request.getYear().trim());
        existing.setDescription(request.getDescription());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setIsActive(request.getIsActive() == null ? existing.getIsActive() : request.getIsActive());
        return repository.save(existing);
    }

    private void validate(AcademicYear academicYear) {
        if (academicYear.getCode() == null || academicYear.getCode().isBlank()
                || academicYear.getName() == null || academicYear.getName().isBlank()
                || academicYear.getYear() == null || academicYear.getYear().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mã, tên và năm đào tạo là bắt buộc");
        }
        if (academicYear.getStartDate() != null && academicYear.getEndDate() != null
                && academicYear.getStartDate().isAfter(academicYear.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ngày bắt đầu không được sau ngày kết thúc");
        }
    }

    public void delete(UUID id) {
        repository.delete(getById(id));
    }

    public AcademicYear updateActive(UUID id, Boolean isActive) {
        if (isActive == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái hoạt động là bắt buộc");
        }
        AcademicYear academicYear = getById(id);
        academicYear.setIsActive(isActive);
        return repository.save(academicYear);
    }
}