package com.example.quanlydaotao.n2.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.entity.StudentStatus;
import com.example.quanlydaotao.n2.entity.StudentStatusHistory;
import com.example.quanlydaotao.n2.repository.StudentRepository;
import com.example.quanlydaotao.n2.repository.StudentStatusHistoryRepository;
import com.example.quanlydaotao.n2.repository.StudentStatusRepository;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentStatusRepository studentStatusRepository;
    private final StudentStatusHistoryRepository studentStatusHistoryRepository;

    public StudentService(
            StudentRepository studentRepository,
            StudentStatusRepository studentStatusRepository,
            StudentStatusHistoryRepository studentStatusHistoryRepository) {
        this.studentRepository = studentRepository;
        this.studentStatusRepository = studentStatusRepository;
        this.studentStatusHistoryRepository = studentStatusHistoryRepository;
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
        String newStatusCode = student.getStatus() == null ? null : student.getStatus().trim();
        String oldStatusCode = null;

        if (student.getId() != null) {
            oldStatusCode = studentRepository.findById(student.getId())
                    .map(existing -> existing.getStatus() == null ? null : existing.getStatus().trim())
                    .orElse(null);
        }

        StudentStatus newStatus = null;
        if (newStatusCode != null && !newStatusCode.isEmpty()) {
            newStatus = studentStatusRepository.findByCode(newStatusCode)
                    .filter(status -> status.getIsActive() != false)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạng thái sinh viên: " + newStatusCode));
            student.setStatus(newStatusCode);
        } else {
            student.setStatus(null);
        }

        Student saved = studentRepository.save(student);

        if (!java.util.Objects.equals(oldStatusCode, newStatusCode)) {
            if (student.getId() != null) {
                studentStatusHistoryRepository.findByStudentId(saved.getId()).stream()
                        .filter(history -> history.getIsActive() != false && history.getEndDate() == null)
                        .forEach(history -> {
                            history.setEndDate(LocalDate.now());
                            history.setUpdatedAt(LocalDateTime.now());
                            studentStatusHistoryRepository.save(history);
                        });
            }

            StudentStatusHistory history = new StudentStatusHistory();
            history.setStudentId(saved.getId());
            history.setStatusId(newStatus == null ? null : newStatus.getId());
            history.setStartDate(LocalDate.now());
            history.setReason(student.getId() == null ? "Khởi tạo trạng thái sinh viên" : "Thay đổi trạng thái sinh viên");
            history.setCreatedAt(LocalDateTime.now());
            history.setIsActive(true);
            studentStatusHistoryRepository.save(history);
        }

        return saved;
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