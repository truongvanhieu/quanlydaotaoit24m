
package com.example.quanlydaotao.n1.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.dto.PermissionResponse;
import com.example.quanlydaotao.n1.entity.Role;

@Service
public class UserPermissionService {

    private final UserRoleService userRoleService;
    private final RolePermissionService rolePermissionService;

    public UserPermissionService(
            UserRoleService userRoleService,
            RolePermissionService rolePermissionService) {
        this.userRoleService = userRoleService;
        this.rolePermissionService = rolePermissionService;
    }

    public List<PermissionResponse> getPermissionsByUserId(UUID userId) {
        List<Role> roles = userRoleService.getRolesByUserId(userId);

        Map<UUID, PermissionResponse> permissions = new LinkedHashMap<>();

        for (Role role : roles) {
            List<PermissionResponse> rolePermissions =
                    rolePermissionService.getPermissionsByRoleId(role.getId());

            for (PermissionResponse permission : rolePermissions) {
                permissions.putIfAbsent(permission.getId(), permission);
            }
        }

        return new ArrayList<>(permissions.values());
    }
}
