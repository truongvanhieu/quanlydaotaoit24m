package com.example.quanlydaotao.n8.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanlydaotao.n8.entity.GradeComponent;

public interface GradeComponentRepository extends JpaRepository<GradeComponent, UUID> {
}