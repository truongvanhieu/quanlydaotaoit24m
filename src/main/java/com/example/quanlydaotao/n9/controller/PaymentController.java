package com.example.quanlydaotao.n9.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n9.dto.PaymentView;
import com.example.quanlydaotao.n9.entity.Payment;
import com.example.quanlydaotao.n9.service.PaymentService;

/**
 * Quản lý lịch sử giao dịch thanh toán học phí (payments).
 * Các API GET trả về {@link PaymentView} để có sẵn tên sinh viên và học kỳ.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public List<PaymentView> listPayments(
            @RequestParam(required = false) UUID tuitionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer method,
            @RequestParam(required = false) Boolean isActive) {

        return paymentService.searchView(tuitionId, status, method, isActive);
    }

    @GetMapping("/{id}")
    public PaymentView getPayment(@PathVariable UUID id) {

        return paymentService.getViewById(id);
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {

        return paymentService.create(payment);
    }

    @PutMapping("/{id}")
    public Payment updatePayment(
            @PathVariable UUID id,
            @RequestBody Payment payment) {

        return paymentService.update(id, payment);
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable UUID id) {

        paymentService.delete(id);
    }
}
