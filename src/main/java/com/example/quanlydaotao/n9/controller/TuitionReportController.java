package com.example.quanlydaotao.n9.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n9.dto.PaymentReceipt;
import com.example.quanlydaotao.n9.dto.SemesterDebtReport;
import com.example.quanlydaotao.n9.dto.TuitionSummary;
import com.example.quanlydaotao.n9.service.TuitionReportService;

/**
 * API báo cáo - thống kê học phí (nhóm N9).
 */
@RestController
@RequestMapping("/api/tuition-reports")
public class TuitionReportController {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final TuitionReportService tuitionReportService;

    public TuitionReportController(TuitionReportService tuitionReportService) {
        this.tuitionReportService = tuitionReportService;
    }

    /** Thống kê tổng quan: tổng phải thu, đã thu, còn nợ, số sinh viên còn nợ. */
    @GetMapping("/summary")
    public TuitionSummary getSummary() {
        return tuitionReportService.getSummary();
    }

    /** Báo cáo công nợ tổng hợp theo từng học kỳ. */
    @GetMapping("/by-semester")
    public List<SemesterDebtReport> getReportBySemester() {
        return tuitionReportService.getReportBySemester();
    }

    /** Dữ liệu in biên lai thu tiền của một giao dịch. */
    @GetMapping("/receipt/{paymentId}")
    public PaymentReceipt getReceipt(@PathVariable UUID paymentId) {
        return tuitionReportService.getReceipt(paymentId);
    }

    /**
     * Xuất danh sách ra file Excel.
     * type = tuition-fees | student-tuitions | payments
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(@RequestParam String type) {
        byte[] data = tuitionReportService.exportExcel(type);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + type + ".xlsx\"")
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .body(data);
    }
}
