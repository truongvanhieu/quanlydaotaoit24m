package com.example.quanlydaotao.auth;

import static com.example.quanlydaotao.auth.AuthModels.UserSessionInfo;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

    private record ResetToken(String otp, Instant expiresAt, String fullName, String role, String studentCode) {
    }

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

        List<UserRow> users = jdbcTemplate.query("""
                SELECT username, password, full_name, role, student_code
                FROM dbo.app_users
                WHERE LOWER(username) = LOWER(?) AND is_active = 1
                """, (rs, rowNum) -> new UserRow(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("student_code")), uname);

        if (!users.isEmpty()) {
            UserRow u = users.get(0);
            if (!pass.equals(u.password())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu!");
            }
            return new UserSessionInfo(u.username(), u.fullName(), u.role(), u.studentCode());
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

    private record UserRow(String username, String password, String fullName, String role, String studentCode) {
    }

    public Map<String, Object> requestPasswordReset(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên đăng nhập hoặc mã sinh viên (IDSV).");
        }
        String uname = username.trim();

        List<UserSessionInfo> users = jdbcTemplate.query("""
                SELECT username, full_name, role, student_code
                FROM dbo.app_users
                WHERE LOWER(username) = LOWER(?) AND is_active = 1
                """, (rs, rowNum) -> new UserSessionInfo(
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("student_code")), uname);

        UserSessionInfo target = users.isEmpty() ? null : users.get(0);
        if (target == null) {
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
                target = students.get(0);
            }
        }

        if (target == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản hoặc mã sinh viên: " + uname);
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        resetTokens.put(target.username().toLowerCase(), new ResetToken(
                otp,
                Instant.now().plusSeconds(600),
                target.fullName(),
                target.role(),
                target.studentCode()));

        return Map.of(
                "username", target.username(),
                "fullName", target.fullName(),
                "otpDemo", otp,
                "message", "Đã tạo mã OTP xác nhận cho tài khoản " + target.username() + " (hiệu lực 10 phút)."
        );
    }

    public Map<String, Object> resetPassword(String username, String otp, String newPassword) {
        if (username == null || username.isBlank() || otp == null || otp.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập đầy đủ tên đăng nhập, mã OTP và mật khẩu mới.");
        }
        String uname = username.trim();
        String pass = newPassword.trim();
        if (pass.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 4 ký tự.");
        }

        String key = uname.toLowerCase();
        ResetToken token = resetTokens.get(key);
        if (token == null || Instant.now().isAfter(token.expiresAt())) {
            resetTokens.remove(key);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã OTP không tồn tại hoặc đã hết hạn. Vui lòng lấy mã mới.");
        }
        if (!token.otp().equals(otp.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã OTP không chính xác.");
        }

        Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.app_users WHERE LOWER(username) = LOWER(?)",
                Integer.class, uname);
        if (existingCount != null && existingCount > 0) {
            jdbcTemplate.update(
                    "UPDATE dbo.app_users SET password = ? WHERE LOWER(username) = LOWER(?)",
                    pass, uname);
        } else {
            jdbcTemplate.update("""
                    INSERT INTO dbo.app_users (id, username, password, full_name, role, student_code, is_active)
                    VALUES (NEWID(), ?, ?, ?, ?, ?, 1)
                    """, uname, pass, token.fullName(), token.role(), token.studentCode());
        }

        resetTokens.remove(key);
        return Map.of(
                "username", uname,
                "message", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới."
        );
    }
}
