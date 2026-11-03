package com.example.quanlydaotao.n7.dto.response;

import java.util.UUID;

public record LecturerAssignmentResponse(UUID id, UUID courseClassId, String courseClassCode,
        String courseCode, String courseName,
        UUID lecturerId, String lecturerCode, String lecturerName, String role, Boolean active) {}
