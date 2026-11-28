package com.example.quanlydaotao.n1.dto;

public record RoleRequest(
        String code,
        String name,
        String description,
        Boolean isActive) {
}
