package com.example.quanlydaotao.n1.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n1.dto.PermissionResponse;
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
}
