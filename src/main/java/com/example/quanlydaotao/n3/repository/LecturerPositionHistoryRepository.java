package com.example.quanlydaotao.n3.repository;

import com.example.quanlydaotao.n3.entity.LecturerPositionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LecturerPositionHistoryRepository
        extends JpaRepository<LecturerPositionHistory, UUID> {

    List<LecturerPositionHistory> findByDeletedAtIsNullOrderByStartDateDesc();

    List<LecturerPositionHistory> findByLecturerIdAndDeletedAtIsNullOrderByStartDateDesc(
            UUID lecturerId
    );

    List<LecturerPositionHistory> findByLecturerIdOrderByStartDateDesc(
            UUID lecturerId
    );

    List<LecturerPositionHistory> findByDeletedAtIsNotNullOrderByDeletedAtDesc();
}
