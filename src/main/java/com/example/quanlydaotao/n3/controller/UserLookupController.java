package com.example.quanlydaotao.n3.controller;

import com.example.quanlydaotao.n3.entity.N3User;
import com.example.quanlydaotao.n3.repository.N3UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/n3/users")
public class UserLookupController {

    private final N3UserRepository userRepository;

    public UserLookupController(N3UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<N3User> getActiveUsers() {
        return userRepository.findByDeletedAtIsNullOrderByUsernameAsc();
    }
}
