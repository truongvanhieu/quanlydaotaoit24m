package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    List<Semester> findByDeletedAtIsNull();
    List<Semester> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
    List<Semester> findBySchoolYearIdAndDeletedAtIsNull(UUID schoolYearId);
}
