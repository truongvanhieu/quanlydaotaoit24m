package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.CourseClassInstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseClassInstructorRepository extends JpaRepository<CourseClassInstructor, UUID> {

    List<CourseClassInstructor> findAllByDeletedAtIsNull();

    Optional<CourseClassInstructor> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByEmployeeIdAndCourseClassIdAndDeletedAtIsNull(UUID employeeId, UUID courseClassId);

    @Query("SELECT c FROM CourseClassInstructor c WHERE c.deletedAt IS NULL " +
           "AND (:employeeId IS NULL OR c.employeeId = :employeeId) " +
           "AND (:courseClassId IS NULL OR c.courseClassId = :courseClassId) " +
           "AND (:employeeName IS NULL OR LOWER(c.employeeName) LIKE LOWER(CONCAT('%', :employeeName, '%'))) " +
           "AND (:courseClassName IS NULL OR LOWER(c.courseClassName) LIKE LOWER(CONCAT('%', :courseClassName, '%'))) " +
           "AND (:role IS NULL OR LOWER(c.role) LIKE LOWER(CONCAT('%', :role, '%'))) " +
           "AND (:isActive IS NULL OR c.isActive = :isActive)")
    List<CourseClassInstructor> search(
            @Param("employeeId") UUID employeeId,
            @Param("courseClassId") UUID courseClassId,
            @Param("employeeName") String employeeName,
            @Param("courseClassName") String courseClassName,
            @Param("role") String role,
            @Param("isActive") Boolean isActive
    );
}