package com.example.quanlydaotao.n6.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_equivalences", schema = "dbo")
public class CourseEquivalence {
    @Id
    public UUID id;
    @Column(name = "original_course_id", nullable = false)
    public UUID originalCourseId;
    @Column(name = "equivalent_course_id", nullable = false)
    public UUID equivalentCourseId;
    @Column(name = "equivalence_type")
    public Short equivalenceType;
    @Column(name = "effect_date")
    public LocalDate effectDate;
    @Column(name = "is_active", nullable = false)
    public boolean active;
    @Column(name = "note", length = 500)
    public String note;
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
