
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Lecturer;
import com.example.quanlydaotao.n3.entity.Position;
import com.example.quanlydaotao.n3.repository.LecturerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LecturerService {

    private final LecturerRepository repository;
    private final DepartmentService departmentService;
    private final PositionService positionService;

    public LecturerService(
            LecturerRepository repository,
            DepartmentService departmentService,
            PositionService positionService) {
        this.repository = repository;
        this.departmentService = departmentService;
        this.positionService = positionService;
    }

    public List<Lecturer> getAll(String keyword) {
        String q = keyword == null ? ""
                : keyword.trim().toLowerCase();

        return repository.findAll().stream()
                .filter(l -> l.getDeletedAt() == null)
                .filter(l -> q.isEmpty()
                        || contains(l.getLecturerCode(), q)
                        || contains(l.getFullName(), q)
                        || contains(l.getEmail(), q))
                .toList();
    }

    private boolean contains(String text, String q) {
        return text != null && text.toLowerCase().contains(q);
    }

    public Lecturer getById(UUID id) {
        Lecturer lecturer = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));

        if (lecturer.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Giảng viên đã bị xóa");
        }

        return lecturer;
    }

    private void validate(Lecturer lecturer) {
        if (lecturer.getLecturerCode() == null
                || lecturer.getLecturerCode().isBlank()
                || lecturer.getFullName() == null
                || lecturer.getFullName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã giảng viên và họ tên là bắt buộc");
        }

        if (lecturer.getLecturerCode().trim().length() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã giảng viên tối đa 20 ký tự");
        }

        if (lecturer.getFullName().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Họ tên giảng viên tối đa 100 ký tự");
        }

        if (lecturer.getDateOfBirth() != null
                && lecturer.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngày sinh không hợp lệ");
        }

        if (lecturer.getEmail() != null && !lecturer.getEmail().isBlank()) {
            String email = lecturer.getEmail().trim();
            if (email.length() > 100 || !email.contains("@")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Email không hợp lệ");
            }
        }

        if (lecturer.getPhone() != null
                && lecturer.getPhone().trim().length() > 15) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số điện thoại tối đa 15 ký tự");
        }

        if (lecturer.getDepartmentId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng chọn khoa");
        }

        departmentService.getById(lecturer.getDepartmentId());

        if (lecturer.getPositionId() != null) {
            Position p = positionService.getById(
                    lecturer.getPositionId());

            if (p.getDepartmentId() != null
                    && !p.getDepartmentId().equals(
                            lecturer.getDepartmentId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Chức danh không thuộc khoa đã chọn");
            }
        }
    }

    private void ensureUniqueCode(String code, UUID excludeId) {
        boolean duplicated = repository.findAll().stream()
                .filter(item -> item.getDeletedAt() == null)
                .filter(item -> excludeId == null || !excludeId.equals(item.getId()))
                .anyMatch(item -> item.getLecturerCode() != null
                        && item.getLecturerCode().trim().equalsIgnoreCase(code));

        if (duplicated) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã giảng viên đã tồn tại");
        }
    }

    public Lecturer create(Lecturer lecturer) {
        validate(lecturer);
        String code = lecturer.getLecturerCode().trim();
        ensureUniqueCode(code, null);

        lecturer.setId(null);
        lecturer.setLecturerCode(code);
        lecturer.setFullName(lecturer.getFullName().trim());
        lecturer.setCreatedAt(null);
        lecturer.setDeletedAt(null);
        if (lecturer.getIsActive() == null) {
            lecturer.setIsActive(true);
        }

        return repository.save(lecturer);
    }

    public Lecturer update(UUID id, Lecturer data) {
        validate(data);

        Lecturer old = getById(id);
        String code = data.getLecturerCode().trim();
        ensureUniqueCode(code, id);

        old.setLecturerCode(code);
        old.setFullName(data.getFullName().trim());
        old.setGender(data.getGender());
        old.setDateOfBirth(data.getDateOfBirth());
        old.setEmail(data.getEmail());
        old.setPhone(data.getPhone());
        old.setDepartmentId(data.getDepartmentId());
        old.setPositionId(data.getPositionId());
        old.setHireDate(data.getHireDate());
        if (data.getIsActive() != null) {
            old.setIsActive(data.getIsActive());
        }

        return repository.save(old);
    }

    public void delete(UUID id) {
        Lecturer lecturer = getById(id);
        lecturer.setDeletedAt(LocalDateTime.now());
        lecturer.setIsActive(false);

        repository.save(lecturer);
    }
}
