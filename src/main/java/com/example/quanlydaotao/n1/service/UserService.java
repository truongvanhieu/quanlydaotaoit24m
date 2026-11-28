package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        if (user.getUsername() == null
                || user.getUsername().isBlank()) {
            throw new RuntimeException("Username không được để trống");
        }

        if (user.getPasswordHash() == null
                || user.getPasswordHash().isBlank()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        if (user.getFullName() == null
                || user.getFullName().isBlank()) {
            throw new RuntimeException("Họ tên không được để trống");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Người dùng đã tồn tại");
        }

        user.setPasswordHash(
                passwordEncoder.encode(user.getPasswordHash()));

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setIsActive(true);

        return userRepository.save(user);
    }
}
