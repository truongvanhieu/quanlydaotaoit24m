package com.example.quanlydaotao.n6.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_registrations", schema = "dbo")
public class CourseRegistration {
    @Id
    public UUID id;
    @Column(name = "student_id", nullable = false)
    public UUID studentId;
    @Column(name = "course_class_id", nullable = false)
    public UUID courseClassId;
    @Column(name = "registration_period_id")
    public UUID registrationPeriodId;
    @Column(name = "registration_type")
    public Short registrationType;
    @Column(name = "replaced_grade_id")
    public UUID replacedGradeId;
    @Column(name = "registered_at", nullable = false)
    public LocalDateTime registeredAt;
    @Column(name = "status")
    public Short status;
    @Column(name = "is_paid", nullable = false)
    public boolean paid;
    @Column(name = "row_version", insertable = false, updatable = false)
    public byte[] rowVersion;
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
        if (registeredAt == null) registeredAt = LocalDateTime.now();
    }
}
