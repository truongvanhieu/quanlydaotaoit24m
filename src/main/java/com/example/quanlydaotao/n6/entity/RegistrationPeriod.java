package com.example.quanlydaotao.n6.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registration_periods", schema = "dbo")
public class RegistrationPeriod {
    @Id
    @Column(name = "id", nullable = false)
    public UUID id;
    @Column(name = "name", nullable = false, length = 200)
    public String name;
    @Column(name = "semester_id", nullable = false)
    public UUID semesterId;
    @Column(name = "start_time", nullable = false)
    public LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    public LocalDateTime endTime;
    @Column(name = "target_config", columnDefinition = "nvarchar(max)")
    public String targetConfig;
    @Column(name = "max_credits")
    public Short maxCredits;
    @Column(name = "min_credits")
    public Short minCredits;
    @Column(name = "allow_retake", nullable = false)
    public boolean allowRetake;
    @Column(name = "is_active", nullable = false)
    public boolean active;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    @Column(name = "created_by")
    public UUID createdBy;
    @Column(name = "updated_by")
    public UUID updatedBy;
    @Column(name = "deleted_by")
    public UUID deletedBy;
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
    @PrePersist
    void create() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
