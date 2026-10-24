package com.example.quanlydaotao.n1.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.quanlydaotao.n1.entity.Permission;
import com.example.quanlydaotao.n1.entity.UserRole;
import com.example.quanlydaotao.n1.repository.PermissionRepository;
import com.example.quanlydaotao.n1.repository.RolePermissionRepository;
import com.example.quanlydaotao.n1.repository.UserRoleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public PermissionInterceptor(
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository) {
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequirePermission required = handlerMethod.getMethodAnnotation(
                RequirePermission.class);

        if (required == null) {
            required = handlerMethod.getBeanType()
                    .getAnnotation(RequirePermission.class);
        }

        if (required == null) {
            return true;
        }

        String userIdValue = request.getHeader("X-User-Id");

        if (userIdValue == null || userIdValue.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Thieu header X-User-Id");
            return false;
        }

        UUID userId;

        try {
            userId = UUID.fromString(userIdValue);
        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "X-User-Id khong hop le");
            return false;
        }

        String permissionCode = required.value();

        List<UserRole> userRoles =
                userRoleRepository.findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);

        for (UserRole userRole : userRoles) {
            List<com.example.quanlydaotao.n1.entity.RolePermission> rolePermissions =
                    rolePermissionRepository
                            .findByRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                                    userRole.getRoleId());

            for (var rolePermission : rolePermissions) {
                List<Permission> permissions =
                        permissionRepository.findByIsActiveTrueAndDeletedAtIsNull();

                for (Permission permission : permissions) {
                    if (permission.getId().equals(rolePermission.getPermissionId())
                            && permission.getCode().equals(permissionCode)) {
                        return true;
                    }
                }
            }
        }

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Ban khong co quyen: " + permissionCode);
        return false;
    }
}
