package com.example.quanlydaotao.n2.repository;

import java.util.List;
<<<<<<< HEAD
import java.util.Optional;
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n2.entity.Student;

public interface StudentRepository extends JpaRepository<Student, UUID> {

<<<<<<< HEAD
    Optional<Student> findByEmailIgnoreCase(String email);

=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    List<Student> findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            String studentCode,
            String fullName,
            String email,
            String phone);
}