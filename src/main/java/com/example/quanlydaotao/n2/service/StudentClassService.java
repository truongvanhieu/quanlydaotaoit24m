package com.example.quanlydaotao.n2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.StudentClass;
import com.example.quanlydaotao.n2.repository.StudentClassRepository;

@Service
@Transactional
public class StudentClassService {

    private final StudentClassRepository studentClassRepository;

    public StudentClassService(StudentClassRepository studentClassRepository) {
        this.studentClassRepository = studentClassRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentClass> getAll() {
        return studentClassRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StudentClass> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentClassRepository.findAll();
        }
        return studentClassRepository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public StudentClass getById(UUID id) {
        return studentClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học với id: " + id));
    }

    public StudentClass save(StudentClass studentClass) {
        return studentClassRepository.save(studentClass);
    }

    public void delete(UUID id) {
        StudentClass studentClass = getById(id);
        studentClass.setIsActive(false);
        studentClass.setDeletedAt(LocalDateTime.now());
        studentClassRepository.save(studentClass);
    }

    public void hardDelete(UUID id) {
        studentClassRepository.deleteById(id);
    }
}
