package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.N3AuditLog;
import com.example.quanlydaotao.n3.repository.N3AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class N3AuditLogService {

    public static final String ACTION_CREATE = "THEM";
    public static final String ACTION_UPDATE = "CAP_NHAT";
    public static final String ACTION_DELETE = "XOA_MEM";
    public static final String ACTION_RESTORE = "KHOI_PHUC";
    public static final String ACTION_END_TERM = "KET_THUC_NHIEM_KY";

    private static final Set<String> N3_TABLES = Set.of(
            "departments",
            "positions",
            "lecturers",
            "employees",
            "employee_positions",
            "lecturer_positions_history"
    );

    private final N3AuditLogRepository repository;

    public N3AuditLogService(N3AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(String action, String tableName, UUID recordId, String description) {
        try {
            N3AuditLog log = new N3AuditLog();
            log.setAction(action);
            log.setTableName(tableName);
            log.setRecordId(recordId);
            log.setDescription(description);
            log.setCreatedAt(LocalDateTime.now());
            log.setIsActive(true);
            repository.save(log);
        } catch (Exception ignored) {
            // Khong lam gian doan thao tac nghiep vu neu ghi log gap loi
        }
    }

    public List<N3AuditLog> getLogs(String tableName, String action, String keyword) {
        String tbl = tableName == null ? "" : tableName.trim().toLowerCase(Locale.ROOT);
        String act = action == null ? "" : action.trim().toUpperCase(Locale.ROOT);
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        return repository.findByDeletedAtIsNullAndTableNameInOrderByCreatedAtDesc(N3_TABLES)
                .stream()
                .filter(log -> tbl.isEmpty() || tbl.equalsIgnoreCase(log.getTableName()))
                .filter(log -> act.isEmpty() || act.equalsIgnoreCase(log.getAction()))
                .filter(log -> q.isEmpty()
                        || contains(log.getDescription(), q)
                        || contains(log.getTableName(), q)
                        || contains(log.getAction(), q)
                        || (log.getRecordId() != null && log.getRecordId().toString().toLowerCase(Locale.ROOT).contains(q)))
                .toList();
    }

    public List<N3AuditLog> getByRecord(UUID recordId) {
        return repository.findByDeletedAtIsNullAndRecordIdOrderByCreatedAtDesc(recordId);
    }

    private boolean contains(String text, String q) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(q);
    }
}
