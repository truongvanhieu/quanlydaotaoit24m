package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.N3AuditLog;
import com.example.quanlydaotao.n3.service.N3AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/n3/audit-logs")
public class N3AuditLogController {

    private final N3AuditLogService auditLogService;

    public N3AuditLogController(N3AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<N3AuditLog> getLogs(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword) {
        return auditLogService.getLogs(tableName, action, keyword);
    }

    @GetMapping("/record/{recordId}")
    public List<N3AuditLog> getByRecord(@PathVariable UUID recordId) {
        return auditLogService.getByRecord(recordId);
    }
}
