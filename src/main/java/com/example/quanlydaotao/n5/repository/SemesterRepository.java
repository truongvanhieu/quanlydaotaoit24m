package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {

    @Query("SELECT s FROM Semester s WHERE " +
           "(:keyword IS NULL OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:schoolYearId IS NULL OR s.schoolYearId = :schoolYearId) " +
           "ORDER BY s.startDate DESC")
    List<Semester> search(
            @Param("keyword") String keyword,
            @Param("schoolYearId") UUID schoolYearId
    );

    List<Semester> findAllByOrderByStartDateDesc();

    boolean existsBySchoolYearId(UUID schoolYearId);

    boolean existsByCode(String code);
}