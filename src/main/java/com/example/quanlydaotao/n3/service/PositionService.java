
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
                .peek(this::normalizeLevel)
                .filter(p -> q.isEmpty()
                        || contains(p.getCode(), q)
                        || contains(p.getName(), q)
                        || contains(p.getLevel(), q))
                .toList();
    }

    private void normalizeLevel(Position p) {
        if (p.getLevel() != null) {
            String trimmed = p.getLevel().trim();
            p.setLevel(trimmed.isEmpty() ? null : trimmed);
        }
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
        normalizeLevel(p);
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

    private void ensureUniqueCode(String code, UUID excludeId) {
        boolean duplicated = repository.findAll().stream()
                .filter(item -> item.getDeletedAt() == null)
                .filter(item -> excludeId == null || !excludeId.equals(item.getId()))
                .anyMatch(item -> item.getCode() != null
                        && item.getCode().trim().equalsIgnoreCase(code));

        if (duplicated) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mã chức danh đã tồn tại");
        }
    }

    public Position create(Position p) {
        validate(p);
        String code = p.getCode().trim();
        ensureUniqueCode(code, null);

        p.setId(null);
        p.setCode(code);
        p.setName(p.getName().trim());
        normalizeLevel(p);
        p.setCreatedAt(null);
        p.setDeletedAt(null);
        if (p.getIsActive() == null) {
            p.setIsActive(true);
        }

        return repository.save(p);
    }

    public Position update(UUID id, Position data) {
        validate(data);
        Position old = getById(id);
        String code = data.getCode().trim();
        ensureUniqueCode(code, id);

        old.setCode(code);
        old.setName(data.getName().trim());
        old.setDescription(data.getDescription());
        old.setLevel(data.getLevel());
        normalizeLevel(old);
        old.setDepartmentId(data.getDepartmentId());
        if (data.getIsActive() != null) {
            old.setIsActive(data.getIsActive());
        }

        return repository.save(old);
    }

    public void delete(UUID id) {
        Position p = getById(id);
        p.setDeletedAt(LocalDateTime.now());
        p.setIsActive(false);

        repository.save(p);
    }
}
