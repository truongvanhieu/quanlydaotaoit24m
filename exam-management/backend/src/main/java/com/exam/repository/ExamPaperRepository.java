package com.exam.repository;

import com.exam.entity.ExamPaper;
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
public interface ExamPaperRepository extends JpaRepository<ExamPaper, UUID> {

    @Query("SELECT ep FROM ExamPaper ep WHERE ep.isActive = true AND ep.deletedAt IS NULL AND " +
           "(:search IS NULL OR LOWER(ep.paperCode) LIKE :search OR " +
           "LOWER(ep.fileUrl) LIKE :search OR " +
           "LOWER(ep.exam.course.courseName) LIKE :search) AND " +
           "(:examId IS NULL OR ep.examId = :examId)")
    Page<ExamPaper> searchActive(
            @Param("search") String search,
            @Param("examId") UUID examId,
            Pageable pageable);

    @Query("SELECT ep FROM ExamPaper ep WHERE ep.id = :id AND ep.isActive = true AND ep.deletedAt IS NULL")
    Optional<ExamPaper> findActiveById(@Param("id") UUID id);

    @Query("SELECT ep FROM ExamPaper ep WHERE ep.examId = :examId AND ep.isActive = true AND ep.deletedAt IS NULL")
    List<ExamPaper> findByExamIdActive(@Param("examId") UUID examId);
}
