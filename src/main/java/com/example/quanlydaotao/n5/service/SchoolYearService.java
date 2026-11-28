package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolYearService {

    private static final Pattern START_YEAR_PATTERN = Pattern.compile("(?:19|20)\\d{2}");

    private final SchoolYearRepository repository;
    private final SemesterRepository semesterRepository;

    public List<SchoolYear> getAll(String keyword, Boolean isActive) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.search(normalizedKeyword, isActive).stream()
                .sorted(Comparator.comparingInt(this::getStartYear)
                        .thenComparing(SchoolYear::getId, Comparator.nullsLast(UUID::compareTo)))
                .toList();
    }

    private int getStartYear(SchoolYear schoolYear) {
        Matcher matcher = START_YEAR_PATTERN.matcher(
                schoolYear.getName() == null ? "" : schoolYear.getName());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return schoolYear.getStartDate() == null ? Integer.MAX_VALUE : schoolYear.getStartDate().getYear();
    }

    public SchoolYear getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy năm học"));
    }

    public SchoolYear create(SchoolYear schoolYear) {
        validate(schoolYear);
        if (repository.existsByCode(schoolYear.getCode().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã năm học đã tồn tại");
        }
        schoolYear.setCode(schoolYear.getCode().trim());
        schoolYear.setName(schoolYear.getName().trim());
        schoolYear.setCreatedAt(java.time.LocalDateTime.now());
        if (schoolYear.getIsActive() == null) {
            schoolYear.setIsActive(true);
        }
        return repository.save(schoolYear);
    }

    public SchoolYear update(UUID id, SchoolYear request) {
        SchoolYear existing = getById(id);
        validate(request);
        String code = request.getCode().trim();
        if (!code.equals(existing.getCode()) && repository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã năm học đã tồn tại");
        }
        existing.setCode(code);
        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription());
        existing.setNote(request.getNote());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        return repository.save(existing);
    }

    private void validate(SchoolYear schoolYear) {
        if (schoolYear.getCode() == null || schoolYear.getCode().isBlank()
                || schoolYear.getName() == null || schoolYear.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã và tên năm học là bắt buộc");
        }
        if (schoolYear.getStartDate() != null && schoolYear.getEndDate() != null
                && schoolYear.getStartDate().isAfter(schoolYear.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu không được sau ngày kết thúc");
        }
    }

    public void delete(UUID id) {
        SchoolYear existing = getById(id);
        if (semesterRepository.existsBySchoolYearId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể xóa năm học đang có học kỳ");
        }
        repository.delete(existing);
    }

    public void updateStatus(UUID id, Boolean isActive) {
        if (isActive == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái hoạt động là bắt buộc");
        }
        SchoolYear existing = getById(id);
        existing.setIsActive(isActive);
        repository.save(existing);
    }
}