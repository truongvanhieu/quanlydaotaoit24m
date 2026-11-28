package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.LecturerCourseClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LecturerCourseClassRepository extends JpaRepository<LecturerCourseClass, UUID> {
    List<LecturerCourseClass> findByDeletedAtIsNull();
    List<LecturerCourseClass> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
    List<LecturerCourseClass> findByEmployeeIdAndDeletedAtIsNull(UUID employeeId);

    @Query("SELECT l FROM LecturerCourseClass l WHERE l.courseSection.id = :courseSectionId AND l.deletedAt IS NULL")
    List<LecturerCourseClass> findByCourseSectionIdAndDeletedAtIsNull(@Param("courseSectionId") UUID courseSectionId);

    @Query("SELECT l FROM LecturerCourseClass l WHERE l.deletedAt IS NULL " +
           "AND ((l.employeeId IS NOT NULL AND l.employeeId = :empId) OR (l.lecturerId IS NOT NULL AND l.lecturerId = :empId)) " +
           "AND l.courseSection.id = :csId")
    List<LecturerCourseClass> findExistingAssignment(@Param("empId") UUID empId, @Param("csId") UUID csId);

    @Query("SELECT l FROM LecturerCourseClass l WHERE l.deletedAt IS NULL " +
           "AND (:employeeId IS NULL OR l.employeeId = :employeeId OR l.lecturerId = :employeeId) " +
           "AND (:courseClassId IS NULL OR (l.courseSection IS NOT NULL AND l.courseSection.id = :courseClassId)) " +
           "AND (:employeeName IS NULL OR LOWER(l.employeeName) LIKE LOWER(CONCAT('%', :employeeName, '%'))) " +
           "AND (:courseClassName IS NULL OR LOWER(l.courseClassName) LIKE LOWER(CONCAT('%', :courseClassName, '%'))) " +
           "AND (:role IS NULL OR LOWER(l.role) LIKE LOWER(CONCAT('%', :role, '%'))) " +
           "AND (:isActive IS NULL OR l.isActive = :isActive) " +
           "ORDER BY l.createdAt DESC")
    List<LecturerCourseClass> search(
            @Param("employeeId") UUID employeeId,
            @Param("courseClassId") UUID courseClassId,
            @Param("employeeName") String employeeName,
            @Param("courseClassName") String courseClassName,
            @Param("role") String role,
            @Param("isActive") Boolean isActive);
}
