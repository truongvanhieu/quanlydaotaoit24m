package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.service.LookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lookups")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Object>> getAll() {
        return ResponseEntity.ok(lookupService.getAllLookups());
    }
}
