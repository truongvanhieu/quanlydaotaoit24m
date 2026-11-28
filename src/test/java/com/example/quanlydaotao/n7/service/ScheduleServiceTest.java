package com.example.quanlydaotao.n7.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.quanlydaotao.n7.dto.response.ShiftOption;

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

    @Test
    void detectsMatchingWeekdayInsideRecurringDateOverlap() {
        assertTrue(ScheduleService.overlapsOnWeekday(
                LocalDateTime.parse("2027-02-01T00:00:00"),
                LocalDateTime.parse("2027-05-02T23:59:59"),
                LocalDateTime.parse("2027-02-08T00:00:00"),
                LocalDateTime.parse("2027-02-08T02:30:00"),
                1));
    }

    @Test
    void ignoresMatchingWeekdayOutsideRecurringDateRange() {
        assertFalse(ScheduleService.overlapsOnWeekday(
                LocalDateTime.parse("2027-02-01T00:00:00"),
                LocalDateTime.parse("2027-05-02T23:59:59"),
                LocalDateTime.parse("2027-01-25T00:00:00"),
                LocalDateTime.parse("2027-01-25T02:30:00"),
                1));
    }

    @Test
    void acceptsPeriodsWithinSelectedShift() {
        assertTrue(ScheduleService.shiftCovers(
                new ShiftOption(UUID.randomUUID(), "Ca 2", 6, 10), 6, 10));
    }

    @Test
    void rejectsPeriodsOutsideSelectedShift() {
        assertFalse(ScheduleService.shiftCovers(
                new ShiftOption(UUID.randomUUID(), "Ca 2", 6, 10), 5, 10));
    }
}
