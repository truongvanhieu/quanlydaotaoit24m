package com.example.quanlydaotao.n7.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ScheduleRequest(
        UUID courseSectionId,
        UUID roomId,
        UUID employeeId,
        Integer dayOfWeek,
        List<Integer> daysOfWeek,
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
        Boolean active) {
}
