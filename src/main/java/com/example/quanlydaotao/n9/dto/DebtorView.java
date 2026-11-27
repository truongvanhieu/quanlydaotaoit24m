package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Khoản học phí còn nợ kèm thông tin liên hệ để nhắc nợ.
 */
public record DebtorView(
        UUID studentTuitionId,
        UUID studentId,
        String studentCode,
        String studentName,
        String email,
        String phone,
        String semesterName,
        BigDecimal netAmount,
        BigDecimal paidAmount,
        BigDecimal debtAmount,
        LocalDate deadline,
        Long daysOverdue,
        Integer status,
        String statusName) {
}
