package com.example.quanlydaotao.n6.repository;
import com.example.quanlydaotao.n6.entity.CourseEquivalence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface CourseEquivalenceRepository extends JpaRepository<CourseEquivalence, UUID> {
    List<CourseEquivalence> findByDeletedAtIsNull();
}
