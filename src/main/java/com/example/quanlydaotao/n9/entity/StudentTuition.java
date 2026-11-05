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
import jakarta.persistence.Transient;

/**
 * Học phí của sinh viên theo từng học kỳ.
 * Bảng: student_tuition
 */
@Entity
@Table(name = "student_tuition")
public class StudentTuition {

    public static final int STATUS_PAID = 1;
    public static final int STATUS_PARTIAL = 2;
    public static final int STATUS_DEBT = 3;
    public static final int STATUS_OVERDUE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /** FK -> students_.id */
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    /** FK -> semesters.id */
    @Column(name = "semester_id", nullable = false)
    private UUID semesterId;

    /** FK -> tuition_fees.id */
    @Column(name = "tuition_fee_id", nullable = false)
    private UUID tuitionFeeId;

    /** Tổng số tín chỉ sinh viên đăng ký trong kỳ */
    @Column(name = "total_credits")
    private Integer totalCredits;

    /** Tổng tiền gốc (tín chỉ x đơn giá + học phí cố định) */
    @Column(name = "raw_amount", precision = 15, scale = 2)
    private BigDecimal rawAmount;

    /** Số tiền được trừ từ học bổng */
    @Column(name = "scholarship_deduction", precision = 15, scale = 2)
    private BigDecimal scholarshipDeduction;

    /** Số tiền được miễn giảm chính sách */
    @Column(name = "exemption_amount", precision = 15, scale = 2)
    private BigDecimal exemptionAmount;

    /** Số tiền thực nộp (gốc - miễn giảm - học bổng) */
    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;

    /** Tổng số tiền sinh viên đã nộp */
    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    /** Số tiền còn nợ */
    @Column(name = "debt_amount", precision = 15, scale = 2)
    private BigDecimal debtAmount;

    /** Trạng thái: 1-PAID, 2-PARTIAL, 3-DEBT, 4-OVERDUE */
    @Column(name = "status")
    private Integer status;

    /** Hạn chót hoàn thành học phí */
    @Column(name = "deadline")
    private LocalDate deadline;

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

    /** Tên trạng thái dùng cho giao diện */
    @Transient
    public String getStatusName() {
        return statusName(status);
    }

    public static String statusName(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case STATUS_PAID:
                return "PAID";
            case STATUS_PARTIAL:
                return "PARTIAL";
            case STATUS_DEBT:
                return "DEBT";
            case STATUS_OVERDUE:
                return "OVERDUE";
            default:
                return "";
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(UUID semesterId) {
        this.semesterId = semesterId;
    }

    public UUID getTuitionFeeId() {
        return tuitionFeeId;
    }

    public void setTuitionFeeId(UUID tuitionFeeId) {
        this.tuitionFeeId = tuitionFeeId;
    }

    public Integer getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Integer totalCredits) {
        this.totalCredits = totalCredits;
    }

    public BigDecimal getRawAmount() {
        return rawAmount;
    }

    public void setRawAmount(BigDecimal rawAmount) {
        this.rawAmount = rawAmount;
    }

    public BigDecimal getScholarshipDeduction() {
        return scholarshipDeduction;
    }

    public void setScholarshipDeduction(BigDecimal scholarshipDeduction) {
        this.scholarshipDeduction = scholarshipDeduction;
    }

    public BigDecimal getExemptionAmount() {
        return exemptionAmount;
    }

    public void setExemptionAmount(BigDecimal exemptionAmount) {
        this.exemptionAmount = exemptionAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getDebtAmount() {
        return debtAmount;
    }

    public void setDebtAmount(BigDecimal debtAmount) {
        this.debtAmount = debtAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
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
