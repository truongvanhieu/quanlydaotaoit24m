package com.example.quanlydaotao.n5.repository;

import com.example.quanlydaotao.n5.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, UUID> {

    Optional<Instructor> findByCodeIgnoreCase(String code);
}
