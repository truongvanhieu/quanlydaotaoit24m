package com.example.quanlydaotao.n1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n1.dto.CreatePermissionRequest;
import com.example.quanlydaotao.n1.dto.PermissionUsageResponse;
import com.example.quanlydaotao.n1.entity.Permission;
import com.example.quanlydaotao.n1.security.RequirePermission;
import com.example.quanlydaotao.n1.service.PermissionService;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionService.getAllActivePermissions();
    }

    @GetMapping("/manage")
    @RequirePermission("ROLE_MANAGE")
    public List<Permission> getPermissionsForManagement() {
        return permissionService.getAllActivePermissions();
    }

    @PostMapping("/manage")
    @RequirePermission("ROLE_MANAGE")
    @ResponseStatus(HttpStatus.CREATED)
    public Permission createPermission(@RequestBody CreatePermissionRequest request) {
        try {
            return permissionService.createPermission(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/manage/{permissionId}/usage")
    @RequirePermission("ROLE_MANAGE")
    public PermissionUsageResponse getPermissionUsage(
            @PathVariable UUID permissionId) {
        try {
            return permissionService.getPermissionUsage(permissionId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping("/manage/{permissionId}")
    @RequirePermission("ROLE_MANAGE")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivatePermission(@PathVariable UUID permissionId) {
        try {
            permissionService.deactivatePermission(permissionId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
