package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {
    List<SchoolYear> findByDeletedAtIsNull();
    List<SchoolYear> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
    boolean existsByCodeAndDeletedAtIsNull(String code);
}
