package com.example.quanlydaotao.n6.repository;
import com.example.quanlydaotao.n6.entity.RegistrationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, UUID> {
    List<RegistrationPeriod> findByDeletedAtIsNull();
}
