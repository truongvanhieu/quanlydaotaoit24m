package com.example.quanlydaotao.n1.service;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.dto.RoleRequest;
=======
import java.util.List;

import org.springframework.stereotype.Service;

>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllActiveRoles() {
        return roleRepository.findByIsActiveTrueAndDeletedAtIsNull();
    }
<<<<<<< HEAD

    public List<Role> getRolesForManagement() {
        return roleRepository.findByDeletedAtIsNullOrderByNameAsc();
    }

    public Role createRole(RoleRequest request) {
        String code = normalizeCode(request.code());
        String name = requiredText(request.name(), "Tên vai trò");
        validateLength(code, 100, "Mã vai trò");
        validateLength(name, 100, "Tên vai trò");
        validateLength(request.description(), 255, "Mô tả");

        if (roleRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã vai trò đã tồn tại");
        }

        LocalDateTime now = LocalDateTime.now();
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setName(name);
        role.setDescription(blankToNull(request.description()));
        role.setIsSystem(false);
        role.setIsActive(request.isActive() == null || request.isActive());
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        return roleRepository.save(role);
    }

    public Role updateRole(UUID roleId, RoleRequest request) {
        Role role = roleRepository.findByIdAndDeletedAtIsNull(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò"));
        String code = normalizeCode(request.code());
        String name = requiredText(request.name(), "Tên vai trò");
        validateLength(code, 100, "Mã vai trò");
        validateLength(name, 100, "Tên vai trò");
        validateLength(request.description(), 255, "Mô tả");

        if (Boolean.TRUE.equals(role.getIsSystem())
                && !role.getCode().equalsIgnoreCase(code)) {
            throw new IllegalArgumentException("Không thể đổi mã của vai trò hệ thống");
        }
        if (roleRepository.existsByCodeAndIdNot(code, roleId)) {
            throw new IllegalArgumentException("Mã vai trò đã tồn tại");
        }

        role.setCode(code);
        role.setName(name);
        role.setDescription(blankToNull(request.description()));
        if (request.isActive() != null) {
            role.setIsActive(request.isActive());
        }
        role.setUpdatedAt(LocalDateTime.now());
        return roleRepository.save(role);
    }

    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findByIdAndDeletedAtIsNull(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò"));
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new IllegalArgumentException("Không thể xóa vai trò hệ thống");
        }

        LocalDateTime now = LocalDateTime.now();
        role.setIsActive(false);
        role.setDeletedAt(now);
        role.setUpdatedAt(now);
        roleRepository.save(role);
    }

    private String normalizeCode(String code) {
        String normalized = requiredText(code, "Mã vai trò").toUpperCase();
        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException(
                    "Mã vai trò chỉ gồm chữ in hoa, số và dấu gạch dưới; bắt đầu bằng chữ cái");
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
            throw new IllegalArgumentException(field + " không được vượt quá " + maxLength + " ký tự");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
}
