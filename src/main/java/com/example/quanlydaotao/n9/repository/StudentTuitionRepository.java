package com.example.quanlydaotao.n9.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n9.entity.StudentTuition;

public interface StudentTuitionRepository extends JpaRepository<StudentTuition, UUID> {

    List<StudentTuition> findByDeletedAtIsNullOrderByCreatedAtDesc();

    List<StudentTuition> findByTuitionFeeIdAndDeletedAtIsNull(UUID tuitionFeeId);
}
