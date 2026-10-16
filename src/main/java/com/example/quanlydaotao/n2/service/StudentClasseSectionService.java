package com.example.quanlydaotao.n2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.StudentClasseSection;
import com.example.quanlydaotao.n2.repository.StudentClasseSectionRepository;

@Service
@Transactional
public class StudentClasseSectionService {

    private final StudentClasseSectionRepository repository;

    public StudentClasseSectionService(StudentClasseSectionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StudentClasseSection> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public StudentClasseSection getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân công lớp với id: " + id));
    }

    @Transactional(readOnly = true)
    public List<StudentClasseSection> getByStudentId(UUID studentId) {
        return repository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<StudentClasseSection> getByStudentClasseId(UUID classeId) {
        return repository.findByStudentClasseId(classeId);
    }

    public StudentClasseSection save(StudentClasseSection section) {
        return repository.save(section);
    }

    public void delete(UUID id) {
        StudentClasseSection section = getById(id);
        section.setIsActive(false);
        section.setDeletedAt(LocalDateTime.now());
        repository.save(section);
    }

    public void hardDelete(UUID id) {
        repository.deleteById(id);
    }
}
