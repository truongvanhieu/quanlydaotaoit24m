package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;

/**
 * Thống kê tổng quan học phí toàn trường.
 */
public record TuitionSummary(
        long totalRecords,
        long paidCount,
        long debtorCount,
        BigDecimal totalNet,
        BigDecimal totalPaid,
        BigDecimal totalDebt,
        BigDecimal collectionRate
) {
}
