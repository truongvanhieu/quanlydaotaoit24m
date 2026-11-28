package com.example.quanlydaotao.n1.controller;

import java.util.List;
<<<<<<< HEAD
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n1.dto.AssignPermissionRequest;
import com.example.quanlydaotao.n1.dto.PermissionResponse;
import com.example.quanlydaotao.n1.entity.RolePermission;
import com.example.quanlydaotao.n1.security.RequirePermission;
=======

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n1.dto.PermissionResponse;
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
import com.example.quanlydaotao.n1.service.RolePermissionService;

@RestController
@RequestMapping("/api/roles")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(
            RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping("/{roleCode}/permissions")
    public List<PermissionResponse> getPermissionsByRole(
            @PathVariable String roleCode) {
        return rolePermissionService.getPermissionsByRoleCode(roleCode);
    }
<<<<<<< HEAD

    @GetMapping("/manage/{roleCode}/permissions")
    @RequirePermission("ROLE_MANAGE")
    public List<PermissionResponse> getPermissionsByRoleForManagement(
            @PathVariable String roleCode) {
        return rolePermissionService.getPermissionsByRoleCode(roleCode);
    }

    @PostMapping("/{roleId}/permissions")
    @RequirePermission("ROLE_MANAGE")
    public RolePermission assignPermission(
            @PathVariable UUID roleId,
            @RequestBody AssignPermissionRequest request) {
        try {
            if (request.getPermissionId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn quyền cần gán");
            }
            return rolePermissionService.assignPermission(roleId, request.getPermissionId());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @RequirePermission("ROLE_MANAGE")
    public RolePermission removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {
        try {
            return rolePermissionService.removePermission(roleId, permissionId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
}
