package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dữ liệu tổng hợp cho trang tổng quan học phí.
 */
public record DashboardStats(
        TuitionSummary summary,
        List<MonthlyRevenue> monthlyRevenue,
        List<PaymentMethodStat> paymentMethods,
        List<SemesterDebtReport> debtBySemester) {

    public record MonthlyRevenue(String label, BigDecimal amount) {
    }

    public record PaymentMethodStat(String methodName, long count, BigDecimal amount) {
    }
}
