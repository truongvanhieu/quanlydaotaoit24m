package com.example.quanlydaotao.auth;

import static com.example.quanlydaotao.auth.AuthModels.UserSessionInfo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureAuthTableAndSeed() {
        jdbcTemplate.execute("""
                IF OBJECT_ID(N'dbo.app_users', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.app_users (
                        id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
                        username NVARCHAR(50) NOT NULL UNIQUE,
                        password NVARCHAR(100) NOT NULL,
                        full_name NVARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL,
                        student_code NVARCHAR(50) NULL,
                        is_active BIT NOT NULL DEFAULT 1
                    );
                END
                """);

        seedUser("admin", "123456", "Quản trị viên (Phòng Đào tạo)", "ADMIN", null);
        seedUser("giangvien", "123456", "ThS. Trương Văn Hiệu (Giảng viên)", "LECTURER", null);
        seedUser("sinhvien", "123456", "Sinh viên (Xem thông tin)", "STUDENT", "110431");
    }

    private void seedUser(String username, String password, String fullName, String role, String studentCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.app_users WHERE LOWER(username) = LOWER(?)",
                Integer.class, username);
        if (count == null || count == 0) {
            jdbcTemplate.update("""
                    INSERT INTO dbo.app_users (id, username, password, full_name, role, student_code, is_active)
                    VALUES (NEWID(), ?, ?, ?, ?, ?, 1)
                    """, username, password, fullName, role, studentCode);
        }
    }

    public UserSessionInfo authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
        }
        String uname = username.trim();
        String pass = password.trim();

        List<UserSessionInfo> users = jdbcTemplate.query("""
                SELECT username, full_name, role, student_code
                FROM dbo.app_users
                WHERE LOWER(username) = LOWER(?) AND password = ? AND is_active = 1
                """, (rs, rowNum) -> new UserSessionInfo(
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("student_code")), uname, pass);

        if (!users.isEmpty()) {
            return users.get(0);
        }

        // Cho phép sinh viên đăng nhập bằng IDSV (student_code) và mật khẩu mặc định 123456
        if ("123456".equals(pass)) {
            List<UserSessionInfo> students = jdbcTemplate.query("""
                    SELECT student_code, full_name
                    FROM dbo.students
                    WHERE student_code = ?
                    """, (rs, rowNum) -> new UserSessionInfo(
                    rs.getString("student_code"),
                    rs.getString("full_name"),
                    "STUDENT",
                    rs.getString("student_code")), uname);
            if (!students.isEmpty()) {
                return students.get(0);
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu!");
    }
}
