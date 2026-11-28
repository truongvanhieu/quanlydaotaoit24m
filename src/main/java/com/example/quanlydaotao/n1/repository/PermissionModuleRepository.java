package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n1.entity.PermissionModule;

public interface PermissionModuleRepository
        extends JpaRepository<PermissionModule, UUID> {

    List<PermissionModule> findByIsActiveTrueAndDeletedAtIsNullOrderByNameAsc();

    Optional<PermissionModule> findByCodeAndIsActiveTrueAndDeletedAtIsNull(
            String code);

    Optional<PermissionModule> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByCode(String code);
}
