package com.example.quanlydaotao.n2.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.quanlydaotao.n2.entity.StudentClass;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass, UUID> {

    Optional<StudentClass> findByCode(String code);

    @Query("SELECT c FROM StudentClass c WHERE " +
           "(:kw IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<StudentClass> searchByKeyword(@Param("kw") String keyword);
}
