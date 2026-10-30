package com.example.quanlydaotao.n7.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        UUID courseSectionId,
        String courseSectionCode,
        String courseSectionName,
        String courseCode,
        String courseName,
        UUID roomId,
        String roomCode,
        String roomName,
        UUID buildingId,
        String buildingCode,
        String buildingName,
        UUID employeeId,
        Integer dayOfWeek,
        UUID dateId,
        UUID shiftId,
        Integer startPeriod,
        Integer endPeriod,
        Integer numberOfPeriods,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String mode,
        String status,
        String description,
        String scheduleStatus,
        String note,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
