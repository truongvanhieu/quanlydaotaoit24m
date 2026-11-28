package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    List<Semester> findByDeletedAtIsNull();
    List<Semester> findByIsActiveAndDeletedAtIsNull(Boolean isActive);

    @Query("SELECT s FROM Semester s WHERE s.schoolYear.id = :schoolYearId AND s.deletedAt IS NULL")
    List<Semester> findBySchoolYearIdAndDeletedAtIsNull(@Param("schoolYearId") UUID schoolYearId);

    @Query("SELECT s FROM Semester s WHERE s.deletedAt IS NULL " +
           "AND (:keyword IS NULL OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.schoolYearName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:schoolYearId IS NULL OR s.schoolYear.id = :schoolYearId) " +
           "AND (:isActive IS NULL OR s.isActive = :isActive) " +
           "ORDER BY s.code DESC")
    List<Semester> search(
            @Param("keyword") String keyword,
            @Param("schoolYearId") UUID schoolYearId,
            @Param("isActive") Boolean isActive);
}
