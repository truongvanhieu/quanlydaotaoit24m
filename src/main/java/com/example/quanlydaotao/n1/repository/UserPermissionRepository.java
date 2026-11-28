package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n1.entity.UserPermission;

public interface UserPermissionRepository
        extends JpaRepository<UserPermission, UUID> {

    List<UserPermission> findByUserIdAndIsActiveTrueAndDeletedAtIsNull(UUID userId);

    long countByPermissionIdAndIsActiveTrueAndDeletedAtIsNull(UUID permissionId);

    Optional<UserPermission> findByUserIdAndPermissionIdAndIsActiveTrueAndDeletedAtIsNull(
            UUID userId, UUID permissionId);
}
