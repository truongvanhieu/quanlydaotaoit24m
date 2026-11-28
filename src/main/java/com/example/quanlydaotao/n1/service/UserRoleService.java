package com.example.quanlydaotao.n1.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
<<<<<<< HEAD
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.entity.UserRole;
import com.example.quanlydaotao.n1.repository.RoleRepository;
import com.example.quanlydaotao.n1.repository.UserRepository;
import com.example.quanlydaotao.n1.repository.UserRoleRepository;
import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.repository.StudentRepository;
=======

import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.entity.UserRole;
import com.example.quanlydaotao.n1.repository.RoleRepository;
import com.example.quanlydaotao.n1.repository.UserRoleRepository;
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
<<<<<<< HEAD
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public UserRoleService(
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            StudentRepository studentRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
=======

    public UserRoleService(
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    }

    public List<Role> getRolesByUserId(UUID userId) {
        List<UserRole> userRoles = userRoleRepository
                .findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);

        List<UUID> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList();

        return roleRepository.findAllById(roleIds).stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsActive()))
                .filter(role -> role.getDeletedAt() == null)
                .toList();
    }

    
<<<<<<< HEAD
    @Transactional
    public UserRole assignRole(UUID userId, UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() ->
                        new RuntimeException("Vai trò không tồn tại hoặc đã bị vô hiệu hóa"));

        Optional<UserRole> existing = userRoleRepository
                .findByUserIdAndRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                        userId, roleId);

        if (existing.isPresent()) {
            ensureStudentProfile(userId, role);
            return existing.get();
        }

        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID());
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setIsActive(true);
        userRole.setCreatedAt(LocalDateTime.now());
        userRole.setUpdatedAt(LocalDateTime.now());

        UserRole savedUserRole = userRoleRepository.save(userRole);
        ensureStudentProfile(userId, role);
        return savedUserRole;
    }

    private void ensureStudentProfile(UUID userId, Role role) {
        if (!isStudentRole(role)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Người dùng không tồn tại"));

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            Optional<Student> existingStudent =
                    studentRepository.findByEmailIgnoreCase(user.getEmail().trim());
            if (existingStudent.isPresent()) {
                return;
            }
        }

        String studentCode = "SV-" + userId.toString().replace("-", "").substring(0, 8);
        Student student = new Student(
                studentCode,
                user.getFullName(),
                user.getEmail() == null ? "" : user.getEmail().trim(),
                user.getPhone() == null ? "" : user.getPhone().trim(),
                "");

        studentRepository.save(student);
    }

    private boolean isStudentRole(Role role) {
        String code = normalizeRoleValue(role.getCode());
        String name = normalizeRoleValue(normalizeAccents(role.getName()));
        return "STUDENT".equals(code)
                || "SINHVIEN".equals(code)
                || "STUDENT".equals(name)
                || "SINHVIEN".equals(name);
    }

    private String normalizeRoleValue(String value) {
        if (value == null) {
            return "";
        }
        return normalizeAccents(value).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String normalizeAccents(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
=======
public UserRole assignRole(UUID userId, UUID roleId) {
    Optional<UserRole> existing = userRoleRepository
            .findByUserIdAndRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                    userId, roleId);

    if (existing.isPresent()) {
        return existing.get();
    }

    Role role = roleRepository.findById(roleId)
            .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
            .filter(r -> r.getDeletedAt() == null)
            .orElseThrow(() ->
                    new RuntimeException("Vai trò không tồn tại hoặc đã bị vô hiệu hóa"));

    UserRole userRole = new UserRole();
    userRole.setId(UUID.randomUUID());
    userRole.setUserId(userId);
    userRole.setRoleId(role.getId());
    userRole.setIsActive(true);
    userRole.setCreatedAt(LocalDateTime.now());
    userRole.setUpdatedAt(LocalDateTime.now());

    return userRoleRepository.save(userRole);
}
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac

    
public UserRole removeRole(UUID userId, UUID roleId) {
    UserRole userRole = userRoleRepository
            .findByUserIdAndRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                    userId, roleId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Người dùng chưa được gán vai trò này"));

    LocalDateTime now = LocalDateTime.now();

    userRole.setIsActive(false);
    userRole.setDeletedAt(now);
    userRole.setUpdatedAt(now);

    return userRoleRepository.save(userRole);
}


}
