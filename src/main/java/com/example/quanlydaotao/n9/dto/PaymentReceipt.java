package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dữ liệu in biên lai thu tiền học phí.
 */
public record PaymentReceipt(
        UUID paymentId,
        LocalDateTime paymentDate,
        BigDecimal amountPaid,
        Integer paymentMethod,
        String paymentMethodName,
        String paymentStatus,
        String transactionRef,
        String notes,
        String studentCode,
        String studentName,
        String semesterName,
        String programName,
        String courseYear,
        BigDecimal netAmount,
        BigDecimal paidAmount,
        BigDecimal debtAmount,
        Integer tuitionStatus,
        String tuitionStatusName
) {
}
