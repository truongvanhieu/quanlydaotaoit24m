package com.example.quanlydaotao.n7.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_sections")
public class CourseSection {

    @Id
    private UUID id;

    @Column(length = 100)
    private String code;

    @Column(length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Course getCourse() {
        return course;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
