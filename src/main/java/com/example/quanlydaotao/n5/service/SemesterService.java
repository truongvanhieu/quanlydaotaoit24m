package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        return findAll(null, null, isActive);
    }

    public List<Semester> findAll(String keyword, UUID schoolYearId, Boolean isActive) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return repo.search(kw, schoolYearId, isActive);
    }

    public Semester findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ với ID: " + id));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
            throw new RuntimeException("Ngày bắt đầu học kỳ phải nhỏ hơn ngày kết thúc!");
        }
    }

    public Semester save(Semester e, UUID syId) {
        if (e.getCode() == null || e.getCode().isBlank() || e.getName() == null || e.getName().isBlank()) {
            throw new IllegalArgumentException("Mã học kỳ và Tên học kỳ không được để trống!");
        }
        validateDates(e.getStartDate(), e.getEndDate());
        UUID targetSyId = syId != null ? syId : e.getSchoolYearId();
        if (targetSyId != null) {
            SchoolYear sy = syRepo.findById(targetSyId).orElse(null);
            if (sy != null) {
                e.setSchoolYear(sy);
                e.setSchoolYearName(sy.getName());
            }
        }
        return repo.save(e);
    }

    public Semester update(UUID id, Semester updateData, UUID syId) {
        Semester existing = findById(id);
        LocalDate start = updateData.getStartDate() != null ? updateData.getStartDate() : existing.getStartDate();
        LocalDate end = updateData.getEndDate() != null ? updateData.getEndDate() : existing.getEndDate();
        validateDates(start, end);

        if (updateData.getCode() != null) existing.setCode(updateData.getCode());
        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getSchoolYearName() != null) existing.setSchoolYearName(updateData.getSchoolYearName());
        if (updateData.getStartDate() != null) existing.setStartDate(updateData.getStartDate());
        if (updateData.getEndDate() != null) existing.setEndDate(updateData.getEndDate());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        UUID targetSyId = syId != null ? syId : updateData.getSchoolYearId();
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
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
