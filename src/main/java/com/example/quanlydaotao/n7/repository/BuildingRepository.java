package com.example.quanlydaotao.n7.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quanlydaotao.n7.entity.Building;

public interface BuildingRepository extends JpaRepository<Building, UUID> {

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    boolean existsByCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(String code, UUID id);

    @Query("""
            select b from Building b
            where b.deletedAt is null
              and (:active is null or b.active = :active)
              and (:keyword is null
                   or lower(coalesce(b.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(b.name, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(b.address, '')) like lower(concat('%', :keyword, '%')))
            """)
    Page<Building> search(@Param("keyword") String keyword, @Param("active") Boolean active, Pageable pageable);
}
