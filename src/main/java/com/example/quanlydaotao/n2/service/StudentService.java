package com.example.quanlydaotao.n2.service;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final JdbcTemplate jdbcTemplate;

    public StudentService(StudentRepository studentRepository, JdbcTemplate jdbcTemplate) {
        this.studentRepository = studentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public List<Student> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll();
        }
        String value = keyword.trim();
        return studentRepository.findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                value, value, value, value);
    }

    public Student getById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với id: " + id));
    }

    public String getNextStudentCode() {
        List<String> codes = jdbcTemplate.query(
                "SELECT student_code FROM students WHERE student_code LIKE 'SV%'",
                (rs, rowNum) -> rs.getString("student_code"));
        int maxNum = 0;
        for (String code : codes) {
            if (code != null) {
                String trimmed = code.trim();
                if (trimmed.toUpperCase().startsWith("SV")) {
                    try {
                        int num = Integer.parseInt(trimmed.substring(2));
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return String.format("SV%03d", maxNum + 1);
    }

    public Student save(Student student) {
        if (student.getStudentCode() == null || student.getStudentCode().trim().isEmpty()
                || "AUTO".equalsIgnoreCase(student.getStudentCode().trim())) {
            student.setStudentCode(getNextStudentCode());
        }
        Student saved = studentRepository.save(student);
        try {
            jdbcTemplate.update("""
                    IF NOT EXISTS (SELECT 1 FROM students_ WHERE id = ?)
                        INSERT INTO students_ (id, code, full_name, is_active) VALUES (?, ?, ?, 1)
                    ELSE
                        UPDATE students_ SET code = ?, full_name = ? WHERE id = ?
                    """,
                    saved.getId(), saved.getId(), saved.getStudentCode(), saved.getFullName(),
                    saved.getStudentCode(), saved.getFullName(), saved.getId());
        } catch (Exception ignored) {
        }
        return saved;
    }

    public void delete(UUID id) {
        studentRepository.deleteById(id);
    }
}