package com.example.quanlydaotao.n2.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.quanlydaotao.n2.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByCode(String code);

    List<Student> findByStudentClassessId(UUID studentClassessId);

    @Query("SELECT s FROM Student s WHERE " +
           "(:kw IS NULL OR LOWER(s.code) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(s.personalIdentificationNumber) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(s.status) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<Student> searchByKeyword(@Param("kw") String keyword);
}