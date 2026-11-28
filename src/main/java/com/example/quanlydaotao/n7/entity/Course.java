package com.example.quanlydaotao.n7.entity;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(length = 100)
    private String code;

    @Column(length = 255)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    private BigDecimal credits;

    @Column(name = "cource_type")
    private String courseType;

    @Column(name = "theory_hours")
    private BigDecimal theoryHours;

    @Column(name = "practice_hours")
    private BigDecimal practiceHours;

    @Column(name = "self_study_hours")
    private BigDecimal selfStudyHours;

    @Column(name = "internship_credits")
    private BigDecimal internshipCredits;

    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public UUID getDepartmentId() { return departmentId; }
    public String getNameEn() { return nameEn; }
    public BigDecimal getCredits() { return credits; }
    public String getCourseType() { return courseType; }
    public BigDecimal getTheoryHours() { return theoryHours; }
    public BigDecimal getPracticeHours() { return practiceHours; }
    public BigDecimal getSelfStudyHours() { return selfStudyHours; }
    public BigDecimal getInternshipCredits() { return internshipCredits; }
    public String getDescription() { return description; }
    public Boolean getActive() { return active; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public void setCredits(BigDecimal credits) { this.credits = credits; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public void setTheoryHours(BigDecimal theoryHours) { this.theoryHours = theoryHours; }
    public void setPracticeHours(BigDecimal practiceHours) { this.practiceHours = practiceHours; }
    public void setSelfStudyHours(BigDecimal selfStudyHours) { this.selfStudyHours = selfStudyHours; }
    public void setInternshipCredits(BigDecimal internshipCredits) { this.internshipCredits = internshipCredits; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(Boolean active) { this.active = active; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    @jakarta.persistence.PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); if (active == null) active = true; }

    @jakarta.persistence.PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
