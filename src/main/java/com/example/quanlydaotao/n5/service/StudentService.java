package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.Student;
import com.example.quanlydaotao.n5.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StudentService {
    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public List<Student> findAll() {
        return repo.findAll();
    }

    public Student findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với ID: " + id));
    }

    public Student save(Student s) {
        return repo.save(s);
    }

    public Student update(UUID id, Student s) {
        Student existing = findById(id);
        if (s.getStudentCode() != null) existing.setStudentCode(s.getStudentCode());
        if (s.getFullName() != null) existing.setFullName(s.getFullName());
        if (s.getEmail() != null) existing.setEmail(s.getEmail());
        if (s.getPhone() != null) existing.setPhone(s.getPhone());
        if (s.getClassName() != null) existing.setClassName(s.getClassName());
        if (s.getIsActive() != null) existing.setIsActive(s.getIsActive());
        return repo.save(existing);
    }

    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
