package com.example.quanlydaotao.n7.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record ScheduleExceptionRequest(
        LocalDate occurrenceDate,
        String exceptionType,
        LocalDate replacementDate,
        UUID roomId,
        UUID employeeId,
        Integer startPeriod,
        Integer endPeriod,
        String note) {
}
