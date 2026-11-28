package com.example.quanlydaotao.n1.dto;

import java.util.UUID;

import com.example.quanlydaotao.n1.entity.Role;

public record RoleSummaryResponse(UUID id, String code, String name) {
    public static RoleSummaryResponse from(Role role) {
        return new RoleSummaryResponse(role.getId(), role.getCode(), role.getName());
    }
}
