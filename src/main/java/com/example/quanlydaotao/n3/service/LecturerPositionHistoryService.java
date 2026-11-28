package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Lecturer;
import com.example.quanlydaotao.n3.entity.LecturerPositionHistory;
import com.example.quanlydaotao.n3.entity.Position;
import com.example.quanlydaotao.n3.repository.LecturerPositionHistoryRepository;
import com.example.quanlydaotao.n3.repository.LecturerRepository;
import com.example.quanlydaotao.n3.repository.PositionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LecturerPositionHistoryService {

    private final LecturerPositionHistoryRepository repository;
    private final LecturerRepository lecturerRepository;
    private final PositionRepository positionRepository;
    private final N3AuditLogService auditLogService;

    public LecturerPositionHistoryService(
            LecturerPositionHistoryRepository repository,
            LecturerRepository lecturerRepository,
            PositionRepository positionRepository,
            N3AuditLogService auditLogService) {
        this.repository = repository;
        this.lecturerRepository = lecturerRepository;
        this.positionRepository = positionRepository;
        this.auditLogService = auditLogService;
    }

    private String describeHistory(LecturerPositionHistory item) {
        String lecName = lecturerRepository.findById(item.getLecturerId())
                .map(l -> (l.getLecturerCode() != null ? l.getLecturerCode() + " - " : "") + l.getFullName())
                .orElse(String.valueOf(item.getLecturerId()));
        String posName = positionRepository.findById(item.getPositionId())
                .map(p -> (p.getCode() != null ? p.getCode() + " - " : "") + p.getName())
                .orElse(String.valueOf(item.getPositionId()));
        return lecName + " | Chức danh: " + posName;
    }

    @Transactional(readOnly = true)
    public List<LecturerPositionHistory> getAll() {
        return repository.findByDeletedAtIsNullOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public List<LecturerPositionHistory> getDeleted() {
        return repository.findByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    @Transactional(readOnly = true)
    public LecturerPositionHistory getById(UUID id) {
        LecturerPositionHistory item = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lịch sử chức vụ giảng viên"));

        if (item.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lịch sử chức vụ giảng viên đã bị xóa");
        }
        return item;
    }

    @Transactional(readOnly = true)
    public List<LecturerPositionHistory> getByLecturer(UUID lecturerId, boolean includeDeleted) {
        if (!lecturerRepository.existsById(lecturerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên");
        }
        return includeDeleted
                ? repository.findByLecturerIdOrderByStartDateDesc(lecturerId)
                : repository.findByLecturerIdAndDeletedAtIsNullOrderByStartDateDesc(lecturerId);
    }

    @Transactional
    public LecturerPositionHistory create(LecturerPositionHistory input) {
        validate(input);

        LecturerPositionHistory item = new LecturerPositionHistory();
        item.setLecturerId(input.getLecturerId());
        item.setPositionId(input.getPositionId());
        item.setStartDate(input.getStartDate());
        item.setEndDate(input.getEndDate());
        item.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);
        LecturerPositionHistory saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_CREATE,
                "lecturer_positions_history",
                saved.getId(),
                "Thêm lịch sử chức vụ GV: " + describeHistory(saved)
        );
        return saved;
    }

    @Transactional
    public LecturerPositionHistory update(UUID id, LecturerPositionHistory input) {
        LecturerPositionHistory item = getById(id);
        validate(input);

        item.setLecturerId(input.getLecturerId());
        item.setPositionId(input.getPositionId());
        item.setStartDate(input.getStartDate());
        item.setEndDate(input.getEndDate());
        if (input.getIsActive() != null) {
            item.setIsActive(input.getIsActive());
        }
        LecturerPositionHistory saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_UPDATE,
                "lecturer_positions_history",
                saved.getId(),
                "Cập nhật lịch sử chức vụ GV: " + describeHistory(saved)
        );
        return saved;
    }

    @Transactional
    public LecturerPositionHistory endTerm(UUID id, LocalDate endDate) {
        LecturerPositionHistory item = getById(id);
        LocalDate targetEnd = endDate != null ? endDate : LocalDate.now();
        if (item.getStartDate() != null && targetEnd.isBefore(item.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
        item.setEndDate(targetEnd);
        LecturerPositionHistory saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_END_TERM,
                "lecturer_positions_history",
                saved.getId(),
                "Kết thúc nhiệm kỳ chức vụ GV: " + describeHistory(saved) + " (Ngày kết thúc: " + saved.getEndDate() + ")"
        );
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        LecturerPositionHistory item = getById(id);
        item.setDeletedAt(LocalDateTime.now());
        item.setIsActive(false);
        repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_DELETE,
                "lecturer_positions_history",
                item.getId(),
                "Xóa mềm lịch sử chức vụ GV: " + describeHistory(item)
        );
    }

    @Transactional
    public LecturerPositionHistory restore(UUID id) {
        LecturerPositionHistory item = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lịch sử chức vụ giảng viên"));
        item.setDeletedAt(null);
        item.setIsActive(true);
        LecturerPositionHistory saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_RESTORE,
                "lecturer_positions_history",
                saved.getId(),
                "Khôi phục lịch sử chức vụ GV: " + describeHistory(saved)
        );
        return saved;
    }

    private void validate(LecturerPositionHistory input) {
        if (input.getLecturerId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng chọn giảng viên");
        }
        if (input.getPositionId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng chọn chức danh");
        }

        Lecturer lecturer = lecturerRepository.findById(input.getLecturerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Giảng viên không tồn tại"));
        if (lecturer.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Giảng viên đã bị xóa");
        }

        Position position = positionRepository.findById(input.getPositionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Chức danh không tồn tại"));
        if (position.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Chức danh đã bị xóa");
        }

        if (input.getStartDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng nhập ngày bắt đầu");
        }

        if (input.getEndDate() != null
                && input.getEndDate().isBefore(input.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ngày kết thúc phải sau ngày bắt đầu");
        }
    }
}
