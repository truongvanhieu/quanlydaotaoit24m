package com.example.quanlydaotao.n9.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n9.entity.TuitionFee;

public interface TuitionFeeRepository extends JpaRepository<TuitionFee, UUID> {

    List<TuitionFee> findByDeletedAtIsNullOrderByEffectiveDateDesc();

    List<TuitionFee> findByDeletedAtIsNullAndCourseYearContainingIgnoreCaseOrderByEffectiveDateDesc(String courseYear);
}
