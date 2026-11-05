package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dữ liệu hiển thị của giao dịch thanh toán — kèm sẵn thông tin sinh viên
 * và học kỳ của khoản thu tương ứng.
 */
public record PaymentView(
        UUID id,
        UUID tuitionId,
        UUID studentId,
        String studentCode,
        String studentName,
        UUID semesterId,
        String semesterName,
        String programName,
        String courseYear,
        BigDecimal amountPaid,
        LocalDateTime paymentDate,
        Integer paymentMethod,
        String paymentMethodName,
        String paymentStatus,
        String transactionRef,
        UUID cashierId,
        String notes,
        Boolean isActive) {
}
