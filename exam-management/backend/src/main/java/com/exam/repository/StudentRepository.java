package com.exam.repository;

import com.exam.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findByIsActiveTrueOrderByStudentCodeAsc();
    Optional<Student> findByStudentCodeAndIsActiveTrue(String studentCode);
}
