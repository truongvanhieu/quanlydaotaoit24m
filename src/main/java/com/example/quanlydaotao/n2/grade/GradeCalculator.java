package com.example.quanlydaotao.n2.grade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.example.quanlydaotao.n2.grade.GradeModels.GradeComponentView;

final class GradeCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private GradeCalculator() {
    }

    static BigDecimal totalWeight(List<GradeComponentView> components) {
        return components.stream()
                .map(GradeComponentView::weightPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static Optional<BigDecimal> calculate(
            List<GradeComponentView> components,
            Map<UUID, BigDecimal> scores) {
        if (components.isEmpty() || totalWeight(components).compareTo(ONE_HUNDRED) != 0) {
            return Optional.empty();
        }

        BigDecimal weightedScore = BigDecimal.ZERO;
        for (GradeComponentView component : components) {
            BigDecimal score = scores.get(component.id());
            if (score == null) {
                return Optional.empty();
            }
            weightedScore = weightedScore.add(
                    score.multiply(component.weightPercentage()).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
        }

        return Optional.of(weightedScore.setScale(2, RoundingMode.HALF_UP));
    }
}
