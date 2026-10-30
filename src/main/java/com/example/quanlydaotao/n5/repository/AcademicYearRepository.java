
package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcademicYearRepository
        extends JpaRepository<AcademicYear, UUID> {

    List<AcademicYear> findByDeletedAtIsNullOrderByStartDateDesc();

    List<AcademicYear> findByDeletedAtIsNullAndIsActiveOrderByStartDateDesc(
            Boolean isActive);

    boolean existsByCodeAndDeletedAtIsNull(String code);
}