package com.example.quanlydaotao.n7.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.example.quanlydaotao.n7.dto.request.BuildingRequest;
import com.example.quanlydaotao.n7.dto.response.BuildingResponse;
import com.example.quanlydaotao.n7.dto.response.RoomResponse;
import com.example.quanlydaotao.n7.service.BuildingService;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping
    public Page<BuildingResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        return buildingService.list(keyword, active, pageable);
    }

    @GetMapping("/{id}")
    public BuildingResponse get(@PathVariable UUID id) {
        return buildingService.get(id);
    }

    @GetMapping("/{id}/rooms")
    public List<RoomResponse> rooms(@PathVariable UUID id) {
        return buildingService.rooms(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuildingResponse create(@RequestBody BuildingRequest request) {
        return buildingService.create(request);
    }

    @PutMapping("/{id}")
    public BuildingResponse update(@PathVariable UUID id, @RequestBody BuildingRequest request) {
        return buildingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        buildingService.delete(id);
    }
}
