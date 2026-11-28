package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.dto.PermissionModuleRequest;
import com.example.quanlydaotao.n1.entity.PermissionModule;
import com.example.quanlydaotao.n1.repository.PermissionModuleRepository;
import com.example.quanlydaotao.n1.repository.PermissionRepository;

@Service
public class PermissionModuleService {

    private final PermissionModuleRepository permissionModuleRepository;
    private final PermissionRepository permissionRepository;

    public PermissionModuleService(
            PermissionModuleRepository permissionModuleRepository,
            PermissionRepository permissionRepository) {
        this.permissionModuleRepository = permissionModuleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionModule> getActiveModules() {
        return permissionModuleRepository
                .findByIsActiveTrueAndDeletedAtIsNullOrderByNameAsc();
    }

    public PermissionModule createModule(PermissionModuleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin phân hệ không hợp lệ");
        }

        String code = normalizeCode(request.code());
        String name = requiredText(request.name(), "Tên phân hệ");
        String description = blankToNull(request.description());

        validateLength(code, 100, "Mã phân hệ");
        validateLength(name, 150, "Tên phân hệ");
        validateLength(description, 255, "Mô tả");

        if (permissionModuleRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã phân hệ đã tồn tại");
        }

        LocalDateTime now = LocalDateTime.now();
        PermissionModule module = new PermissionModule();
        module.setId(UUID.randomUUID());
        module.setCode(code);
        module.setName(name);
        module.setDescription(description);
        module.setCreatedAt(now);
        module.setUpdatedAt(now);
        module.setIsActive(true);
        return permissionModuleRepository.save(module);
    }

    public PermissionModule updateModule(
            UUID moduleId,
            PermissionModuleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin phân hệ không hợp lệ");
        }

        PermissionModule module = findModule(moduleId);
        String code = normalizeCode(request.code());
        if (!module.getCode().equals(code)) {
            throw new IllegalArgumentException(
                    "Không thể đổi mã phân hệ; hãy tạo phân hệ mới nếu cần");
        }

        String name = requiredText(request.name(), "Tên phân hệ");
        String description = blankToNull(request.description());
        validateLength(name, 150, "Tên phân hệ");
        validateLength(description, 255, "Mô tả");

        module.setName(name);
        module.setDescription(description);
        module.setUpdatedAt(LocalDateTime.now());
        return permissionModuleRepository.save(module);
    }

    public void deactivateModule(UUID moduleId) {
        PermissionModule module = findModule(moduleId);
        long activePermissionCount = permissionRepository
                .countByModuleAndIsActiveTrueAndDeletedAtIsNull(module.getCode());
        if (activePermissionCount > 0) {
            throw new IllegalArgumentException(
                    "Không thể ẩn phân hệ vì vẫn còn "
                            + activePermissionCount
                            + " quyền đang hoạt động thuộc phân hệ này");
        }

        LocalDateTime now = LocalDateTime.now();
        module.setIsActive(false);
        module.setDeletedAt(now);
        module.setUpdatedAt(now);
        permissionModuleRepository.save(module);
    }

    private PermissionModule findModule(UUID moduleId) {
        return permissionModuleRepository.findByIdAndDeletedAtIsNull(moduleId)
                .filter(module -> Boolean.TRUE.equals(module.getIsActive()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy phân hệ đang hoạt động"));
    }

    private String normalizeCode(String value) {
        String code = requiredText(value, "Mã phân hệ").toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException(
                    "Mã phân hệ chỉ gồm chữ cái, số và dấu gạch dưới; bắt đầu bằng chữ cái");
        }
        return code;
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
