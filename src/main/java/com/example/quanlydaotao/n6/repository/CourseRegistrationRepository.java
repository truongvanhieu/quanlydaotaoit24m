package com.example.quanlydaotao.n6.repository;
import com.example.quanlydaotao.n6.entity.CourseRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, UUID> {
    List<CourseRegistration> findByDeletedAtIsNull();
}
