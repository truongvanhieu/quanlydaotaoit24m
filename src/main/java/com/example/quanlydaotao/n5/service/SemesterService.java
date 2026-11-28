package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository repository;
    private final SchoolYearRepository schoolYearRepository;
    private final CourseSectionRepository courseSectionRepository;

    public List<Semester> getAll(String keyword, UUID schoolYearId) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        if (normalizedKeyword != null || schoolYearId != null) {
            return repository.search(normalizedKeyword, schoolYearId);
        }
        return repository.findAllByOrderByStartDateDesc();
    }

    public Semester getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));
    }

    public Semester create(Semester semester) {
        validate(semester);
        if (repository.existsByCode(semester.getCode().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã học kỳ đã tồn tại");
        }
        semester.setCode(semester.getCode().trim());
        semester.setName(semester.getName().trim());
        SchoolYear schoolYear = schoolYearRepository.findById(semester.getSchoolYearId()).orElseThrow();
        semester.setSchoolYearName(schoolYear.getName());
        semester.setCreatedAt(java.time.LocalDateTime.now());
        if (semester.getIsActive() == null) {
            semester.setIsActive(true);
        }
        return repository.save(semester);
    }

    public Semester update(UUID id, Semester request) {
        validate(request);
        Semester existing = getById(id);
        String code = request.getCode().trim();
        if (!code.equals(existing.getCode()) && repository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã học kỳ đã tồn tại");
        }
        existing.setCode(code);
        existing.setName(request.getName().trim());
        existing.setSchoolYearId(request.getSchoolYearId());
        schoolYearRepository.findById(request.getSchoolYearId())
                .ifPresent(schoolYear -> existing.setSchoolYearName(schoolYear.getName()));
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        return repository.save(existing);
    }

    private void validate(Semester semester) {
        if (semester.getCode() == null || semester.getCode().isBlank()
                || semester.getName() == null || semester.getName().isBlank()
                || semester.getSchoolYearId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã, tên học kỳ và năm học là bắt buộc");
        }
        if (!schoolYearRepository.existsById(semester.getSchoolYearId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Năm học không tồn tại");
        }
        if (semester.getStartDate() != null && semester.getEndDate() != null
                && semester.getStartDate().isAfter(semester.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu không được sau ngày kết thúc");
        }
    }

    public void delete(UUID id) {
        Semester existing = getById(id);
        if (courseSectionRepository.existsBySemesterId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Không thể xóa học kỳ đang có lớp học phần");
        }
        repository.delete(existing);
    }
}
