package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n1.entity.RevokedToken;
import com.example.quanlydaotao.n1.repository.RevokedTokenRepository;
import com.example.quanlydaotao.n1.security.JwtService;

import io.jsonwebtoken.Claims;

@Service
public class LogoutService {

    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    public LogoutService(
            JwtService jwtService,
            RevokedTokenRepository revokedTokenRepository) {
        this.jwtService = jwtService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void logout(String token) {

        // Lấy thông tin từ JWT
        Claims claims = jwtService.extractClaims(token);

        String tokenId = claims.getId();

        // Kiểm tra JWT có mã định danh hay không
        if (tokenId == null || tokenId.isBlank()) {
            throw new RuntimeException("JWT không có Token ID!");
        }

        // Tránh lưu trùng token
        if (revokedTokenRepository.existsByTokenId(tokenId)) {
            return;
        }

        // Lấy ID người dùng từ JWT
        UUID userId = UUID.fromString(claims.getSubject());

        // Lấy thời gian hết hạn
        Date expiration = claims.getExpiration();

        LocalDateTime expiresAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Tạo bản ghi JWT bị thu hồi
        RevokedToken revokedToken = new RevokedToken();

        revokedToken.setTokenId(tokenId);
        revokedToken.setUserId(userId);
        revokedToken.setExpiresAt(expiresAt);
        revokedToken.setRevokedAt(LocalDateTime.now());

        // Lưu vào SQL Server
        revokedTokenRepository.save(revokedToken);
    }
}
