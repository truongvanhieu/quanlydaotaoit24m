package com.example.quanlydaotao.n1.dto;

public record CreatePermissionRequest(
        String code,
        String name,
        String module,
        String description) {
}
