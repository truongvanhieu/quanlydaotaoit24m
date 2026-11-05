package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dữ liệu hiển thị của học phí sinh viên — kèm sẵn mã/tên sinh viên,
 * tên học kỳ và tên chương trình đào tạo.
 */
public record StudentTuitionView(
        UUID id,
        UUID studentId,
        String studentCode,
        String studentName,
        UUID semesterId,
        String semesterName,
        UUID tuitionFeeId,
        String programName,
        String courseYear,
        BigDecimal pricePerCredit,
        BigDecimal baseTuition,
        Integer totalCredits,
        BigDecimal rawAmount,
        BigDecimal scholarshipDeduction,
        BigDecimal exemptionAmount,
        BigDecimal netAmount,
        BigDecimal paidAmount,
        BigDecimal debtAmount,
        Integer status,
        String statusName,
        LocalDate deadline,
        Boolean isActive) {
}
