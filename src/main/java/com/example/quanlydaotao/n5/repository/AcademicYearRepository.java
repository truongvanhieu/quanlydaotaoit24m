package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    List<AcademicYear> findByDeletedAtIsNull();
    List<AcademicYear> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
    boolean existsByCodeAndDeletedAtIsNull(String code);

    @Query("SELECT a FROM AcademicYear a WHERE a.deletedAt IS NULL " +
           "AND (:keyword IS NULL OR LOWER(a.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:isActive IS NULL OR a.isActive = :isActive) " +
           "ORDER BY a.code DESC")
    List<AcademicYear> search(@Param("keyword") String keyword, @Param("isActive") Boolean isActive);
}
