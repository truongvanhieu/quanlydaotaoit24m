package com.example.quanlydaotao.n1.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n1.dto.ForgotPasswordRequest;
import com.example.quanlydaotao.n1.dto.LoginRequest;
import com.example.quanlydaotao.n1.dto.LoginResponse;
import com.example.quanlydaotao.n1.dto.RegisterRequest;
import com.example.quanlydaotao.n1.dto.ResetPasswordRequest;
import com.example.quanlydaotao.n1.dto.UserResponse;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.service.AuthService;
import com.example.quanlydaotao.n1.service.ForgotPasswordService;
import com.example.quanlydaotao.n1.service.LogoutService;
import com.example.quanlydaotao.n1.service.ResetPasswordService;
import com.example.quanlydaotao.n1.service.UserService;

import jakarta.validation.Valid;@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final LogoutService logoutService;
    private final UserService userService;
    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;
    private final ResetPasswordService resetPasswordService;
    public AuthController(
        UserService userService,
        AuthService authService,
        LogoutService logoutService,
        ForgotPasswordService forgotPasswordService,
        ResetPasswordService resetPasswordService) {
            this.userService = userService;
            this.authService = authService;
            this.logoutService = logoutService;
            this.forgotPasswordService = forgotPasswordService;
            this.resetPasswordService = resetPasswordService;
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();

        user.setUsername(request.username());
        user.setPasswordHash(request.password());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        User savedUser = userService.createUser(user);

        return UserResponse.from(savedUser);
        
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false)String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Vui long cung cap JWT token!"
            );
        }

        String token = authorization.substring(7);

        try {
            logoutService.logout(token);

            return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "Dang xuat thanh cong!"
                )
            );

        } catch (Exception ex) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Token khong hop le hoac da het han!"
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        var token = forgotPasswordService.createResetToken(request.getEmail());

        if (token.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi."));
        }

        return ResponseEntity.ok(Map.of(
            "message", "Tạo yêu cầu đặt lại mật khẩu thành công.",
            "resetToken", token.get()
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {

    try {
        resetPasswordService.resetPassword(request);

        return ResponseEntity.ok(
                Map.of("message", "Đặt lại mật khẩu thành công!"));

    } catch (IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                Map.of("message", ex.getMessage()));
        }
    }
}
