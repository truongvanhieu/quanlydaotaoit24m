package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentCourseSectionRepository extends JpaRepository<StudentCourseSection, UUID> {

    List<StudentCourseSection> findAllByDeletedAtIsNull();

    Optional<StudentCourseSection> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(UUID studentId, UUID courseSectionId);

    @Query("SELECT s FROM StudentCourseSection s WHERE s.deletedAt IS NULL " +
           "AND (:studentId IS NULL OR s.studentId = :studentId) " +
           "AND (:courseSectionId IS NULL OR s.courseSectionId = :courseSectionId) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:registrationType IS NULL OR s.registrationType = :registrationType) " +
           "AND (:isActive IS NULL OR s.isActive = :isActive)")
    List<StudentCourseSection> search(
            @Param("studentId") UUID studentId,
            @Param("courseSectionId") UUID courseSectionId,
            @Param("status") String status,
            @Param("registrationType") String registrationType,
            @Param("isActive") Boolean isActive
    );
}