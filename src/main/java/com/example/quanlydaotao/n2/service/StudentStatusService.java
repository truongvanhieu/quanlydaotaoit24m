package com.example.quanlydaotao.n2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.StudentStatus;
import com.example.quanlydaotao.n2.repository.StudentStatusRepository;

@Service
@Transactional
public class StudentStatusService {

    private final StudentStatusRepository repository;

    public StudentStatusService(StudentStatusRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StudentStatus> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StudentStatus> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findAll();
        }
        return repository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public StudentStatus getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái với id: " + id));
    }

    @Transactional(readOnly = true)
    public List<StudentStatus> getByStudentId(UUID studentId) {
        return repository.findByStudentId(studentId);
    }

    public StudentStatus save(StudentStatus status) {
        return repository.save(status);
    }

    public void delete(UUID id) {
        StudentStatus status = getById(id);
        status.setIsActive(false);
        status.setDeletedAt(LocalDateTime.now());
        repository.save(status);
    }

    public void hardDelete(UUID id) {
        repository.deleteById(id);
    }
}
