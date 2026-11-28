package com.example.quanlydaotao.auth;

public final class AuthModels {
    private AuthModels() {
    }

    public record LoginRequest(String username, String password) {
    }

    public record UserSessionInfo(
            String username,
            String fullName,
            String role,
            String studentCode) {
    }

    public record ForgotPasswordRequest(String username) {
    }

    public record ResetPasswordRequest(String username, String otp, String newPassword) {
    }
}
