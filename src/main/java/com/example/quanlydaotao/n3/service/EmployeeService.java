
package com.example.quanlydaotao.n3.service;

import com.example.quanlydaotao.n3.entity.Department;
import com.example.quanlydaotao.n3.entity.Employee;
import com.example.quanlydaotao.n3.entity.N3User;
import com.example.quanlydaotao.n3.repository.DepartmentRepository;
import com.example.quanlydaotao.n3.repository.EmployeeRepository;
import com.example.quanlydaotao.n3.repository.N3UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final N3UserRepository userRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            N3UserRepository userRepository) {

        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    // GET danh sach va tim kiem
    @Transactional(readOnly = true)
    public List<Employee> getAll(String keyword) {

        List<Employee> data =
                employeeRepository
                        .findByDeletedAtIsNullOrderByFullNameAsc();

        if (keyword == null || keyword.isBlank()) {
            return data;
        }

        String key = keyword.trim().toLowerCase(Locale.ROOT);

        return data.stream()
                .filter(e ->
                        contains(e.getCode(), key)
                        || contains(e.getFullName(), key)
                        || contains(e.getEmail(), key)
                        || contains(e.getPhone(), key)
                        || contains(e.getSpecialization(), key)
                )
                .toList();
    }

    private boolean contains(String value, String keyword) {
        return value != null &&
                value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    // GET theo ID
    @Transactional(readOnly = true)
    public Employee getById(UUID id) {

        Employee employee = employeeRepository.findById(id)
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

        return employee;
    }

    // GET theo khoa
    @Transactional(readOnly = true)
    public List<Employee> getByDepartment(UUID departmentId) {

        return employeeRepository
                .findByDepartmentIdAndDeletedAtIsNull(
                        departmentId
                );
    }

    // POST
    @Transactional
    public Employee create(Employee input) {

        validate(input);

        if (employeeRepository.existsByCodeIgnoreCaseAndDeletedAtIsNull(
                input.getCode().trim())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ma nhan vien da ton tai"
            );
        }

        Employee employee = new Employee();
        employee.copyBusinessFields(input);
        employee.setCode(input.getCode().trim());
        employee.setFullName(input.getFullName().trim());

        if (employee.getIsActive() == null) {
            employee.setIsActive(true);
        }

        return employeeRepository.save(employee);
    }

    // PUT
    @Transactional
    public Employee update(UUID id, Employee input) {

        Employee employee = getById(id);

        validate(input);

        String newCode = input.getCode().trim();

        if (!newCode.equalsIgnoreCase(employee.getCode())
                && employeeRepository.existsByCodeIgnoreCaseAndDeletedAtIsNull(newCode)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ma nhan vien da ton tai"
            );
        }

        Boolean oldStatus = employee.getIsActive();

        employee.copyBusinessFields(input);

        employee.setCode(newCode);
        employee.setFullName(input.getFullName().trim());

        if (input.getIsActive() == null) {
            employee.setIsActive(oldStatus);
        }

        return employeeRepository.save(employee);
    }

    // DELETE mem
    @Transactional
    public void delete(UUID id) {

        Employee employee = getById(id);

        employee.setDeletedAt(LocalDateTime.now());
        employee.setIsActive(false);

        employeeRepository.save(employee);
    }

    // Kiem tra du lieu
    private void validate(Employee employee) {

        if (employee.getCode() == null
                || employee.getCode().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma nhan vien khong duoc de trong"
            );
        }

        if (employee.getFullName() == null
                || employee.getFullName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ho ten khong duoc de trong"
            );
        }

        if (employee.getCode().trim().length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma nhan vien qua 100 ky tu"
            );
        }

        if (employee.getFullName().trim().length() > 255) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ho ten qua 255 ky tu"
            );
        }

        if (employee.getDateOfBirth() != null
                && employee.getDateOfBirth()
                           .isAfter(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngay sinh khong hop le"
            );
        }

        if (employee.getEmail() != null
                && !employee.getEmail().isBlank()
                && !employee.getEmail().contains("@")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email khong hop le"
            );
        }

        if (employee.getSalaryCoefficient() != null) {

            BigDecimal salary = employee.getSalaryCoefficient();

            if (salary.compareTo(BigDecimal.ZERO) < 0
                    || salary.compareTo(
                            new BigDecimal("99.99")) > 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "He so luong phai tu 0 den 99.99"
                );
            }
        }

        if (employee.getDepartmentId() != null) {
            Department department = departmentRepository
                    .findById(employee.getDepartmentId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Khoa / vien khong ton tai"
                            )
                    );

            if (department.getDeletedAt() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Khoa / vien da bi xoa"
                );
            }
        }

        if (employee.getUserId() != null) {
            N3User user = userRepository
                    .findById(employee.getUserId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Tai khoan lien ket khong ton tai"
                            )
                    );

            if (user.getDeletedAt() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tai khoan lien ket da bi xoa"
                );
            }
        }
    }
}
