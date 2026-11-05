package com.example.quanlydaotao.n9.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dữ liệu hiển thị của định mức học phí — kèm sẵn tên chương trình đào tạo
 * để giao diện không phải hiện dãy mã UUID.
 */
public record TuitionFeeView(
        UUID id,
        UUID programId,
        String programCode,
        String programName,
        String courseYear,
        BigDecimal pricePerCredit,
        BigDecimal baseTuition,
        LocalDate effectiveDate,
        Boolean isActive) {
}
