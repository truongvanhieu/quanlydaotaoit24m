package com.example.quanlydaotao.n1.dto;

import java.util.UUID;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UUID userId,
        String username
) {
}
