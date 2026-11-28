package com.example.quanlydaotao.n3.repository;

import com.example.quanlydaotao.n3.entity.N3AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface N3AuditLogRepository extends JpaRepository<N3AuditLog, UUID> {

    List<N3AuditLog> findByDeletedAtIsNullAndTableNameInOrderByCreatedAtDesc(
            Collection<String> tableNames
    );

    List<N3AuditLog> findByDeletedAtIsNullAndRecordIdOrderByCreatedAtDesc(
            UUID recordId
    );
}
