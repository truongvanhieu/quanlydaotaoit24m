package com.example.quanlydaotao.n7.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n7.dto.request.ScheduleRequest;
import com.example.quanlydaotao.n7.dto.response.ScheduleResponse;
import com.example.quanlydaotao.n7.service.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public Page<ScheduleResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID courseSectionId,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = {"startDate", "startPeriod"}) Pageable pageable) {
        return scheduleService.list(
                keyword, roomId, courseSectionId, dayOfWeek, fromDate, toDate, active, pageable);
    }

    @GetMapping("/weekly")
    public List<ScheduleResponse> weekly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.weekly(date);
    }

    @GetMapping("/course-section/{courseSectionId}")
    public List<ScheduleResponse> byCourseSection(@PathVariable UUID courseSectionId) {
        return scheduleService.byCourseSection(courseSectionId);
    }

    @GetMapping("/{id}")
    public ScheduleResponse get(@PathVariable UUID id) {
        return scheduleService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse create(@RequestBody ScheduleRequest request) {
        return scheduleService.create(request);
    }

    @PutMapping("/{id}")
    public ScheduleResponse update(@PathVariable UUID id, @RequestBody ScheduleRequest request) {
        return scheduleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        scheduleService.delete(id);
    }
}
