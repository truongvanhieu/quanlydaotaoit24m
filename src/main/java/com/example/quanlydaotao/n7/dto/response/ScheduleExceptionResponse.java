package com.example.quanlydaotao.n7.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record ScheduleExceptionResponse(
        UUID id,
        UUID scheduleId,
        LocalDate occurrenceDate,
        String exceptionType,
        LocalDate replacementDate,
        UUID roomId,
        UUID employeeId,
        Integer startPeriod,
        Integer endPeriod,
        String note,
        ScheduleResponse schedule) {
}
