
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Position;
import com.example.quanlydaotao.n3.repository.PositionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PositionService {

    private final PositionRepository repository;
    private final DepartmentService departmentService;

    public PositionService(
            PositionRepository repository,
            DepartmentService departmentService) {
        this.repository = repository;
        this.departmentService = departmentService;
    }

    public List<Position> getAll(String keyword) {
        String q = keyword == null ? ""
                : keyword.trim().toLowerCase();

        return repository.findAll().stream()
                .filter(p -> p.getDeletedAt() == null)
                .filter(p -> q.isEmpty()
                        || contains(p.getCode(), q)
                        || contains(p.getName(), q))
                .toList();
    }

    private boolean contains(String text, String q) {
        return text != null && text.toLowerCase().contains(q);
    }

    public Position getById(UUID id) {
        Position p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy chức danh"));

        if (p.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Chức danh đã bị xóa");
        }
        return p;
    }

    private void validate(Position p) {
        if (p.getCode() == null || p.getCode().isBlank()
                || p.getName() == null || p.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã chức danh và tên chức danh là bắt buộc");
        }

        if (p.getDepartmentId() != null) {
            departmentService.getById(p.getDepartmentId());
        }
    }

    public Position create(Position p) {
        validate(p);
        p.setId(null);
        p.setCreatedAt(null);
        p.setDeletedAt(null);
        p.setIsActive(true);

        return repository.save(p);
    }

    public Position update(UUID id, Position data) {
        validate(data);
        Position old = getById(id);

        old.setCode(data.getCode());
        old.setName(data.getName());
        old.setDescription(data.getDescription());
        old.setLevel(data.getLevel());
        old.setDepartmentId(data.getDepartmentId());

        return repository.save(old);
    }

    public void delete(UUID id) {
        Position p = getById(id);
        p.setDeletedAt(LocalDateTime.now());
        p.setIsActive(false);

        repository.save(p);
    }
}
