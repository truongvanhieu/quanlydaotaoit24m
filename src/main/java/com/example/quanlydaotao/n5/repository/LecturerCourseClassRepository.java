package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.LecturerCourseClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LecturerCourseClassRepository extends JpaRepository<LecturerCourseClass, UUID> {
    List<LecturerCourseClass> findByDeletedAtIsNull();
    List<LecturerCourseClass> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
    List<LecturerCourseClass> findByEmployeeIdAndDeletedAtIsNull(UUID employeeId);
    List<LecturerCourseClass> findByCourseSectionIdAndDeletedAtIsNull(UUID courseSectionId);
}
