package com.example.quanlydaotao.n9.entity;

import java.math.BigDecimal;
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
 * Lịch sử giao dịch thanh toán học phí.
 * Bảng: payments
 */
@Entity
@Table(name = "payments")
public class Payment {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /** FK -> student_tuition.id */
    @Column(name = "tuition_id", nullable = false)
    private UUID tuitionId;

    /** Số tiền nộp trong lần giao dịch này */
    @Column(name = "amount_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid;

    /** Ngày giờ thực tế thực hiện giao dịch */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    /** 1-Bank Transfer, 2-Cash, 3-E-wallet */
    @Column(name = "payment_method")
    private Integer paymentMethod;

    /** PENDING, SUCCESS, FAILED, REFUNDED, CANCELLED */
    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    /** Mã tham chiếu / đối soát ngân hàng */
    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    /** Người thu tiền hoặc hệ thống tự động */
    @Column(name = "cashier_id")
    private UUID cashierId;

    /** Ghi chú thêm (NVARCHAR(MAX) trong CSDL) */
    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    /** 1: Hợp lệ, 0: Đã hủy */
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

    /** Thời điểm hủy / xóa giao dịch */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Người thực hiện hủy */
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
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
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

    public UUID getTuitionId() {
        return tuitionId;
    }

    public void setTuitionId(UUID tuitionId) {
        this.tuitionId = tuitionId;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public UUID getCashierId() {
        return cashierId;
    }

    public void setCashierId(UUID cashierId) {
        this.cashierId = cashierId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
