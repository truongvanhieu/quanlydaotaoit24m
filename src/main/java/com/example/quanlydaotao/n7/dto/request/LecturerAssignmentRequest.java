package com.example.quanlydaotao.n7.dto.request;

import java.util.UUID;

public record LecturerAssignmentRequest(UUID courseClassId, UUID lecturerId, String role) {}
