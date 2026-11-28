package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.dto.AssignPermissionRequest;
import com.example.quanlydaotao.n1.dto.PermissionResponse;
import com.example.quanlydaotao.n1.entity.Permission;
import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.entity.UserPermission;
import com.example.quanlydaotao.n1.repository.PermissionRepository;
import com.example.quanlydaotao.n1.repository.UserPermissionRepository;
import com.example.quanlydaotao.n1.repository.UserRepository;

@Service
public class UserPermissionService {

    private final UserRoleService userRoleService;
    private final RolePermissionService rolePermissionService;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public UserPermissionService(
            UserRoleService userRoleService,
            RolePermissionService rolePermissionService,
            UserPermissionRepository userPermissionRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository) {
        this.userRoleService = userRoleService;
        this.rolePermissionService = rolePermissionService;
        this.userPermissionRepository = userPermissionRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    public List<PermissionResponse> getPermissionsByUserId(UUID userId) {
        Map<UUID, PermissionResponse> permissions = new LinkedHashMap<>();

        for (PermissionResponse permission : getDirectPermissionsByUserId(userId)) {
            permissions.put(permission.getId(), permission);
        }

        List<Role> roles = userRoleService.getRolesByUserId(userId);
        for (Role role : roles) {
            List<PermissionResponse> rolePermissions =
                    rolePermissionService.getPermissionsByRoleId(role.getId());

            for (PermissionResponse permission : rolePermissions) {
                permissions.putIfAbsent(permission.getId(), permission);
            }
        }

        return new ArrayList<>(permissions.values());
    }

    public List<PermissionResponse> getDirectPermissionsByUserId(UUID userId) {
        List<UUID> permissionIds = userPermissionRepository
                .findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId)
                .stream()
                .map(UserPermission::getPermissionId)
                .distinct()
                .toList();

        Map<UUID, Permission> permissionsById = permissionRepository
                .findAllById(permissionIds)
                .stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getIsActive()))
                .filter(permission -> permission.getDeletedAt() == null)
                .collect(Collectors.toMap(Permission::getId, permission -> permission));

        return permissionIds.stream()
                .map(permissionsById::get)
                .filter(permission -> permission != null)
                .map(permission -> new PermissionResponse(
                        permission.getId(),
                        permission.getCode(),
                        permission.getName(),
                        permission.getModule(),
                        permission.getDescription()))
                .toList();
    }

    public UserPermission assignPermission(
            UUID userId,
            AssignPermissionRequest request) {
        if (request == null || request.getPermissionId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn quyền cần gán");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .filter(candidate -> Boolean.TRUE.equals(candidate.getIsActive()))
                .filter(candidate -> candidate.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quyền không tồn tại hoặc đã bị vô hiệu hóa"));

        return userPermissionRepository
                .findByUserIdAndPermissionIdAndIsActiveTrueAndDeletedAtIsNull(
                        userId, permission.getId())
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    UserPermission userPermission = new UserPermission();
                    userPermission.setId(UUID.randomUUID());
                    userPermission.setUserId(userId);
                    userPermission.setPermissionId(permission.getId());
                    userPermission.setIsActive(true);
                    userPermission.setCreatedAt(now);
                    userPermission.setUpdatedAt(now);
                    return userPermissionRepository.save(userPermission);
                });
    }

    public UserPermission removePermission(UUID userId, UUID permissionId) {
        UserPermission userPermission = userPermissionRepository
                .findByUserIdAndPermissionIdAndIsActiveTrueAndDeletedAtIsNull(
                        userId, permissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Người dùng chưa được gán quyền trực tiếp này"));

        LocalDateTime now = LocalDateTime.now();
        userPermission.setIsActive(false);
        userPermission.setDeletedAt(now);
        userPermission.setUpdatedAt(now);
        return userPermissionRepository.save(userPermission);
    }
}
