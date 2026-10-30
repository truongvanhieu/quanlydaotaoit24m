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

    // Lấy danh sách năm học chưa bị xóa
    public List<AcademicYear> getAll() {
        return repository.findByDeletedAtIsNullOrderByStartDateDesc();
    }

    // Lấy danh sách theo trạng thái
    public List<AcademicYear> getAll(Boolean isActive) {
        if (isActive == null) {
            return getAll();
        }

        return repository
                .findByDeletedAtIsNullAndIsActiveOrderByStartDateDesc(
                        isActive);
    }

    // Lấy chi tiết năm học
    public AcademicYear getById(UUID id) {
        AcademicYear academicYear = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy năm học"));

        if (academicYear.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Năm học không tồn tại");
        }

        return academicYear;
    }

    // Thêm năm học
    public AcademicYear create(AcademicYear academicYear) {
        if (academicYear.getStartDate() != null
                && academicYear.getEndDate() != null
                && academicYear.getStartDate()
                    .isAfter(academicYear.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngày bắt đầu không được sau ngày kết thúc");
        }

        if (academicYear.getCode() != null
                && repository.existsByCodeAndDeletedAtIsNull(
                        academicYear.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mã năm học đã tồn tại");
        }

        academicYear.setId(UUID.randomUUID());

        LocalDateTime now = LocalDateTime.now();
        academicYear.setCreatedAt(now);
        academicYear.setUpdatedAt(now);
        academicYear.setDeletedAt(null);
        academicYear.setDeletedBy(null);

        if (academicYear.getIsActive() == null) {
            academicYear.setIsActive(true);
        }

        return repository.save(academicYear);
    }

    // Cập nhật năm học
    public AcademicYear update(UUID id, AcademicYear request) {
        AcademicYear existing = getById(id);

        if (request.getStartDate() != null
                && request.getEndDate() != null
                && request.getStartDate()
                    .isAfter(request.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngày bắt đầu không được sau ngày kết thúc");
        }

        if (request.getCode() != null
                && !request.getCode().equals(existing.getCode())
                && repository.existsByCodeAndDeletedAtIsNull(
                        request.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mã năm học đã tồn tại");
        }

        existing.setCode(request.getCode());
        existing.setName(request.getName());
        existing.setYear(request.getYear());
        existing.setDescription(request.getDescription());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setUpdatedBy(request.getUpdatedBy());
        existing.setUpdatedAt(LocalDateTime.now());

        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        return repository.save(existing);
    }

    // Xóa mềm năm học
    public void delete(UUID id, UUID deletedBy) {
        AcademicYear academicYear = getById(id);

        academicYear.setDeletedAt(LocalDateTime.now());
        academicYear.setDeletedBy(deletedBy);
        academicYear.setUpdatedAt(LocalDateTime.now());

        repository.save(academicYear);
    }

    // Bật hoặc tắt trạng thái năm học
    public AcademicYear updateActive(UUID id, Boolean isActive) {
        AcademicYear academicYear = getById(id);

        academicYear.setIsActive(isActive);
        academicYear.setUpdatedAt(LocalDateTime.now());

        return repository.save(academicYear);
    }
}