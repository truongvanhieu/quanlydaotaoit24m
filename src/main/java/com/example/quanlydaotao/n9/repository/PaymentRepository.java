package com.example.quanlydaotao.n9.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quanlydaotao.n9.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByDeletedAtIsNullOrderByPaymentDateDesc();

    /**
     * Tổng số tiền đã thu thành công của một bản ghi học phí sinh viên.
     * Chỉ tính các giao dịch SUCCESS, còn hiệu lực và chưa bị xóa.
     */
    @Query("select sum(p.amountPaid) from Payment p "
            + "where p.tuitionId = :tuitionId "
            + "and p.paymentStatus = 'SUCCESS' "
            + "and p.isActive = true "
            + "and p.deletedAt is null")
    BigDecimal sumSuccessfulAmount(@Param("tuitionId") UUID tuitionId);
}
