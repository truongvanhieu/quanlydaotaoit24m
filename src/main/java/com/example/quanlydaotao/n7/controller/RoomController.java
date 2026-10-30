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

import com.example.quanlydaotao.n7.dto.request.RoomRequest;
import com.example.quanlydaotao.n7.dto.response.RoomResponse;
import com.example.quanlydaotao.n7.dto.response.ScheduleResponse;
import com.example.quanlydaotao.n7.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Page<RoomResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "roomCode") Pageable pageable) {
        return roomService.list(keyword, buildingId, status, roomType, minCapacity, active, pageable);
    }

    @GetMapping("/available")
    public List<RoomResponse> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer startPeriod,
            @RequestParam Integer endPeriod,
            @RequestParam(required = false) Integer minCapacity) {
        return roomService.available(date, startPeriod, endPeriod, minCapacity);
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable UUID id) {
        return roomService.get(id);
    }

    @GetMapping("/{id}/schedules")
    public List<ScheduleResponse> schedules(@PathVariable UUID id) {
        return roomService.schedules(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@RequestBody RoomRequest request) {
        return roomService.create(request);
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable UUID id, @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        roomService.delete(id);
    }
}
