
package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n1.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserIdAndIsActiveTrueAndDeletedAtIsNull(UUID userId);

    List<UserRole> findByRoleIdAndIsActiveTrueAndDeletedAtIsNull(UUID roleId);

    Optional<UserRole> findByUserIdAndRoleIdAndIsActiveTrueAndDeletedAtIsNull(
            UUID userId, UUID roleId);
}
