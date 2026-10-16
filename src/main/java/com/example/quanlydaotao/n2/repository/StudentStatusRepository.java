package com.example.quanlydaotao.n2.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.quanlydaotao.n2.entity.StudentStatus;

@Repository
public interface StudentStatusRepository extends JpaRepository<StudentStatus, UUID> {

    Optional<StudentStatus> findByCode(String code);

    List<StudentStatus> findByStudentId(UUID studentId);

    @Query("SELECT s FROM StudentStatus s WHERE " +
           "(:kw IS NULL OR LOWER(s.code) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<StudentStatus> searchByKeyword(@Param("kw") String keyword);
}
