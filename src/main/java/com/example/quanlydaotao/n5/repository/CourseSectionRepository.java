package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.CourseSection;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {

    @Query("SELECT c FROM CourseSection c WHERE " +
            "(:keyword IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:semesterId IS NULL OR c.semesterId = :semesterId) " +
            "ORDER BY c.createdAt DESC")
    List<CourseSection> search(
            @Param("keyword") String keyword,
            @Param("semesterId") UUID semesterId);

    List<CourseSection> findAllByOrderByCreatedAtDesc();

    boolean existsByCode(String code);

    boolean existsBySemesterId(UUID semesterId);

    @Query(value = """
            SELECT CAST(CASE WHEN EXISTS (
                SELECT 1 FROM schedules WHERE course_section_id = :courseSectionId
            ) THEN 1 ELSE 0 END AS bit)
            """, nativeQuery = true)
    boolean hasSchedule(@Param("courseSectionId") UUID courseSectionId);
}
