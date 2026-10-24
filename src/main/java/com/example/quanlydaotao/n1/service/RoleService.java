
package com.example.quanlydaotao.n1.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllActiveRoles() {
        return roleRepository.findByIsActiveTrueAndDeletedAtIsNull();
    }
}
