package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n1.dto.LoginRequest;
import com.example.quanlydaotao.n1.dto.LoginResponse;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.repository.UserRepository;
import com.example.quanlydaotao.n1.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${JWT_EXPIRATION_MS:3600000}") long expirationMs) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException(
                    "Username và mật khẩu không được để trống");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tên đăng nhập hoặc mật khẩu không đúng"));

        if (Boolean.FALSE.equals(user.getIsActive())
                || user.getDeletedAt() != null) {
            throw new IllegalArgumentException(
                    "Tài khoản đã bị vô hiệu hóa");
        }

        if (!passwordEncoder.matches(
                request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getId(), user.getUsername());

        return new LoginResponse(
                token,
                "Bearer",
                expirationMs / 1000,
                user.getId(),
                user.getUsername());
    }
}
