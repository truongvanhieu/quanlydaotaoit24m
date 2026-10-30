package com.example.quanlydaotao.n1.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanlydaotao.n1.entity.RevokedToken;

@Repository
public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, UUID> {

    boolean existsByTokenId(String tokenId);
}
