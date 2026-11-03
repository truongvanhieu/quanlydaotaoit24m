package com.example.quanlydaotao.n7.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quanlydaotao.n7.entity.Course;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    @Query("""
            select c from Course c where c.deletedAt is null
              and (:active is null or c.active = :active)
              and (:keyword is null or lower(coalesce(c.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(c.nameEn, '')) like lower(concat('%', :keyword, '%')))
            order by c.code
            """)
    List<Course> search(@Param("keyword") String keyword, @Param("active") Boolean active);

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);
    boolean existsByCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(String code, UUID id);
}
