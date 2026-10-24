
package com.example.quanlydaotao.n1.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.entity.Permission;
import com.example.quanlydaotao.n1.repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> getAllActivePermissions() {
        return permissionRepository.findByIsActiveTrueAndDeletedAtIsNull();
    }
}
