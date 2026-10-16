
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Lecturer;
import com.example.quanlydaotao.n3.entity.Position;
import com.example.quanlydaotao.n3.repository.LecturerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public Lecturer create(Lecturer lecturer) {
        validate(lecturer);

        lecturer.setId(null);
        lecturer.setCreatedAt(null);
        lecturer.setDeletedAt(null);
        lecturer.setIsActive(true);

        return repository.save(lecturer);
    }

    public Lecturer update(UUID id, Lecturer data) {
        validate(data);

        Lecturer old = getById(id);

        old.setLecturerCode(data.getLecturerCode());
        old.setFullName(data.getFullName());
        old.setGender(data.getGender());
        old.setDateOfBirth(data.getDateOfBirth());
        old.setEmail(data.getEmail());
        old.setPhone(data.getPhone());
        old.setDepartmentId(data.getDepartmentId());
        old.setPositionId(data.getPositionId());
        old.setHireDate(data.getHireDate());

        return repository.save(old);
    }

    public void delete(UUID id) {
        Lecturer lecturer = getById(id);
        lecturer.setDeletedAt(LocalDateTime.now());
        lecturer.setIsActive(false);

        repository.save(lecturer);
    }
}
