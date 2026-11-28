package com.example.quanlydaotao.n7.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseSectionResponse(
        UUID id, String code, String name, UUID courseId, String courseCode, String courseName,
        UUID semesterId, UUID roomId, UUID buildingId, Integer maxStudents, Integer minStudents,
        String classType, String status, LocalDateTime registrationStart, LocalDateTime registrationEnd,
        String note, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {}
