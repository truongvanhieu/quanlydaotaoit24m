package com.exam.repository;

import com.exam.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    @Query("SELECT e FROM Exam e WHERE e.isActive = true AND e.deletedAt IS NULL AND " +
           "(:search IS NULL OR LOWER(e.course.courseName) LIKE :search OR " +
           "LOWER(e.course.courseCode) LIKE :search OR " +
           "LOWER(e.examFormat) LIKE :search) AND " +
           "(:semesterId IS NULL OR e.semesterId = :semesterId) AND " +
           "(:examStatus IS NULL OR e.examStatus = :examStatus) AND " +
           "(:examTypeId IS NULL OR e.examTypeId = :examTypeId)")
    Page<Exam> searchActive(
            @Param("search") String search,
            @Param("semesterId") UUID semesterId,
            @Param("examStatus") String examStatus,
            @Param("examTypeId") UUID examTypeId,
            Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.id = :id AND e.isActive = true AND e.deletedAt IS NULL")
    Optional<Exam> findActiveById(@Param("id") UUID id);

    @Query("SELECT e FROM Exam e WHERE e.isActive = true AND e.deletedAt IS NULL ORDER BY e.examDate DESC, e.startTime ASC")
    List<Exam> findAllActive();

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Exam e WHERE e.id = :id AND e.isActive = true AND e.deletedAt IS NULL")
    Optional<Exam> lockActiveById(@Param("id") UUID id);

    long countByExamTypeIdAndIsActiveTrueAndDeletedAtIsNull(UUID examTypeId);
}
