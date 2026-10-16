package com.example.quanlydaotao.n2.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanlydaotao.n2.entity.StudentClasseSection;

@Repository
public interface StudentClasseSectionRepository extends JpaRepository<StudentClasseSection, UUID> {

    List<StudentClasseSection> findByStudentId(UUID studentId);

    List<StudentClasseSection> findByStudentClasseId(UUID studentClasseId);
}
