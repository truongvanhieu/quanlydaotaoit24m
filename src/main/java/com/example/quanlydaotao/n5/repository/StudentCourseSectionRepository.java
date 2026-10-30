package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.dto.StudentCourseSectionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentCourseSectionRepository extends JpaRepository<StudentCourseSection, UUID> {

    @Query(value = """
            SELECT s.id AS id,
                   s.student_id AS studentId,
                   st.student_code AS studentCode,
                   st.full_name AS studentName,
                   s.course_section_id AS courseSectionId,
                   cs.code AS courseSectionCode,
                   cs.name AS courseSectionName,
                   s.registration_date AS registrationDate,
                   s.registration_type AS registrationType,
                   LOWER(s.status) AS status,
                   s.is_active AS isActive
            FROM student_course_sections s
            LEFT JOIN students st ON st.id = s.student_id
            LEFT JOIN course_sections cs ON cs.id = s.course_section_id
            WHERE s.deleted_at IS NULL
              AND (:studentId IS NULL OR s.student_id = :studentId)
              AND (:courseSectionId IS NULL OR s.course_section_id = :courseSectionId)
              AND (:status IS NULL OR LOWER(s.status) = LOWER(:status))
              AND (:isActive IS NULL OR s.is_active = :isActive)
            ORDER BY s.registration_date DESC
            """, nativeQuery = true)
    List<StudentCourseSectionDetails> findDetails(
            @Param("studentId") UUID studentId,
            @Param("courseSectionId") UUID courseSectionId,
            @Param("status") String status,
            @Param("isActive") Boolean isActive
    );

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