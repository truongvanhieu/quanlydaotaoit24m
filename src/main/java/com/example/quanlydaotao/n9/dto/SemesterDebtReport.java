package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Báo cáo công nợ học phí tổng hợp theo từng học kỳ.
 */
public record SemesterDebtReport(
        UUID semesterId,
        String semesterName,
        long studentCount,
        long debtorCount,
        BigDecimal totalNet,
        BigDecimal totalPaid,
        BigDecimal totalDebt
) {
}
