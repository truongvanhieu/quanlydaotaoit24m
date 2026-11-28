package com.example.quanlydaotao.n1.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n1.entity.PasswordResetToken;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.repository.PasswordResetTokenRepository;
import com.example.quanlydaotao.n1.repository.UserRepository;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public ForgotPasswordService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public Optional<String> createResetToken(String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        // Xóa token cũ của tài khoản
        tokenRepository.deleteByUserId(user.getId());

        // Tạo token ngẫu nhiên
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Chỉ lưu SHA-256 hash vào database
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenHash(tokenHash);
        resetToken.setUserId(user.getId());
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        resetToken.setCreatedAt(LocalDateTime.now());

        tokenRepository.save(resetToken);

        // Trả token gốc để phục vụ kiểm thử local.
        // Khi tích hợp email, gửi token qua email thay vì trả trong API.
        return Optional.of(rawToken);
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