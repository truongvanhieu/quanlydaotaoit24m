package com.example.quanlydaotao.n1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n1.dto.RoleSummaryResponse;
import com.example.quanlydaotao.n1.dto.UserResponse;
import com.example.quanlydaotao.n1.dto.UserSearchResponse;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.repository.UserRepository;
import com.example.quanlydaotao.n1.security.RequirePermission;
import com.example.quanlydaotao.n1.service.UserService;
import com.example.quanlydaotao.n1.service.UserRoleService;

@RestController
@RequestMapping("/api/n1/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;

    public UserController(
            UserService userService,
            UserRepository userRepository,
            UserRoleService userRoleService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userRoleService = userRoleService;
    }

    @GetMapping
    @RequirePermission("ROLE_MANAGE")
    public List<UserSearchResponse> searchUsers(
            @RequestParam(defaultValue = "") String keyword) {
        return userRepository.searchActiveUsers(keyword.trim()).stream()
                .map(user -> new UserSearchResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        userRoleService.getRolesByUserId(user.getId()).stream()
                                .map(RoleSummaryResponse::from)
                                .toList()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("ROLE_MANAGE")
    public UserResponse createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return UserResponse.from(savedUser);
    }
}
