package com.example.quanlydaotao.n2.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n1.entity.Role;
import com.example.quanlydaotao.n1.entity.User;
import com.example.quanlydaotao.n1.entity.UserRole;
import com.example.quanlydaotao.n1.repository.RoleRepository;
import com.example.quanlydaotao.n1.repository.UserRepository;
import com.example.quanlydaotao.n1.repository.UserRoleRepository;
import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.quanlydaotao.n1.repository.UserRoleRepository userRoleWriteRepository;

    public StudentService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            com.example.quanlydaotao.n1.repository.UserRoleRepository userRoleWriteRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleWriteRepository = userRoleWriteRepository;
    }

    public List<Student> getAll() {
        syncStudentRoles();
        return studentRepository.findAll();
    }

    public List<Student> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        String value = keyword.trim();
        syncStudentRoles();

        List<Student> matchedByStudentFields = studentRepository
                .findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                        value, value, value, value);

        List<User> matchedUsers = userRepository.searchActiveUsers(value);
        List<Student> matchedByUsername = matchedUsers.stream()
                .filter(user -> hasStudentRole(user.getId()))
                .map(this::ensureStudentProfile)
                .toList();

        java.util.LinkedHashMap<UUID, Student> result = new java.util.LinkedHashMap<>();
        matchedByUsername.forEach(student -> result.put(student.getId(), student));
        matchedByStudentFields.forEach(student -> result.put(student.getId(), student));
        return result.values().stream().toList();
    }

    public Student getById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với id: " + id));
    }

    @Transactional
    public Student save(Student student) {
        Student savedStudent = studentRepository.save(student);
        syncStudentAccount(savedStudent);
        savedStudent.setPassword(null);
        return savedStudent;
    }

    @Transactional
    public void delete(UUID id) {
        studentRepository.findById(id).ifPresent(student -> {
            if (student.getEmail() != null && !student.getEmail().isBlank()) {
                userRepository.findByEmailIgnoreCase(student.getEmail().trim())
                        .ifPresent(user -> {
                            user.setIsActive(false);
                            user.setDeletedAt(java.time.LocalDateTime.now());
                            userRepository.save(user);
                        });
            }
        });
        studentRepository.deleteById(id);
    }

    private void syncStudentAccount(Student student) {
        java.util.Optional<User> existingUser = java.util.Optional.empty();

        if (student.getEmail() != null && !student.getEmail().isBlank()) {
            existingUser = userRepository.findByEmailIgnoreCase(student.getEmail().trim());
        }
        if (existingUser.isEmpty()
                && student.getUsername() != null
                && !student.getUsername().isBlank()) {
            existingUser = userRepository.findByUsername(student.getUsername().trim());
        }

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setFullName(student.getFullName());
            user.setEmail(student.getEmail());
            user.setPhone(student.getPhone());
            user.setUpdatedAt(java.time.LocalDateTime.now());
            user.setIsActive(true);
            user.setDeletedAt(null);
            if (student.getPassword() != null && !student.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(student.getPassword()));
            }
            if (student.getUsername() != null && !student.getUsername().isBlank()) {
                String username = student.getUsername().trim();
                if (!user.getUsername().equals(username)
                        && userRepository.existsByUsername(username)) {
                    throw new RuntimeException("Tên đăng nhập đã được sử dụng");
                }
                user.setUsername(username);
            }
            user = userRepository.save(user);
        } else {
            String username = student.getUsername();
            if (username == null || username.isBlank()) {
                throw new RuntimeException("Vui lòng nhập tên đăng nhập cho sinh viên");
            }
            username = username.trim();
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại");
            }
            if (student.getPassword() == null || student.getPassword().isBlank()) {
                throw new RuntimeException("Vui lòng nhập mật khẩu cho sinh viên");
            }
            if (student.getEmail() != null
                    && !student.getEmail().isBlank()
                    && userRepository.existsByEmail(student.getEmail().trim())) {
                throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
            }

            user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(student.getPassword()));
            user.setFullName(student.getFullName());
            user.setEmail(student.getEmail());
            user.setPhone(student.getPhone());
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            user.setIsActive(true);
            user = userRepository.save(user);
        }

        Role studentRole = findStudentRole();
        if (studentRole != null) {
            boolean alreadyAssigned = userRoleWriteRepository
                    .findByUserIdAndRoleIdAndIsActiveTrueAndDeletedAtIsNull(
                            user.getId(), studentRole.getId())
                    .isPresent();
            if (!alreadyAssigned) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID());
                userRole.setUserId(user.getId());
                userRole.setRoleId(studentRole.getId());
                userRole.setIsActive(true);
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                userRole.setCreatedAt(now);
                userRole.setUpdatedAt(now);
                userRoleWriteRepository.save(userRole);
            }
        }
    }

    private Role findStudentRole() {
        Role existingRole = roleRepository.findByIsActiveTrueAndDeletedAtIsNull().stream()
                .filter(value -> "STUDENT".equals(normalizeRoleValue(value.getCode()))
                        || "SINHVIEN".equals(normalizeRoleValue(value.getCode()))
                        || "STUDENT".equals(normalizeRoleValue(normalizeAccents(value.getName())))
                        || "SINHVIEN".equals(normalizeRoleValue(normalizeAccents(value.getName()))))
                .findFirst()
                .orElse(null);
        if (existingRole != null) {
            return existingRole;
        }

        Role role = roleRepository.findByCode("STUDENT").orElseGet(Role::new);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (role.getId() == null) {
            role.setId(UUID.randomUUID());
            role.setCode("STUDENT");
            role.setCreatedAt(now);
            role.setIsSystem(true);
        }
        role.setName("Sinh viên");
        role.setDescription("Vai trò mặc định cho tài khoản sinh viên");
        role.setIsActive(true);
        role.setDeletedAt(null);
        role.setUpdatedAt(now);
        return roleRepository.save(role);
    }

    private void syncStudentRoles() {
        userRepository.findAll().forEach(user -> {
            if (hasStudentRole(user.getId())) {
                ensureStudentProfile(user);
            }
        });
    }

    private boolean hasStudentRole(UUID userId) {
        return userRoleRepository
                .findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId)
                .stream()
                .map(UserRole::getRoleId)
                .map(roleRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::orElseThrow)
                .filter(role -> Boolean.TRUE.equals(role.getIsActive()))
                .filter(role -> role.getDeletedAt() == null)
                .anyMatch(this::isStudentRole);
    }

    private Student ensureStudentProfile(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            java.util.Optional<Student> byEmail =
                    studentRepository.findByEmailIgnoreCase(user.getEmail().trim());
            if (byEmail.isPresent()) {
                return byEmail.get();
            }
        }

        String studentCode = "SV-" + user.getId().toString().replace("-", "").substring(0, 8);
        java.util.Optional<Student> byCode = studentRepository.findAll().stream()
                .filter(student -> studentCode.equalsIgnoreCase(student.getStudentCode()))
                .findFirst();
        if (byCode.isPresent()) {
            return byCode.get();
        }

        Student student = new Student(
                studentCode,
                user.getFullName(),
                user.getEmail() == null ? "" : user.getEmail().trim(),
                user.getPhone() == null ? "" : user.getPhone().trim(),
                "");
        return studentRepository.save(student);
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
}