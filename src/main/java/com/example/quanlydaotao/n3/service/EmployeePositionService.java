
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Employee;
import com.example.quanlydaotao.n3.entity.EmployeePosition;

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

    public EmployeePositionService(
            EmployeePositionRepository repository,
            EmployeeRepository employeeRepository,
            PositionRepository positionRepository) {

        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
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

        return repository.save(item);
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

        return repository.save(item);
    }

    // DELETE mem
    @Transactional
    public void delete(UUID id) {

        EmployeePosition item = getById(id);

        item.setDeletedAt(LocalDateTime.now());
        item.setIsActive(false);

        repository.save(item);
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

        if (!positionRepository.existsById(
                input.getPositionId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chuc vu khong ton tai"
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
