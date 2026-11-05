package com.example.quanlydaotao.n9.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Định mức học phí theo chương trình đào tạo và khóa học.
 * Bảng: tuition_fees
 */
@Entity
@Table(name = "tuition_fees")
public class TuitionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /** FK -> training_programs.id */
    @Column(name = "program_id", nullable = false)
    private UUID programId;

    /** Khóa học, ví dụ: K23, K24 */
    @Column(name = "course_year", length = 10)
    private String courseYear;

    /** Đơn giá cho mỗi tín chỉ */
    @Column(name = "price_per_credit", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerCredit;

    /** Học phí cố định mỗi kỳ (nếu có) */
    @Column(name = "base_tuition", precision = 15, scale = 2)
    private BigDecimal baseTuition;

    /** Ngày bắt đầu áp dụng mức phí */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    /** Trạng thái hiệu lực */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    /** Thời điểm xóa mềm */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Người thực hiện xóa */
    @Column(name = "deleted_by")
    private UUID deletedBy;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProgramId() {
        return programId;
    }

    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    public String getCourseYear() {
        return courseYear;
    }

    public void setCourseYear(String courseYear) {
        this.courseYear = courseYear;
    }

    public BigDecimal getPricePerCredit() {
        return pricePerCredit;
    }

    public void setPricePerCredit(BigDecimal pricePerCredit) {
        this.pricePerCredit = pricePerCredit;
    }

    public BigDecimal getBaseTuition() {
        return baseTuition;
    }

    public void setBaseTuition(BigDecimal baseTuition) {
        this.baseTuition = baseTuition;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public UUID getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(UUID deletedBy) {
        this.deletedBy = deletedBy;
    }
}
