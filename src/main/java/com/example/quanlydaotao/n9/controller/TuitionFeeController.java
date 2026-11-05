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

import com.example.quanlydaotao.n9.dto.TuitionFeeView;
import com.example.quanlydaotao.n9.entity.TuitionFee;
import com.example.quanlydaotao.n9.service.TuitionFeeService;

/**
 * Quản lý định mức học phí (tuition_fees).
 * Các API GET trả về {@link TuitionFeeView} để có sẵn tên chương trình đào tạo.
 */
@RestController
@RequestMapping("/api/tuition-fees")
public class TuitionFeeController {

    @Autowired
    private TuitionFeeService tuitionFeeService;

    @GetMapping
    public List<TuitionFeeView> listTuitionFees(
            @RequestParam(required = false) String keyword) {

        return tuitionFeeService.searchView(keyword);
    }

    @GetMapping("/{id}")
    public TuitionFeeView getTuitionFee(@PathVariable UUID id) {

        return tuitionFeeService.getViewById(id);
    }

    @PostMapping
    public TuitionFee createTuitionFee(@RequestBody TuitionFee tuitionFee) {

        return tuitionFeeService.create(tuitionFee);
    }

    @PutMapping("/{id}")
    public TuitionFee updateTuitionFee(
            @PathVariable UUID id,
            @RequestBody TuitionFee tuitionFee) {

        return tuitionFeeService.update(id, tuitionFee);
    }

    @DeleteMapping("/{id}")
    public void deleteTuitionFee(@PathVariable UUID id) {

        tuitionFeeService.delete(id);
    }
}
