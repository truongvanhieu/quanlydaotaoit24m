package com.example.quanlydaotao.n7.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseRequest(UUID departmentId, String code, String name, String nameEn,
        BigDecimal credits, String courseType, BigDecimal theoryHours, BigDecimal practiceHours,
        BigDecimal selfStudyHours, BigDecimal internshipCredits, String description, Boolean active) {}
