
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Department;
import com.example.quanlydaotao.n3.repository.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;
    private final N3AuditLogService auditLogService;

    public DepartmentService(
            DepartmentRepository repository,
            N3AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
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
        Department saved = repository.save(d);
        auditLogService.record(
                N3AuditLogService.ACTION_CREATE,
                "departments",
                saved.getId(),
                "Thêm mới Khoa/Viện: " + saved.getCode() + " - " + saved.getName()
        );
        return saved;
    }

    public Department update(UUID id, Department data) {
        validate(data);
        Department old = getById(id);
        String oldSummary = old.getCode() + " - " + old.getName() + " (Trạng thái: " + (Boolean.TRUE.equals(old.getIsActive()) ? "Hoạt động" : "Tạm khóa") + ")";
        String code = data.getCode().trim();
        ensureUniqueCode(code, id);

        old.setCode(code);
        old.setName(data.getName().trim());
        old.setDescription(data.getDescription());
        old.setEstablishedYear(data.getEstablishedYear());
        if (data.getIsActive() != null) {
            old.setIsActive(data.getIsActive());
        }

        Department saved = repository.save(old);
        String newSummary = saved.getCode() + " - " + saved.getName() + " (Trạng thái: " + (Boolean.TRUE.equals(saved.getIsActive()) ? "Hoạt động" : "Tạm khóa") + ")";
        auditLogService.record(
                N3AuditLogService.ACTION_UPDATE,
                "departments",
                saved.getId(),
                "Cập nhật Khoa/Viện: [" + oldSummary + "] -> [" + newSummary + "]"
        );
        return saved;
    }

    public void delete(UUID id) {
        Department d = getById(id);
        d.setDeletedAt(LocalDateTime.now());
        d.setIsActive(false);
        repository.save(d);
        auditLogService.record(
                N3AuditLogService.ACTION_DELETE,
                "departments",
                d.getId(),
                "Xóa mềm Khoa/Viện: " + d.getCode() + " - " + d.getName()
        );
    }

    public List<Department> getDeleted() {
        return repository.findAll().stream()
                .filter(d -> d.getDeletedAt() != null)
                .sorted(Comparator.comparing(Department::getDeletedAt).reversed())
                .toList();
    }

    public Department restore(UUID id) {
        Department d = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy khoa"));
        if (d.getCode() != null && !d.getCode().isBlank()) {
            ensureUniqueCode(d.getCode().trim(), id);
        }
        d.setDeletedAt(null);
        d.setIsActive(true);
        Department saved = repository.save(d);
        auditLogService.record(
                N3AuditLogService.ACTION_RESTORE,
                "departments",
                saved.getId(),
                "Khôi phục Khoa/Viện đã xóa mềm: " + saved.getCode() + " - " + saved.getName()
        );
        return saved;
    }
}
