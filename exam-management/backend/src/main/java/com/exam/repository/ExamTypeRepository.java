package com.exam.repository;

import com.exam.entity.ExamType;
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
public interface ExamTypeRepository extends JpaRepository<ExamType, UUID> {

    @Query("SELECT e FROM ExamType e WHERE e.isActive = true AND e.deletedAt IS NULL AND " +
           "(:search IS NULL OR LOWER(e.name) LIKE :search OR " +
           "LOWER(e.description) LIKE :search)")
    Page<ExamType> searchActive(@Param("search") String search, Pageable pageable);

    @Query("SELECT e FROM ExamType e WHERE e.id = :id AND e.isActive = true AND e.deletedAt IS NULL")
    Optional<ExamType> findActiveById(@Param("id") UUID id);

    @Query("SELECT e FROM ExamType e WHERE e.isActive = true AND e.deletedAt IS NULL ORDER BY e.name ASC")
    List<ExamType> findAllActive();
}
