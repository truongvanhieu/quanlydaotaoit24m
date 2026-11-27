package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;

/**
 * Học bổng và miễn giảm áp dụng cho một khoản học phí sinh viên.
 */
public record TuitionAdjustmentRequest(
        BigDecimal scholarshipDeduction,
        BigDecimal exemptionAmount) {
}
