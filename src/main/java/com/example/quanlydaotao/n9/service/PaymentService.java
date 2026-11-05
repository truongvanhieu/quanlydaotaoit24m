package com.example.quanlydaotao.n9.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n9.dto.PaymentView;
import com.example.quanlydaotao.n9.entity.Payment;
import com.example.quanlydaotao.n9.exception.BusinessException;
import com.example.quanlydaotao.n9.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final List<String> VALID_STATUS = Arrays.asList(
            Payment.STATUS_PENDING,
            Payment.STATUS_SUCCESS,
            Payment.STATUS_FAILED,
            Payment.STATUS_REFUNDED,
            Payment.STATUS_CANCELLED);

    /** Câu truy vấn nền: lấy giao dịch kèm thông tin sinh viên và học kỳ của khoản thu. */
    private static final String PAYMENT_VIEW_SELECT = """
            SELECT p.id, p.tuition_id, st.student_id, sv.code AS student_code, sv.full_name AS student_name,
                   st.semester_id, sem.name AS semester_name,
                   tp.name AS program_name, tf.course_year,
                   p.amount_paid, p.payment_date, p.payment_method, p.payment_status,
                   p.transaction_ref, p.cashier_id, p.notes, p.is_active
              FROM payments p
              INNER JOIN student_tuition st ON st.id = p.tuition_id
              LEFT JOIN students_ sv ON sv.id = st.student_id
              LEFT JOIN semesters sem ON sem.id = st.semester_id
              LEFT JOIN tuition_fees tf ON tf.id = st.tuition_fee_id
              LEFT JOIN training_programs tp ON tp.id = tf.program_id
            """;

    private static final RowMapper<PaymentView> PAYMENT_VIEW_MAPPER = (rs, rowNum) -> {
        Integer method = getInteger(rs, "payment_method");

        return new PaymentView(
                UUID.fromString(rs.getString("id")),
                toUuid(rs.getString("tuition_id")),
                toUuid(rs.getString("student_id")),
                rs.getString("student_code"),
                rs.getString("student_name"),
                toUuid(rs.getString("semester_id")),
                rs.getString("semester_name"),
                rs.getString("program_name"),
                rs.getString("course_year"),
                rs.getBigDecimal("amount_paid"),
                rs.getTimestamp("payment_date") == null ? null : rs.getTimestamp("payment_date").toLocalDateTime(),
                method,
                paymentMethodName(method),
                rs.getString("payment_status"),
                rs.getString("transaction_ref"),
                toUuid(rs.getString("cashier_id")),
                rs.getString("notes"),
                rs.getObject("is_active") == null ? null : rs.getBoolean("is_active"));
    };

    private final PaymentRepository paymentRepository;
    private final StudentTuitionService studentTuitionService;
    private final JdbcTemplate jdbcTemplate;

    public PaymentService(
            PaymentRepository paymentRepository,
            StudentTuitionService studentTuitionService,
            JdbcTemplate jdbcTemplate) {

        this.paymentRepository = paymentRepository;
        this.studentTuitionService = studentTuitionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Danh sách giao dịch kèm tên sinh viên, học kỳ để hiển thị lên bảng. */
    public List<PaymentView> searchView(UUID tuitionId, String status, Integer paymentMethod, Boolean isActive) {
        String normalizedStatus = normalizeStatus(status, false);

        StringBuilder sql = new StringBuilder(PAYMENT_VIEW_SELECT).append(" WHERE p.deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (tuitionId != null) {
            sql.append(" AND p.tuition_id = ?");
            params.add(tuitionId.toString());
        }

        if (normalizedStatus != null) {
            sql.append(" AND p.payment_status = ?");
            params.add(normalizedStatus);
        }

        if (paymentMethod != null) {
            sql.append(" AND p.payment_method = ?");
            params.add(paymentMethod);
        }

        if (isActive != null) {
            sql.append(" AND p.is_active = ?");
            params.add(isActive);
        }

        sql.append(" ORDER BY p.payment_date DESC");

        return jdbcTemplate.query(sql.toString(), PAYMENT_VIEW_MAPPER, params.toArray());
    }

    /** Chi tiết một giao dịch thanh toán kèm thông tin sinh viên, học kỳ. */
    public PaymentView getViewById(UUID id) {
        List<PaymentView> result = jdbcTemplate.query(
                PAYMENT_VIEW_SELECT + " WHERE p.id = ? AND p.deleted_at IS NULL",
                PAYMENT_VIEW_MAPPER,
                id.toString());

        if (result.isEmpty()) {
            throw new BusinessException("Không tìm thấy giao dịch thanh toán với id: " + id);
        }

        return result.get(0);
    }

    private static UUID toUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static String paymentMethodName(Integer method) {
        if (method == null) {
            return null;
        }

        return switch (method) {
            case 1 -> "Chuyển khoản";
            case 2 -> "Tiền mặt";
            case 3 -> "Ví điện tử";
            default -> "Khác";
        };
    }

    public List<Payment> search(UUID tuitionId, String status, Integer paymentMethod, Boolean isActive) {
        String normalizedStatus = normalizeStatus(status, false);

        return paymentRepository.findByDeletedAtIsNullOrderByPaymentDateDesc().stream()
                .filter(item -> tuitionId == null || tuitionId.equals(item.getTuitionId()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.getPaymentStatus()))
                .filter(item -> paymentMethod == null || paymentMethod.equals(item.getPaymentMethod()))
                .filter(item -> isActive == null || isActive.equals(item.getIsActive()))
                .toList();
    }

    public Payment getById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch thanh toán với id: " + id));

        if (payment.getDeletedAt() != null) {
            throw new BusinessException("Giao dịch thanh toán đã bị hủy với id: " + id);
        }

        return payment;
    }

    @Transactional
    public Payment create(Payment request) {
        validate(request);

        Payment payment = new Payment();
        apply(payment, request);
        payment.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());

        Payment saved = paymentRepository.save(payment);

        studentTuitionService.syncPayments(saved.getTuitionId());

        return saved;
    }

    @Transactional
    public Payment update(UUID id, Payment request) {
        validate(request);

        Payment payment = getById(id);
        UUID oldTuitionId = payment.getTuitionId();

        apply(payment, request);

        Payment saved = paymentRepository.save(payment);

        studentTuitionService.syncPayments(oldTuitionId);

        if (!oldTuitionId.equals(saved.getTuitionId())) {
            studentTuitionService.syncPayments(saved.getTuitionId());
        }

        return saved;
    }

    /** Hủy giao dịch (xóa mềm) và cập nhật lại công nợ của học phí sinh viên. */
    @Transactional
    public void delete(UUID id) {
        Payment payment = getById(id);

        payment.setIsActive(false);
        payment.setPaymentStatus(Payment.STATUS_CANCELLED);
        payment.setDeletedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        studentTuitionService.syncPayments(payment.getTuitionId());
    }

    private void apply(Payment payment, Payment request) {
        payment.setTuitionId(request.getTuitionId());
        payment.setAmountPaid(request.getAmountPaid());
        payment.setPaymentDate(request.getPaymentDate() == null ? LocalDateTime.now() : request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(normalizeStatus(request.getPaymentStatus(), true));
        payment.setTransactionRef(request.getTransactionRef());
        payment.setCashierId(request.getCashierId());
        payment.setNotes(request.getNotes());

        if (request.getIsActive() != null) {
            payment.setIsActive(request.getIsActive());
        }
    }

    private void validate(Payment request) {
        if (request.getTuitionId() == null) {
            throw new BusinessException("Vui lòng chọn học phí sinh viên cần thanh toán");
        }

        // Chặn lỗi khóa ngoại: học phí sinh viên phải tồn tại
        studentTuitionService.getById(request.getTuitionId());

        if (request.getAmountPaid() == null || request.getAmountPaid().signum() <= 0) {
            throw new BusinessException("Số tiền nộp phải lớn hơn 0");
        }

        if (request.getPaymentMethod() != null && (request.getPaymentMethod() < 1 || request.getPaymentMethod() > 3)) {
            throw new BusinessException("Hình thức thanh toán không hợp lệ (1-Chuyển khoản, 2-Tiền mặt, 3-Ví điện tử)");
        }

        String status = normalizeStatus(request.getPaymentStatus(), false);

        if (status != null && !VALID_STATUS.contains(status)) {
            throw new BusinessException("Trạng thái giao dịch không hợp lệ");
        }
    }

    private String normalizeStatus(String status, boolean useDefault) {
        if (status == null || status.trim().isEmpty()) {
            return useDefault ? Payment.STATUS_SUCCESS : null;
        }

        return status.trim().toUpperCase();
    }
}
