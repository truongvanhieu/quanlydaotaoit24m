package com.example.quanlydaotao.n7.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n7.dto.request.ScheduleExceptionRequest;
import com.example.quanlydaotao.n7.dto.response.ScheduleExceptionResponse;
import com.example.quanlydaotao.n7.service.ScheduleExceptionService;

@RestController
@RequestMapping("/api")
public class ScheduleExceptionController {

    private final ScheduleExceptionService service;

    public ScheduleExceptionController(ScheduleExceptionService service) {
        this.service = service;
    }

    @GetMapping("/schedule-exceptions")
    public List<ScheduleExceptionResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return service.list(fromDate, toDate);
    }

    @PostMapping("/schedules/{scheduleId}/exceptions")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleExceptionResponse save(
            @PathVariable UUID scheduleId, @RequestBody ScheduleExceptionRequest request) {
        return service.save(scheduleId, request);
    }

    @DeleteMapping("/schedule-exceptions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
