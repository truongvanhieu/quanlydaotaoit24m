package com.example.quanlydaotao.n2.grade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.quanlydaotao.n2.grade.GradeModels.GradeComponentView;

class GradeCalculatorTest {

    @Test
    void calculatesWeightedTotalAndRoundsToTwoDecimalPlaces() {
        UUID quizId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        List<GradeComponentView> components = List.of(
                component(quizId, "30"),
                component(examId, "70"));

        BigDecimal total = GradeCalculator.calculate(
                components,
                Map.of(quizId, new BigDecimal("8.50"), examId, new BigDecimal("6.75"))).orElseThrow();

        assertEquals(new BigDecimal("7.28"), total);
    }

    @Test
    void doesNotCalculateWhenAnyComponentIsMissing() {
        UUID quizId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();

        assertFalse(GradeCalculator.calculate(
                List.of(component(quizId, "30"), component(examId, "70")),
                Map.of(quizId, new BigDecimal("8.50"))).isPresent());
    }

    @Test
    void doesNotCalculateWhenConfiguredWeightsDoNotEqualOneHundredPercent() {
        UUID quizId = UUID.randomUUID();

        assertFalse(GradeCalculator.calculate(
                List.of(component(quizId, "80")),
                Map.of(quizId, new BigDecimal("8.50"))).isPresent());
    }

    private GradeComponentView component(UUID id, String weight) {
        return new GradeComponentView(
                id,
                UUID.randomUUID(),
                "COMP",
                "Component",
                new BigDecimal(weight),
                BigDecimal.ZERO,
                new BigDecimal("10"),
                true,
                1);
    }
}
