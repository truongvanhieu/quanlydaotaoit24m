package com.example.quanlydaotao.n2.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.repository.StudentRepository;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Student> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll();
        }
        return studentRepository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public Student getById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Student> getByClassId(UUID classId) {
        return studentRepository.findByStudentClassessId(classId);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void delete(UUID id) {
        // Soft delete
        Student student = getById(id);
        student.setIsActive(false);
        student.setDeletedAt(java.time.LocalDateTime.now());
        studentRepository.save(student);
    }

    public void hardDelete(UUID id) {
        studentRepository.deleteById(id);
    }
}