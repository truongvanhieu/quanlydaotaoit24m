
package com.example.quanlydaotao.n3.repository;

import com.example.quanlydaotao.n3.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, UUID> {

    List<Employee> findByDeletedAtIsNullOrderByFullNameAsc();

    List<Employee> findByDepartmentIdAndDeletedAtIsNull(
            UUID departmentId
    );

    boolean existsByCodeIgnoreCase(String code);
}
