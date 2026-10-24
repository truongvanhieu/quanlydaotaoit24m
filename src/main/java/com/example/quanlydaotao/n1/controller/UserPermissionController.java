
package com.example.quanlydaotao.n1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n1.dto.PermissionResponse;
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
}
