package com.example.quanlydaotao.n2.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanlydaotao.n2.entity.StudentStatusHistory;

@Repository
public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, UUID> {

    List<StudentStatusHistory> findByStudentId(UUID studentId);

    List<StudentStatusHistory> findByStatusId(UUID statusId);
}
