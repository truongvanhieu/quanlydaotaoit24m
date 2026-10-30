
package com.example.quanlydaotao.n6.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrationSummaryResponse(
        UUID studentId,
        UUID registrationPeriodId,
        int registrationCount,
        BigDecimal selectedCredits,
        int minCredits,
        int maxCredits,
        BigDecimal creditsRemaining,
        boolean minimumReached,
        boolean maximumExceeded) {
}
