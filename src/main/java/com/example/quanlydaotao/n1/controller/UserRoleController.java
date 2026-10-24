
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n1.dto.AssignRoleRequest;
import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.entity.UserRole;
import com.example.quanlydaotao.n1.service.UserRoleService;
@RestController
@RequestMapping("/api/users")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @GetMapping("/{userId}/roles")
    public List<Role> getRolesByUserId(@PathVariable UUID userId) {
        return userRoleService.getRolesByUserId(userId);
    }

    
    @PostMapping("/{userId}/roles")
    public UserRole assignRole(
        @PathVariable UUID userId,
        @RequestBody AssignRoleRequest request
    ) {
        try {
            return userRoleService.assignRole(userId, request.getRoleId());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    
    @DeleteMapping("/{userId}/roles/{roleId}")
    public UserRole removeRole(
        @PathVariable UUID userId,
        @PathVariable UUID roleId) {
    try {
            return userRoleService.removeRole(userId, roleId);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
