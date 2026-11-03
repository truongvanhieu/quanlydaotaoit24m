package com.example.quanlydaotao.n7.dto.response;

import java.util.UUID;

public record CourseSectionStudentResponse(
        UUID studentId, String studentCode, String fullName, String email,
        String status, String registrationType) {}
