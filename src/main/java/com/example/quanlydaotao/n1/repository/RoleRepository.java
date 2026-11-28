package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n1.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCodeAndIsActiveTrue(String code);

<<<<<<< HEAD
    Optional<Role> findByCode(String code);

    List<Role> findByIsActiveTrueAndDeletedAtIsNull();

    List<Role> findByDeletedAtIsNullOrderByNameAsc();

    Optional<Role> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);
=======
    List<Role> findByIsActiveTrueAndDeletedAtIsNull();
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
}
