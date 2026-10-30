package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n1.entity.RolePermission;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleIdAndIsActiveTrueAndDeletedAtIsNull(
            UUID roleId);

    List<RolePermission> findByPermissionIdAndIsActiveTrueAndDeletedAtIsNull(
            UUID permissionId);

    Optional<RolePermission>
    findByRoleIdAndPermissionIdAndIsActiveTrueAndDeletedAtIsNull(
            UUID roleId, UUID permissionId);
}
