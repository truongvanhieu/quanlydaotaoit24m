package com.example.quanlydaotao.n1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n1.dto.PermissionModuleRequest;
import com.example.quanlydaotao.n1.entity.PermissionModule;
import com.example.quanlydaotao.n1.security.RequirePermission;
import com.example.quanlydaotao.n1.service.PermissionModuleService;

@RestController
@RequestMapping("/api/permission-modules")
public class PermissionModuleController {

    private final PermissionModuleService permissionModuleService;

    public PermissionModuleController(
            PermissionModuleService permissionModuleService) {
        this.permissionModuleService = permissionModuleService;
    }

    @GetMapping
    public List<PermissionModule> getActiveModules() {
        return permissionModuleService.getActiveModules();
    }

    @PostMapping("/manage")
    @RequirePermission("ROLE_MANAGE")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionModule createModule(
            @RequestBody PermissionModuleRequest request) {
        try {
            return permissionModuleService.createModule(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping("/manage/{moduleId}")
    @RequirePermission("ROLE_MANAGE")
    public PermissionModule updateModule(
            @PathVariable UUID moduleId,
            @RequestBody PermissionModuleRequest request) {
        try {
            return permissionModuleService.updateModule(moduleId, request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/manage/{moduleId}")
    @RequirePermission("ROLE_MANAGE")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateModule(@PathVariable UUID moduleId) {
        try {
            permissionModuleService.deactivateModule(moduleId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
