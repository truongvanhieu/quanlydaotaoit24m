
package com.example.quanlydaotao.n6.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrationEligibilityResponse(
        boolean eligible,
        String message,
        UUID suggestedReplacedGradeId,
        BigDecimal courseCredits,
        BigDecimal totalCreditsAfter,
        Integer minCredits,
        Integer maxCredits) {
}
