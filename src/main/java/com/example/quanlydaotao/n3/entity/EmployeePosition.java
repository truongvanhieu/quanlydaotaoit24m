
package com.example.quanlydaotao.n3.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "N3EmployeePosition")
@Table(name = "employee_positions", schema = "dbo")
public class EmployeePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "position_id")
    private UUID positionId;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "is_active")
    private Boolean isActive;

    public EmployeePosition() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void copyBusinessFields(EmployeePosition source) {
        this.employeeId = source.employeeId;
        this.positionId = source.positionId;
        this.description = source.description;
        this.note = source.note;
        this.startDate = source.startDate;
        this.endDate = source.endDate;
        this.isActive = source.isActive;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID value) {
        this.employeeId = value;
    }

    public UUID getPositionId() { return positionId; }
    public void setPositionId(UUID value) {
        this.positionId = value;
    }

    public String getDescription() { return description; }
    public void setDescription(String value) {
        this.description = value;
    }

    public String getNote() { return note; }
    public void setNote(String value) {
        this.note = value;
    }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime value) {
        this.startDate = value;
    }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime value) {
        this.endDate = value;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) {
        this.createdAt = value;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) {
        this.updatedAt = value;
    }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID value) {
        this.createdBy = value;
    }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID value) {
        this.updatedBy = value;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime value) {
        this.deletedAt = value;
    }

    public UUID getDeletedBy() { return deletedBy; }
    public void setDeletedBy(UUID value) {
        this.deletedBy = value;
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }
}
