package com.example.quanlydaotao.n2.grade;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class GradeModels {

    private GradeModels() {
    }

    public record CourseClassOption(UUID id, String classCode, String courseCode, String courseName) {
    }

    public record GradeComponentView(
            UUID id,
            UUID courseClassId,
            String componentCode,
            String componentName,
            BigDecimal weightPercentage,
            BigDecimal minScore,
            BigDecimal maxScore,
            boolean required,
            Integer inputOrder) {
    }

    public record GradeComponentRequest(
            UUID courseClassId,
            String componentCode,
            String componentName,
            BigDecimal weightPercentage,
            BigDecimal minScore,
            BigDecimal maxScore,
            Boolean required,
            Integer inputOrder) {
    }

    public record GradeEntryRequest(
            UUID registrationId,
            UUID componentId,
            BigDecimal score,
            Boolean retake,
            String note) {
    }

    public record GradeEntryUpdateRequest(BigDecimal score, Boolean retake, String note) {
    }

    public record GradeEntryView(
            UUID id,
            UUID componentId,
            BigDecimal score,
            boolean retake,
            String note) {
    }

    public record StudentGradeView(
            UUID registrationId,
            String studentCode,
            String studentName,
            List<GradeEntryView> grades,
            BigDecimal totalScore,
            String letterGrade,
            BigDecimal gpaValue,
            String result) {
    }

    public record GradeBookView(
            UUID courseClassId,
            List<GradeComponentView> components,
            List<StudentGradeView> students,
            BigDecimal configuredWeightTotal,
            boolean gradebookReady,
            String statusMessage) {
    }
}
