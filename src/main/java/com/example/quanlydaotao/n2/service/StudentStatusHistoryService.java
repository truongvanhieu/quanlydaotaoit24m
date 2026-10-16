package com.example.quanlydaotao.n2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.StudentStatusHistory;
import com.example.quanlydaotao.n2.repository.StudentStatusHistoryRepository;

@Service
@Transactional
public class StudentStatusHistoryService {

    private final StudentStatusHistoryRepository repository;

    public StudentStatusHistoryService(StudentStatusHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StudentStatusHistory> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public StudentStatusHistory getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi lịch sử trạng thái với id: " + id));
    }

    @Transactional(readOnly = true)
    public List<StudentStatusHistory> getByStudentId(UUID studentId) {
        return repository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<StudentStatusHistory> getByStatusId(UUID statusId) {
        return repository.findByStatusId(statusId);
    }

    /**
     * Mỗi khi trạng thái thay đổi, tạo một bản ghi mới, không sửa/xóa bản ghi lịch sử cũ.
     */
    public StudentStatusHistory recordNewHistory(StudentStatusHistory history) {
        history.setId(null); // Luôn tạo mới bản ghi lịch sử
        if (history.getCreatedAt() == null) {
            history.setCreatedAt(LocalDateTime.now());
        }
        history.setIsActive(true);
        return repository.save(history);
    }

    public StudentStatusHistory update(UUID id, StudentStatusHistory history) {
        history.setId(id);
        return repository.save(history);
    }

    public void delete(UUID id) {
        StudentStatusHistory history = getById(id);
        history.setIsActive(false);
        history.setDeletedAt(LocalDateTime.now());
        repository.save(history);
    }

    public void hardDelete(UUID id) {
        repository.deleteById(id);
    }
}
