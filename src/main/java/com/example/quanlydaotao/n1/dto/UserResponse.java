
package com.example.quanlydaotao.n1.dto;

import java.util.UUID;

import com.example.quanlydaotao.n1.entity.User;

public record UserResponse(
        UUID id,
        String username,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        Boolean isActive
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getIsActive()
        );
    }
}
