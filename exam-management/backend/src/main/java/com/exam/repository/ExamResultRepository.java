package com.exam.repository;

import com.exam.entity.ExamResult;
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
public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {

    @Query("SELECT res FROM ExamResult res WHERE res.isActive = true AND res.deletedAt IS NULL AND " +
           "(:search IS NULL OR LOWER(res.registration.rollNumber) LIKE :search OR " +
           "LOWER(res.registration.student.studentCode) LIKE :search OR " +
           "LOWER(res.registration.student.fullName) LIKE :search OR " +
           "LOWER(res.registration.exam.course.courseName) LIKE :search) AND " +
           "(:examId IS NULL OR res.registration.examId = :examId) AND " +
           "(:status IS NULL OR res.status = :status) AND " +
           "(:isLocked IS NULL OR res.isLocked = :isLocked) AND " +
           "(:appealStatus IS NULL OR res.appealStatus = :appealStatus)")
    Page<ExamResult> searchActive(
            @Param("search") String search,
            @Param("examId") UUID examId,
            @Param("status") String status,
            @Param("isLocked") Boolean isLocked,
            @Param("appealStatus") String appealStatus,
            Pageable pageable);

    @Query("SELECT res FROM ExamResult res WHERE res.id = :id AND res.isActive = true AND res.deletedAt IS NULL")
    Optional<ExamResult> findActiveById(@Param("id") UUID id);

    @Query("SELECT res FROM ExamResult res WHERE res.registrationId = :registrationId AND res.isActive = true AND res.deletedAt IS NULL")
    Optional<ExamResult> findByRegistrationIdActive(@Param("registrationId") UUID registrationId);

    @Query("SELECT res FROM ExamResult res WHERE res.registration.studentId = :studentId AND res.isActive = true AND res.deletedAt IS NULL")
    List<ExamResult> findByStudentIdActive(@Param("studentId") UUID studentId);
}
