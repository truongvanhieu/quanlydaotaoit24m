
package com.example.quanlydaotao.n3.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lecturers")
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lecturer_code")
    private String lecturerCode;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "position_id")
    private UUID positionId;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @PrePersist
    public void beforeInsert() {
        createdAt = LocalDateTime.now();
        isActive = true;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID value) { id = value; }

    public String getLecturerCode() { return lecturerCode; }
    public void setLecturerCode(String value) {
        lecturerCode = value;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String value) {
        fullName = value;
    }

    public String getGender() { return gender; }
    public void setGender(String value) { gender = value; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate value) {
        dateOfBirth = value;
    }

    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }

    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }

    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID value) {
        departmentId = value;
    }

    public UUID getPositionId() { return positionId; }
    public void setPositionId(UUID value) {
        positionId = value;
    }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate value) {
        hireDate = value;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) {
        createdAt = value;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) {
        updatedAt = value;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime value) {
        deletedAt = value;
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean value) {
        isActive = value;
    }
}
