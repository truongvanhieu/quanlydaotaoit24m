
package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n1.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCodeAndIsActiveTrue(String code);

    List<Permission> findByIsActiveTrueAndDeletedAtIsNull();
}
