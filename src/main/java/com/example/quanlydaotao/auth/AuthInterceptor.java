package com.example.quanlydaotao.auth;

import static com.example.quanlydaotao.auth.AuthModels.UserSessionInfo;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object attr = session != null ? session.getAttribute(AuthController.SESSION_USER_KEY) : null;
        if (!(attr instanceof UserSessionInfo user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để sử dụng hệ thống");
        }

        String role = user.role();
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }

        boolean isRead = HttpMethod.GET.matches(method);
        if ("STUDENT".equalsIgnoreCase(role)) {
            if (!isRead) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Tài khoản Sinh viên chỉ được phép xem, không có quyền chỉnh sửa!");
            }
            return true;
        }

        if ("LECTURER".equalsIgnoreCase(role)) {
            String path = request.getRequestURI();
            if (!isRead) {
                if (path.startsWith("/api/students")) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Chỉ Admin mới có quyền thêm, sửa, xóa hồ sơ sinh viên!");
                }
                if (HttpMethod.DELETE.matches(method) && path.startsWith("/api/grades/course-classes")) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Chỉ Admin mới có quyền xóa lớp học phần!");
                }
            }
            return true;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập");
    }
}
