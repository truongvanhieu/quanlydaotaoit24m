package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {
    List<CourseSection> findByDeletedAtIsNull();
    List<CourseSection> findByIsActiveAndDeletedAtIsNull(Boolean isActive);

    @Query("SELECT c FROM CourseSection c WHERE c.semester.id = :semesterId AND c.deletedAt IS NULL")
    List<CourseSection> findBySemesterIdAndDeletedAtIsNull(@Param("semesterId") UUID semesterId);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    @Query("SELECT c FROM CourseSection c WHERE c.deletedAt IS NULL " +
           "AND (:keyword IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:semesterId IS NULL OR c.semester.id = :semesterId) " +
           "AND (:status IS NULL OR LOWER(c.status) = LOWER(:status)) " +
           "AND (:isActive IS NULL OR c.isActive = :isActive) " +
           "ORDER BY c.code ASC")
    List<CourseSection> search(
            @Param("keyword") String keyword,
            @Param("semesterId") UUID semesterId,
            @Param("status") String status,
            @Param("isActive") Boolean isActive);
}
