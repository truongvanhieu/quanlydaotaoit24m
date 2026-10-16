package com.example.quanlydaotao.n2.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanlydaotao.n2.entity.AdvisorClasseSection;

@Repository
public interface AdvisorClasseSectionRepository extends JpaRepository<AdvisorClasseSection, UUID> {

    List<AdvisorClasseSection> findByEmployeeId(UUID employeeId);

    List<AdvisorClasseSection> findByStudentClasseId(UUID studentClasseId);
}
