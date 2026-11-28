package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentCourseSectionRepository extends JpaRepository<StudentCourseSection, UUID> {
    List<StudentCourseSection> findByDeletedAtIsNull();
    List<StudentCourseSection> findByIsActiveAndDeletedAtIsNull(Boolean isActive);

    @Query("SELECT s FROM StudentCourseSection s WHERE s.courseSection.id = :courseSectionId AND s.deletedAt IS NULL")
    List<StudentCourseSection> findByCourseSectionIdAndDeletedAtIsNull(@Param("courseSectionId") UUID courseSectionId);

    List<StudentCourseSection> findByStudentIdAndDeletedAtIsNull(UUID studentId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StudentCourseSection s WHERE s.studentId = :studentId AND s.courseSection.id = :courseSectionId AND s.deletedAt IS NULL")
    boolean existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(@Param("studentId") UUID studentId, @Param("courseSectionId") UUID courseSectionId);

    @Query("SELECT COUNT(s) FROM StudentCourseSection s WHERE s.courseSection.id = :courseSectionId AND s.deletedAt IS NULL")
    long countByCourseSectionIdAndDeletedAtIsNull(@Param("courseSectionId") UUID courseSectionId);

    @Query("SELECT s FROM StudentCourseSection s WHERE s.deletedAt IS NULL " +
           "AND (:studentId IS NULL OR s.studentId = :studentId) " +
           "AND (:courseSectionId IS NULL OR s.courseSection.id = :courseSectionId) " +
           "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
           "AND (:isActive IS NULL OR s.isActive = :isActive) " +
           "ORDER BY s.registrationDate DESC")
    List<StudentCourseSection> search(
            @Param("studentId") UUID studentId,
            @Param("courseSectionId") UUID courseSectionId,
            @Param("status") String status,
            @Param("isActive") Boolean isActive);
}
