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
import com.example.quanlydaotao.n1.repository.RevokedTokenRepository;
import com.example.quanlydaotao.n1.repository.RolePermissionRepository;
import com.example.quanlydaotao.n1.repository.UserRoleRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;
    public PermissionInterceptor(
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            JwtService jwtService,
            RevokedTokenRepository revokedTokenRepository) {
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.jwtService = jwtService;
        this.revokedTokenRepository = revokedTokenRepository;
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

        // API không yêu cầu quyền RBAC thì giữ nguyên hành vi hiện tại.
        if (required == null) {
            return true;
        }

        // Đọc JWT từ Authorization: Bearer <token>
        String authorization = request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Thieu token Bearer");
            return false;
        }

        String token = authorization.substring(7).trim();
        UUID userId;

        try {
            Claims claims = jwtService.extractClaims(token);
            String tokenId = claims.getId();

            if (tokenId == null || tokenId.isBlank()) {
    response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Token khong co ID");
    return false;
}

if (revokedTokenRepository.existsByTokenId(tokenId)) {
    response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Token da bi thu hoi do dang xuat");
    return false;
}
            userId = UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token khong hop le hoac da het han");
            return false;
        }

        String permissionCode = required.value();

        List<UserRole> userRoles =
                userRoleRepository
                        .findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);

        for (UserRole userRole : userRoles) {
            var rolePermissions = rolePermissionRepository
                    .findByRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                            userRole.getRoleId());

            for (var rolePermission : rolePermissions) {
                List<Permission> permissions =
                        permissionRepository
                                .findByIsActiveTrueAndDeletedAtIsNull();

                for (Permission permission : permissions) {
                    if (permission.getId().equals(
                            rolePermission.getPermissionId())
                            && permissionCode.equals(
                                    permission.getCode())) {
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
