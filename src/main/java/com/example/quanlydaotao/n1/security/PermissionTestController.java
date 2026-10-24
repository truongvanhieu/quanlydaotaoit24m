package com.example.quanlydaotao.n1.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PermissionTestController {

    @RequirePermission("GRADE_INPUT")
    @GetMapping("/api/rbac-test/grade")
    public String testGradePermission() {
        return "Ban co quyen nhap diem!";
    }
}
