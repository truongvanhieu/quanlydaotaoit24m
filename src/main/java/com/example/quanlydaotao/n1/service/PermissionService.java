package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.dto.CreatePermissionRequest;
import com.example.quanlydaotao.n1.dto.PermissionUsageResponse;
import com.example.quanlydaotao.n1.entity.Permission;
import com.example.quanlydaotao.n1.repository.PermissionModuleRepository;
import com.example.quanlydaotao.n1.repository.PermissionRepository;
import com.example.quanlydaotao.n1.repository.RolePermissionRepository;
import com.example.quanlydaotao.n1.repository.UserPermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionModuleRepository permissionModuleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    public PermissionService(
            PermissionRepository permissionRepository,
            PermissionModuleRepository permissionModuleRepository,
            RolePermissionRepository rolePermissionRepository,
            UserPermissionRepository userPermissionRepository) {
        this.permissionRepository = permissionRepository;
        this.permissionModuleRepository = permissionModuleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    public List<Permission> getAllActivePermissions() {
        return permissionRepository.findByIsActiveTrueAndDeletedAtIsNull();
    }

    public Permission createPermission(CreatePermissionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin quyền không hợp lệ");
        }

        String code = normalizeCode(request.code(), "Mã quyền");
        String name = requiredText(request.name(), "Tên quyền");
        String module = normalizeCode(request.module(), "Phân hệ");
        String description = blankToNull(request.description());

        validateLength(code, 100, "Mã quyền");
        validateLength(name, 150, "Tên quyền");
        validateLength(module, 100, "Phân hệ");
        validateLength(description, 255, "Mô tả");

        if (permissionModuleRepository
                .findByCodeAndIsActiveTrueAndDeletedAtIsNull(module)
                .isEmpty()) {
            throw new IllegalArgumentException(
                    "Phân hệ không tồn tại hoặc đã bị ẩn");
        }

        if (permissionRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã quyền đã tồn tại");
        }

        LocalDateTime now = LocalDateTime.now();
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode(code);
        permission.setName(name);
        permission.setModule(module);
        permission.setDescription(description);
        permission.setCreatedAt(now);
        permission.setUpdatedAt(now);
        permission.setIsActive(true);

        return permissionRepository.save(permission);
    }

    public PermissionUsageResponse getPermissionUsage(UUID permissionId) {
        findPermission(permissionId);
        return new PermissionUsageResponse(
                rolePermissionRepository
                        .countByPermissionIdAndIsActiveTrueAndDeletedAtIsNull(permissionId),
                userPermissionRepository
                        .countByPermissionIdAndIsActiveTrueAndDeletedAtIsNull(permissionId));
    }

    public void deactivatePermission(UUID permissionId) {
        Permission permission = findPermission(permissionId);
        if ("ROLE_MANAGE".equalsIgnoreCase(permission.getCode())) {
            throw new IllegalArgumentException(
                    "Không thể ngừng kích hoạt quyền quản trị phân quyền");
        }
        if (!Boolean.TRUE.equals(permission.getIsActive())) {
            throw new IllegalArgumentException("Quyền này đã ngừng hoạt động");
        }

        LocalDateTime now = LocalDateTime.now();
        permission.setIsActive(false);
        permission.setDeletedAt(now);
        permission.setUpdatedAt(now);
        permissionRepository.save(permission);
    }

    private Permission findPermission(UUID permissionId) {
        return permissionRepository.findByIdAndDeletedAtIsNull(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quyền"));
    }

    private String normalizeCode(String value, String field) {
        String normalized = requiredText(value, field).toUpperCase();
        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException(
                    field + " chỉ gồm chữ cái, số và dấu gạch dưới; bắt đầu bằng chữ cái");
        }
        return normalized;
    }

    private String requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " không được để trống");
        }
        return value.trim();
    }

    private void validateLength(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " không được vượt quá " + maxLength + " ký tự");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
