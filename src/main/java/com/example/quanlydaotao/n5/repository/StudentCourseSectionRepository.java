package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentCourseSectionRepository extends JpaRepository<StudentCourseSection, UUID> {
    List<StudentCourseSection> findByDeletedAtIsNull();
    List<StudentCourseSection> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
    List<StudentCourseSection> findByCourseSectionIdAndDeletedAtIsNull(UUID courseSectionId);
    List<StudentCourseSection> findByStudentIdAndDeletedAtIsNull(UUID studentId);
    boolean existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(UUID studentId, UUID courseSectionId);
    long countByCourseSectionIdAndDeletedAtIsNull(UUID courseSectionId);
}
