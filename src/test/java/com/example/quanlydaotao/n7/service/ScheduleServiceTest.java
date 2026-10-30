package com.example.quanlydaotao.n7.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

    @Test
    void findsSelectedWeekdayInsideDateRange() {
        assertTrue(ScheduleService.hasWeekdayOccurrence(
                LocalDateTime.parse("2026-10-05T08:00:00"),
                LocalDateTime.parse("2026-10-11T17:00:00"),
                1));
    }

    @Test
    void rejectsWeekdayOutsideDateRange() {
        assertFalse(ScheduleService.hasWeekdayOccurrence(
                LocalDateTime.parse("2026-10-06T08:00:00"),
                LocalDateTime.parse("2026-10-08T17:00:00"),
                1));
    }

    @Test
    void acceptsSingleMatchingDay() {
        assertTrue(ScheduleService.hasWeekdayOccurrence(
                LocalDateTime.parse("2026-10-09T08:00:00"),
                LocalDateTime.parse("2026-10-09T17:00:00"),
                5));
    }
}
