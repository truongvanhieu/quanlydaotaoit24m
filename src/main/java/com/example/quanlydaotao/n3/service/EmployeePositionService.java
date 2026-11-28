
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Employee;
import com.example.quanlydaotao.n3.entity.EmployeePosition;
import com.example.quanlydaotao.n3.entity.Position;

import com.example.quanlydaotao.n3.repository.EmployeeRepository;
import com.example.quanlydaotao.n3.repository.EmployeePositionRepository;
import com.example.quanlydaotao.n3.repository.PositionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeePositionService {

    private final EmployeePositionRepository repository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final N3AuditLogService auditLogService;

    public EmployeePositionService(
            EmployeePositionRepository repository,
            EmployeeRepository employeeRepository,
            PositionRepository positionRepository,
            N3AuditLogService auditLogService) {

        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.auditLogService = auditLogService;
    }

    private String describeAssignment(EmployeePosition item) {
        String empName = employeeRepository.findById(item.getEmployeeId())
                .map(e -> (e.getCode() != null ? e.getCode() + " - " : "") + e.getFullName())
                .orElse(String.valueOf(item.getEmployeeId()));
        String posName = positionRepository.findById(item.getPositionId())
                .map(p -> (p.getCode() != null ? p.getCode() + " - " : "") + p.getName())
                .orElse(String.valueOf(item.getPositionId()));
        return empName + " | Chức vụ: " + posName;
    }

    // GET tat ca phan cong
    @Transactional(readOnly = true)
    public List<EmployeePosition> getAll() {

        return repository
                .findByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    // GET theo ID
    @Transactional(readOnly = true)
    public EmployeePosition getById(UUID id) {

        EmployeePosition item = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Khong tim thay vi tri cong tac"
                        )
                );

        if (item.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vi tri cong tac da bi xoa"
            );
        }

        return item;
    }

    // GET lich su vi tri cua mot nhan vien
    @Transactional(readOnly = true)
    public List<EmployeePosition> getByEmployee(UUID employeeId) {

        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Khong tim thay nhan vien"
                        )
                );

        if (employee.getDeletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Nhan vien da bi xoa"
            );
        }

        return repository
                .findByEmployeeIdAndDeletedAtIsNullOrderByStartDateDesc(
                        employeeId
                );
    }

    // POST
    @Transactional
    public EmployeePosition create(EmployeePosition input) {

        validate(input);

        EmployeePosition item = new EmployeePosition();
        item.copyBusinessFields(input);

        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }

        EmployeePosition saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_CREATE,
                "employee_positions",
                saved.getId(),
                "Phân công vị trí công tác: " + describeAssignment(saved)
        );
        return saved;
    }

    // PUT
    @Transactional
    public EmployeePosition update(
            UUID id,
            EmployeePosition input) {

        EmployeePosition item = getById(id);

        validate(input);

        Boolean oldStatus = item.getIsActive();

        item.copyBusinessFields(input);

        if (input.getIsActive() == null) {
            item.setIsActive(oldStatus);
        }

        EmployeePosition saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_UPDATE,
                "employee_positions",
                saved.getId(),
                "Cập nhật vị trí công tác: " + describeAssignment(saved)
                        + (saved.getEndDate() != null ? " (Kết thúc: " + saved.getEndDate() + ")" : " (Đang đương nhiệm)")
        );
        return saved;
    }

    // GET danh sach da xoa mem
    @Transactional(readOnly = true)
    public List<EmployeePosition> getDeleted() {
        return repository.findByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    // GET toan bo lich su vi tri cua nhan vien (ke ca khi nhan vien da xoa mem)
    @Transactional(readOnly = true)
    public List<EmployeePosition> getHistoryByEmployee(
            UUID employeeId,
            boolean includeDeleted) {

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Khong tim thay nhan vien"
            );
        }

        return includeDeleted
                ? repository.findByEmployeeIdOrderByStartDateDesc(employeeId)
                : repository.findByEmployeeIdAndDeletedAtIsNullOrderByStartDateDesc(employeeId);
    }

    // PUT ket thuc nhiem ky (cap nhat end_date, giu lai lich su)
    @Transactional
    public EmployeePosition endTerm(UUID id, LocalDateTime endDate) {
        EmployeePosition item = getById(id);
        LocalDateTime targetEnd = endDate != null ? endDate : LocalDateTime.now();

        if (item.getStartDate() != null
                && targetEnd.isBefore(item.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngay ket thuc phai sau ngay bat dau"
            );
        }

        item.setEndDate(targetEnd);
        EmployeePosition saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_END_TERM,
                "employee_positions",
                saved.getId(),
                "Kết thúc nhiệm kỳ vị trí công tác: " + describeAssignment(saved) + " (Ngày kết thúc: " + saved.getEndDate() + ")"
        );
        return saved;
    }

    // PUT khoi phuc ban ghi da xoa mem
    @Transactional
    public EmployeePosition restore(UUID id) {
        EmployeePosition item = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Khong tim thay vi tri cong tac"
                        )
                );

        item.setDeletedAt(null);
        item.setIsActive(true);
        EmployeePosition saved = repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_RESTORE,
                "employee_positions",
                saved.getId(),
                "Khôi phục vị trí công tác đã xóa mềm: " + describeAssignment(saved)
        );
        return saved;
    }

    // DELETE mem
    @Transactional
    public void delete(UUID id) {

        EmployeePosition item = getById(id);

        item.setDeletedAt(LocalDateTime.now());
        item.setIsActive(false);

        repository.save(item);
        auditLogService.record(
                N3AuditLogService.ACTION_DELETE,
                "employee_positions",
                item.getId(),
                "Xóa mềm vị trí công tác: " + describeAssignment(item)
        );
    }

    // Validation
    private void validate(EmployeePosition input) {

        if (input.getEmployeeId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chua chon nhan vien"
            );
        }

        if (input.getPositionId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chua chon chuc vu"
            );
        }

        Employee employee = employeeRepository
                .findById(input.getEmployeeId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Nhan vien khong ton tai"
                        )
                );

        if (employee.getDeletedAt() != null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nhan vien da bi xoa"
            );
        }

        Position position = positionRepository
                .findById(input.getPositionId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Chuc vu khong ton tai"
                        )
                );

        if (position.getDeletedAt() != null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chuc vu da bi xoa"
            );
        }

        if (input.getStartDate() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chua nhap ngay bat dau"
            );
        }

        if (input.getEndDate() != null
                && input.getEndDate().isBefore(
                        input.getStartDate())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngay ket thuc phai sau ngay bat dau"
            );
        }
    }
}
