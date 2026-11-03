package com.example.quanlydaotao.n7.dto.response;

import java.util.UUID;

public record CourseClassResponse(UUID id, String classCode, UUID courseId, String courseCode,
        String courseName, UUID lecturerId, String lecturerName, String status) {}
