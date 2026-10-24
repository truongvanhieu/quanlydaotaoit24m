
package com.example.quanlydaotao.n1.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.dto.PermissionResponse;
import com.example.quanlydaotao.n1.entity.Permission;
import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.entity.RolePermission;
import com.example.quanlydaotao.n1.repository.PermissionRepository;
import com.example.quanlydaotao.n1.repository.RolePermissionRepository;
import com.example.quanlydaotao.n1.repository.RoleRepository;

@Service
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionService(
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionResponse> getPermissionsByRoleCode(String roleCode) {
        Role role = roleRepository.findByCodeAndIsActiveTrue(roleCode)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy vai trò: " + roleCode));

        UUID roleId = role.getId();

        List<RolePermission> rolePermissions = rolePermissionRepository
                .findByRoleIdAndIsActiveTrueAndDeletedAtIsNull(roleId);

        List<UUID> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .toList();

        Map<UUID, Permission> permissionsById = permissionRepository
                .findAllById(permissionIds)
                .stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getIsActive()))
                .filter(permission -> permission.getDeletedAt() == null)
                .collect(Collectors.toMap(
                        Permission::getId,
                        Function.identity()
                ));

        return permissionIds.stream()
                .map(permissionsById::get)
                .filter(permission -> permission != null)
                .map(permission -> new PermissionResponse(
                        permission.getId(),
                        permission.getCode(),
                        permission.getName(),
                        permission.getModule(),
                        permission.getDescription()
                ))
                .toList();
    }

    
public List<PermissionResponse> getPermissionsByRoleId(UUID roleId) {
    List<RolePermission> rolePermissions = rolePermissionRepository
            .findByRoleIdAndIsActiveTrueAndDeletedAtIsNull(roleId);

    List<UUID> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .distinct()
            .toList();

    Map<UUID, Permission> permissionsById = permissionRepository
            .findAllById(permissionIds)
            .stream()
            .filter(permission -> Boolean.TRUE.equals(permission.getIsActive()))
            .filter(permission -> permission.getDeletedAt() == null)
            .collect(Collectors.toMap(
                    Permission::getId,
                    Function.identity()
            ));

    return permissionIds.stream()
            .map(permissionsById::get)
            .filter(permission -> permission != null)
            .map(permission -> new PermissionResponse(
                    permission.getId(),
                    permission.getCode(),
                    permission.getName(),
                    permission.getModule(),
                    permission.getDescription()
            ))
            .toList();
}

}
