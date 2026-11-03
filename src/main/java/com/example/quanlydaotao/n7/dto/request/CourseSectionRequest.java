package com.example.quanlydaotao.n7.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseSectionRequest(
        String code,
        String name,
        UUID courseId,
        UUID semesterId,
        UUID roomId,
        UUID buildingId,
        Integer maxStudents,
        Integer minStudents,
        String classType,
        String status,
        LocalDateTime registrationStart,
        LocalDateTime registrationEnd,
        String note,
        Boolean active) {}
