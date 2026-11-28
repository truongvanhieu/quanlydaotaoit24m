package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.dto.CourseClassInstructorDetails;
import com.example.quanlydaotao.n5.entity.CourseClassInstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseClassInstructorRepository extends JpaRepository<CourseClassInstructor, UUID> {

    @Query(value = """
            SELECT lcc.id AS id,
                   lcc.employee_id AS employeeId,
                   i.code AS instructorCode,
                   i.name AS instructorName,
                   lcc.course_class_id AS courseClassId,
                   cs.code AS courseSectionCode,
                   cs.name AS courseSectionName,
                   lcc.teaching_hours AS teachingHours,
                   lcc.created_at AS createdAt,
                   lcc.is_active AS isActive,
                   lcc.role AS role
            FROM lecturer_course_classes lcc
            LEFT JOIN course_sections cs ON cs.id = lcc.course_class_id
            LEFT JOIN instructors i ON i.id = lcc.employee_id
            WHERE (:id IS NULL OR lcc.id = :id)
              AND (:employeeId IS NULL OR lcc.employee_id = :employeeId)
              AND (:instructorCode IS NULL OR LOWER(i.code) LIKE LOWER(CONCAT('%', :instructorCode, '%')))
              AND (:instructorName IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :instructorName, '%')))
              AND (:courseClassName IS NULL OR LOWER(cs.name) LIKE LOWER(CONCAT('%', :courseClassName, '%')))
              AND (:role IS NULL OR LOWER(lcc.role) LIKE LOWER(CONCAT('%', :role, '%')))
            ORDER BY lcc.created_at DESC
            """, nativeQuery = true)
    List<CourseClassInstructorDetails> findDetails(
            @Param("id") UUID id,
            @Param("employeeId") UUID employeeId,
            @Param("instructorCode") String instructorCode,
            @Param("instructorName") String instructorName,
            @Param("courseClassName") String courseClassName,
            @Param("role") String role);

    boolean existsByEmployeeIdAndCourseClassId(UUID employeeId, UUID courseClassId);

    boolean existsByEmployeeIdAndCourseClassIdAndIdNot(UUID employeeId, UUID courseClassId, UUID id);

    boolean existsByCourseClassId(UUID courseClassId);
}
