package com.example.quanlydaotao.n2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.StudentClasseSection;
import com.example.quanlydaotao.n2.repository.StudentClasseSectionRepository;
import com.example.quanlydaotao.n2.repository.StudentRepository;

@Service
@Transactional
public class StudentClasseSectionService {

    private final StudentClasseSectionRepository repository;
    private final StudentRepository studentRepository;

    public StudentClasseSectionService(
            StudentClasseSectionRepository repository,
            StudentRepository studentRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
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
        if (section.getStudentId() == null || section.getStudentClasseId() == null) {
            throw new IllegalArgumentException("Cần chọn sinh viên và lớp hành chính");
        }

        if (section.getId() == null && repository.findByStudentClasseId(section.getStudentClasseId()).stream()
                .anyMatch(existing -> existing.getIsActive() != false
                        && existing.getStudentId() != null
                        && existing.getStudentId().equals(section.getStudentId()))) {
            throw new IllegalArgumentException("Sinh viên đã thuộc lớp hành chính này");
        }

        StudentClasseSection saved = repository.save(section);
        studentRepository.findById(saved.getStudentId()).ifPresent(student -> {
            student.setStudentClassessId(saved.getStudentClasseId());
            studentRepository.save(student);
        });
        return saved;
    }

    public void delete(UUID id) {
        StudentClasseSection section = getById(id);
        section.setIsActive(false);
        section.setDeletedAt(LocalDateTime.now());
        if (section.getEndDate() == null) {
            section.setEndDate(LocalDateTime.now());
        }
        repository.save(section);

        if (section.getStudentId() != null) {
            studentRepository.findById(section.getStudentId()).ifPresent(student -> {
                if (section.getStudentClasseId() != null
                        && section.getStudentClasseId().equals(student.getStudentClassessId())) {
                    student.setStudentClassessId(null);
                    studentRepository.save(student);
                }
            });
        }
    }

    public void hardDelete(UUID id) {
        repository.deleteById(id);
    }
}
