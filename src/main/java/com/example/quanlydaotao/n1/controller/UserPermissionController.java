package com.example.quanlydaotao.n1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n1.dto.AssignPermissionRequest;
import com.example.quanlydaotao.n1.dto.PermissionResponse;
import com.example.quanlydaotao.n1.entity.UserPermission;
import com.example.quanlydaotao.n1.security.RequirePermission;
import com.example.quanlydaotao.n1.service.UserPermissionService;

@RestController
@RequestMapping("/api/users")
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    public UserPermissionController(
            UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    @GetMapping("/{userId}/permissions")
    public List<PermissionResponse> getPermissionsByUserId(
            @PathVariable UUID userId) {
        return userPermissionService.getPermissionsByUserId(userId);
    }

    @GetMapping("/{userId}/direct-permissions")
    @RequirePermission("ROLE_MANAGE")
    public List<PermissionResponse> getDirectPermissionsByUserId(
            @PathVariable UUID userId) {
        return userPermissionService.getDirectPermissionsByUserId(userId);
    }

    @PostMapping("/{userId}/permissions")
    @RequirePermission("ROLE_MANAGE")
    @ResponseStatus(HttpStatus.CREATED)
    public UserPermission assignPermission(
            @PathVariable UUID userId,
            @RequestBody AssignPermissionRequest request) {
        try {
            return userPermissionService.assignPermission(userId, request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{userId}/permissions/{permissionId}")
    @RequirePermission("ROLE_MANAGE")
    public UserPermission removePermission(
            @PathVariable UUID userId,
            @PathVariable UUID permissionId) {
        try {
            return userPermissionService.removePermission(userId, permissionId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
