package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.dto.CourseSectionStudent;
import com.example.quanlydaotao.n5.dto.StudentCourseSectionDetails;
import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentCourseSectionRepository extends JpaRepository<StudentCourseSection, UUID> {

    @Query(value = """
            SELECT s.id AS registrationId,
                   s.student_id AS studentId,
                   s.course_section_id AS courseSectionId,
                   cs.code AS courseSectionCode,
                   cs.name AS courseSectionName,
                   s.registration_date AS registrationDate,
                   s.status AS status,
                   s.attempt_number AS attemptNumber,
                   s.is_active AS isActive
            FROM student_course_sections s
            LEFT JOIN course_sections cs ON cs.id = s.course_section_id
            WHERE s.course_section_id = :courseSectionId
              AND s.is_active = 1
            ORDER BY s.registration_date DESC, s.created_at DESC
            """, nativeQuery = true)
    List<CourseSectionStudent> findStudentsByCourseSectionId(
            @Param("courseSectionId") UUID courseSectionId);

    @Query(value = """
            SELECT s.id AS id,
                   s.student_id AS studentId,
                   s.course_section_id AS courseSectionId,
                   cs.code AS courseSectionCode,
                   cs.name AS courseSectionName,
                   s.registration_date AS registrationDate,
                   s.registration_type AS registrationType,
                   s.status AS status,
                   s.attempt_number AS attemptNumber,
                   s.note AS note,
                   s.created_at AS createdAt,
                   s.is_active AS isActive
            FROM student_course_sections s
            LEFT JOIN course_sections cs ON cs.id = s.course_section_id
            WHERE (:studentId IS NULL OR s.student_id = :studentId)
              AND (:courseSectionId IS NULL OR s.course_section_id = :courseSectionId)
              AND (:id IS NULL OR s.id = :id)
            ORDER BY s.registration_date DESC, s.created_at DESC
            """, nativeQuery = true)
    List<StudentCourseSectionDetails> findDetails(
            @Param("studentId") UUID studentId,
            @Param("courseSectionId") UUID courseSectionId,
            @Param("id") UUID id);

    boolean existsByStudentIdAndCourseSectionIdAndIsActiveTrue(UUID studentId, UUID courseSectionId);

    boolean existsByStudentIdAndCourseSectionIdAndIsActiveTrueAndIdNot(
            UUID studentId, UUID courseSectionId, UUID id);

    boolean existsByCourseSectionId(UUID courseSectionId);

    long countByCourseSectionIdAndIsActiveTrue(UUID courseSectionId);

    long countByCourseSectionIdAndIsActiveTrueAndIdNot(UUID courseSectionId, UUID id);
}
