package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n1.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCodeAndIsActiveTrue(String code);

    List<Role> findByIsActiveTrueAndDeletedAtIsNull();
}
