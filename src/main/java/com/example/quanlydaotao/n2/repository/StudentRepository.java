package com.example.quanlydaotao.n2.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n2.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            String studentCode,
            String fullName,
            String email,
            String phone);
}