package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {

    List<SchoolYear> findAllByDeletedAtIsNull();

    Optional<SchoolYear> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    @Query("SELECT s FROM SchoolYear s WHERE s.deletedAt IS NULL " +
           "AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:isActive IS NULL OR s.isActive = :isActive)")
    List<SchoolYear> search(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive
    );
}