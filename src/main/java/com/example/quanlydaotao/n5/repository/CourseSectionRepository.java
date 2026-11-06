package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {
    List<CourseSection> findByDeletedAtIsNull();
    List<CourseSection> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
    List<CourseSection> findBySemesterIdAndDeletedAtIsNull(UUID semesterId);
    boolean existsByCodeAndDeletedAtIsNull(String code);
}
