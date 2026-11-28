package com.example.quanlydaotao.n1.dto;

public record RegisterRequest(
        String username,
        String password,
        String fullName,
        String email,
        String phone
) {
}
