package com.exam.repository;

import com.exam.entity.ExamRoom;
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
public interface ExamRoomRepository extends JpaRepository<ExamRoom, UUID> {

    @Query("SELECT er FROM ExamRoom er WHERE er.isActive = true AND er.deletedAt IS NULL AND " +
           "(:search IS NULL OR LOWER(er.room.roomNumber) LIKE :search OR " +
           "LOWER(er.room.building) LIKE :search OR " +
           "LOWER(er.exam.course.courseName) LIKE :search) AND " +
           "(:examId IS NULL OR er.examId = :examId)")
    Page<ExamRoom> searchActive(
            @Param("search") String search,
            @Param("examId") UUID examId,
            Pageable pageable);

    @Query("SELECT er FROM ExamRoom er WHERE er.id = :id AND er.isActive = true AND er.deletedAt IS NULL")
    Optional<ExamRoom> findActiveById(@Param("id") UUID id);

    @Query("SELECT er FROM ExamRoom er WHERE er.isActive = true AND er.deletedAt IS NULL")
    List<ExamRoom> findAllActive();

    @Query("SELECT er FROM ExamRoom er WHERE er.examId = :examId AND er.roomId = :roomId AND er.isActive = true AND er.deletedAt IS NULL")
    List<ExamRoom> findByExamIdAndRoomIdActive(@Param("examId") UUID examId, @Param("roomId") UUID roomId);

    long countByExamIdAndIsActiveTrueAndDeletedAtIsNull(UUID examId);
}
