
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Department;
import com.example.quanlydaotao.n3.repository.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public List<Department> getAll(String keyword) {
        String q = keyword == null ? ""
                : keyword.trim().toLowerCase();

        return repository.findAll().stream()
                .filter(d -> d.getDeletedAt() == null)
                .filter(d -> q.isEmpty()
                        || contains(d.getCode(), q)
                        || contains(d.getName(), q))
                .toList();
    }

    private boolean contains(String text, String q) {
        return text != null && text.toLowerCase().contains(q);
    }

    public Department getById(UUID id) {
        Department d = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy khoa"));

        if (d.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Khoa đã bị xóa");
        }
        return d;
    }

    private void validate(Department d) {
        if (d.getCode() == null || d.getCode().isBlank()
                || d.getName() == null || d.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mã khoa và tên khoa là bắt buộc");
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
                    HttpStatus.CONFLICT, "Mã khoa đã tồn tại");
        }
    }

    public Department create(Department d) {
        validate(d);
        String code = d.getCode().trim();
        ensureUniqueCode(code, null);

        d.setId(null);
        d.setCode(code);
        d.setName(d.getName().trim());
        d.setCreatedAt(null);
        d.setDeletedAt(null);
        if (d.getIsActive() == null) {
            d.setIsActive(true);
        }
        return repository.save(d);
    }

    public Department update(UUID id, Department data) {
        validate(data);
        Department old = getById(id);
        String code = data.getCode().trim();
        ensureUniqueCode(code, id);

        old.setCode(code);
        old.setName(data.getName().trim());
        old.setDescription(data.getDescription());
        old.setEstablishedYear(data.getEstablishedYear());
        if (data.getIsActive() != null) {
            old.setIsActive(data.getIsActive());
        }

        return repository.save(old);
    }

    public void delete(UUID id) {
        Department d = getById(id);
        d.setDeletedAt(LocalDateTime.now());
        d.setIsActive(false);
        repository.save(d);
    }
}
