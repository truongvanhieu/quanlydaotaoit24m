package com.example.quanlydaotao.auth;

import static com.example.quanlydaotao.auth.AuthModels.LoginRequest;
import static com.example.quanlydaotao.auth.AuthModels.UserSessionInfo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UserSessionInfo login(@RequestBody LoginRequest request, HttpSession session) {
        UserSessionInfo user = authService.authenticate(request.username(), request.password());
        session.setAttribute(SESSION_USER_KEY, user);
        return user;
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping("/me")
    public UserSessionInfo me(HttpSession session) {
        Object attr = session.getAttribute(SESSION_USER_KEY);
        if (!(attr instanceof UserSessionInfo user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }
        return user;
    }
}
