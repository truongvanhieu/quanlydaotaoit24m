package com.example.quanlydaotao.n1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n1.dto.UserResponse;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.service.UserService;

@RestController
@RequestMapping("/api/n1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return UserResponse.from(savedUser);
    }
}
