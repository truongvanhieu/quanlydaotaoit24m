package com.exam.repository;

import com.exam.entity.ExamRegistration;
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
public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration, UUID> {

    @Query("SELECT r FROM ExamRegistration r WHERE r.isActive = true AND r.deletedAt IS NULL AND " +
           "(:search IS NULL OR LOWER(r.rollNumber) LIKE :search OR " +
           "LOWER(r.student.studentCode) LIKE :search OR " +
           "LOWER(r.student.fullName) LIKE :search OR " +
           "LOWER(r.exam.course.courseName) LIKE :search) AND " +
           "(:examId IS NULL OR r.examId = :examId) AND " +
           "(:examRoomId IS NULL OR r.examRoomId = :examRoomId) AND " +
           "(:studentId IS NULL OR r.studentId = :studentId) AND " +
           "(:groupType IS NULL OR (:groupType = 'single' AND r.mergedGroupId IS NULL) OR (:groupType = 'merged' AND r.mergedGroupId IS NOT NULL)) AND " +
           "(:groupId IS NULL OR r.mergedGroupId = :groupId) AND " +
           "(:studentClass IS NULL OR r.student.className = :studentClass)")
    Page<ExamRegistration> searchActive(
            @Param("search") String search,
            @Param("examId") UUID examId,
            @Param("examRoomId") UUID examRoomId,
            @Param("studentId") UUID studentId,
            @Param("groupType") String groupType,
            @Param("groupId") UUID groupId,
            @Param("studentClass") String studentClass,
            Pageable pageable);

    @Query("SELECT r FROM ExamRegistration r WHERE r.id = :id AND r.isActive = true AND r.deletedAt IS NULL")
    Optional<ExamRegistration> findActiveById(@Param("id") UUID id);

    @Query("SELECT r FROM ExamRegistration r WHERE r.isActive = true AND r.deletedAt IS NULL")
    List<ExamRegistration> findAllActive();

    @Query("SELECT r FROM ExamRegistration r WHERE r.examId = :examId AND r.isActive = true AND r.deletedAt IS NULL")
    List<ExamRegistration> findByExamIdActive(@Param("examId") UUID examId);

    long countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(UUID examRoomId);

    long countByExamIdAndIsActiveTrueAndDeletedAtIsNull(UUID examId);

    @Query("SELECT r FROM ExamRegistration r WHERE r.examId = :examId AND r.studentId = :studentId AND r.isActive = true AND r.deletedAt IS NULL")
    List<ExamRegistration> findByExamIdAndStudentIdActive(@Param("examId") UUID examId, @Param("studentId") UUID studentId);

    @Query("SELECT r FROM ExamRegistration r WHERE r.studentId = :studentId AND r.isActive = true AND r.deletedAt IS NULL ORDER BY r.exam.examDate ASC, r.exam.startTime ASC")
    List<ExamRegistration> findByStudentIdActive(@Param("studentId") UUID studentId);
}
