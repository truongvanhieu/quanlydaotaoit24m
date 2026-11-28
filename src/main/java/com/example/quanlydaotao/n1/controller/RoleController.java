package com.example.quanlydaotao.n1.controller;

import java.util.List;
<<<<<<< HEAD
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

import com.example.quanlydaotao.n1.dto.RoleRequest;
import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.security.RequirePermission;
=======

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n1.entity.Role;
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
import com.example.quanlydaotao.n1.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllActiveRoles();
    }
<<<<<<< HEAD

    @GetMapping("/manage")
    @RequirePermission("ROLE_MANAGE")
    public List<Role> getRolesForManagement() {
        return roleService.getRolesForManagement();
    }

    @PostMapping
    @RequirePermission("ROLE_MANAGE")
    @ResponseStatus(HttpStatus.CREATED)
    public Role createRole(@RequestBody RoleRequest request) {
        try {
            return roleService.createRole(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping("/{roleId}")
    @RequirePermission("ROLE_MANAGE")
    public Role updateRole(
            @PathVariable UUID roleId,
            @RequestBody RoleRequest request) {
        try {
            return roleService.updateRole(roleId, request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{roleId}")
    @RequirePermission("ROLE_MANAGE")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable UUID roleId) {
        try {
            roleService.deleteRole(roleId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
}
