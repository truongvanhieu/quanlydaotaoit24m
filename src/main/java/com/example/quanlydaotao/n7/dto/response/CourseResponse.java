package com.example.quanlydaotao.n7.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CourseResponse(UUID id, UUID departmentId, String code, String name, String nameEn,
        BigDecimal credits, String courseType, BigDecimal theoryHours, BigDecimal practiceHours,
        BigDecimal selfStudyHours, BigDecimal internshipCredits, String description,
        Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {}
