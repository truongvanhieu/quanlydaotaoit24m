package com.example.quanlydaotao.n3.repository;

import com.example.quanlydaotao.n3.entity.N3User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface N3UserRepository extends JpaRepository<N3User, UUID> {

    List<N3User> findByDeletedAtIsNullOrderByUsernameAsc();
}
