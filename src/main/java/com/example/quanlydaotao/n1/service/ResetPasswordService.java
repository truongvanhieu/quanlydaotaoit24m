package com.example.quanlydaotao.n1.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n1.dto.ResetPasswordRequest;
import com.example.quanlydaotao.n1.entity.PasswordResetToken;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.repository.PasswordResetTokenRepository;
import com.example.quanlydaotao.n1.repository.UserRepository;

@Service
public class ResetPasswordService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        String tokenHash = hashToken(request.getToken());

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException("Token không hợp lệ"));

        if (resetToken.getUsedAt() != null) {
            throw new IllegalArgumentException("Token đã được sử dụng");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy tài khoản"));

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword()));

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Đánh dấu token đã sử dụng
        resetToken.setUsedAt(LocalDateTime.now());

        tokenRepository.save(resetToken);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new IllegalStateException("Không thể mã hóa token", e);
        }
    }
}