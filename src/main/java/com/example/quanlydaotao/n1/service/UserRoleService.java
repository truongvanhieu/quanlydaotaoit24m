
package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.entity.UserRole;
import com.example.quanlydaotao.n1.repository.RoleRepository;
import com.example.quanlydaotao.n1.repository.UserRoleRepository;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public UserRoleService(
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    public List<Role> getRolesByUserId(UUID userId) {
        List<UserRole> userRoles = userRoleRepository
                .findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);

        List<UUID> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList();

        return roleRepository.findAllById(roleIds).stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsActive()))
                .filter(role -> role.getDeletedAt() == null)
                .toList();
    }

    
public UserRole assignRole(UUID userId, UUID roleId) {
    Optional<UserRole> existing = userRoleRepository
            .findByUserIdAndRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                    userId, roleId);

    if (existing.isPresent()) {
        return existing.get();
    }

    Role role = roleRepository.findById(roleId)
            .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
            .filter(r -> r.getDeletedAt() == null)
            .orElseThrow(() ->
                    new RuntimeException("Vai trò không tồn tại hoặc đã bị vô hiệu hóa"));

    UserRole userRole = new UserRole();
    userRole.setId(UUID.randomUUID());
    userRole.setUserId(userId);
    userRole.setRoleId(role.getId());
    userRole.setIsActive(true);
    userRole.setCreatedAt(LocalDateTime.now());
    userRole.setUpdatedAt(LocalDateTime.now());

    return userRoleRepository.save(userRole);
}

    
public UserRole removeRole(UUID userId, UUID roleId) {
    UserRole userRole = userRoleRepository
            .findByUserIdAndRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                    userId, roleId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Người dùng chưa được gán vai trò này"));

    LocalDateTime now = LocalDateTime.now();

    userRole.setIsActive(false);
    userRole.setDeletedAt(now);
    userRole.setUpdatedAt(now);

    return userRoleRepository.save(userRole);
}


}
