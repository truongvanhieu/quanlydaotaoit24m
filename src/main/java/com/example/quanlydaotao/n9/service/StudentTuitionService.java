package com.example.quanlydaotao.n9.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n9.dto.StudentTuitionView;
import com.example.quanlydaotao.n9.entity.StudentTuition;
import com.example.quanlydaotao.n9.entity.TuitionFee;
import com.example.quanlydaotao.n9.exception.BusinessException;
import com.example.quanlydaotao.n9.repository.PaymentRepository;
import com.example.quanlydaotao.n9.repository.StudentTuitionRepository;
import com.example.quanlydaotao.n9.repository.TuitionFeeRepository;

@Service
public class StudentTuitionService {

    /** Câu truy vấn nền: lấy học phí kèm tên sinh viên, học kỳ và chương trình đào tạo. */
    private static final String TUITION_VIEW_SELECT = """
            SELECT st.id, st.student_id, sv.code AS student_code, sv.full_name AS student_name,
                   st.semester_id, sem.name AS semester_name, st.tuition_fee_id,
                   tp.name AS program_name, tf.course_year,
                   tf.price_per_credit, tf.base_tuition,
                   st.total_credits, st.raw_amount, st.scholarship_deduction, st.exemption_amount,
                   st.net_amount, st.paid_amount, st.debt_amount, st.status, st.deadline, st.is_active
              FROM student_tuition st
              LEFT JOIN students_ sv ON sv.id = st.student_id
              LEFT JOIN semesters sem ON sem.id = st.semester_id
              LEFT JOIN tuition_fees tf ON tf.id = st.tuition_fee_id
              LEFT JOIN training_programs tp ON tp.id = tf.program_id
            """;

    private static final RowMapper<StudentTuitionView> TUITION_VIEW_MAPPER = (rs, rowNum) -> new StudentTuitionView(
            UUID.fromString(rs.getString("id")),
            toUuid(rs.getString("student_id")),
            rs.getString("student_code"),
            rs.getString("student_name"),
            toUuid(rs.getString("semester_id")),
            rs.getString("semester_name"),
            toUuid(rs.getString("tuition_fee_id")),
            rs.getString("program_name"),
            rs.getString("course_year"),
            rs.getBigDecimal("price_per_credit"),
            rs.getBigDecimal("base_tuition"),
            getInteger(rs, "total_credits"),
            rs.getBigDecimal("raw_amount"),
            rs.getBigDecimal("scholarship_deduction"),
            rs.getBigDecimal("exemption_amount"),
            rs.getBigDecimal("net_amount"),
            rs.getBigDecimal("paid_amount"),
            rs.getBigDecimal("debt_amount"),
            getInteger(rs, "status"),
            StudentTuition.statusName(getInteger(rs, "status")),
            rs.getDate("deadline") == null ? null : rs.getDate("deadline").toLocalDate(),
            rs.getObject("is_active") == null ? null : rs.getBoolean("is_active"));

    private final StudentTuitionRepository studentTuitionRepository;
    private final TuitionFeeRepository tuitionFeeRepository;
    private final PaymentRepository paymentRepository;
    private final JdbcTemplate jdbcTemplate;

    public StudentTuitionService(
            StudentTuitionRepository studentTuitionRepository,
            TuitionFeeRepository tuitionFeeRepository,
            PaymentRepository paymentRepository,
            JdbcTemplate jdbcTemplate) {

        this.studentTuitionRepository = studentTuitionRepository;
        this.tuitionFeeRepository = tuitionFeeRepository;
        this.paymentRepository = paymentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Danh sách học phí kèm tên sinh viên, học kỳ, chương trình để hiển thị lên bảng. */
    public List<StudentTuitionView> searchView(UUID studentId, UUID semesterId, Integer status) {
        StringBuilder sql = new StringBuilder(TUITION_VIEW_SELECT).append(" WHERE st.deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (studentId != null) {
            sql.append(" AND st.student_id = ?");
            params.add(studentId.toString());
        }

        if (semesterId != null) {
            sql.append(" AND st.semester_id = ?");
            params.add(semesterId.toString());
        }

        if (status != null) {
            sql.append(" AND st.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY st.created_at DESC");

        return jdbcTemplate.query(sql.toString(), TUITION_VIEW_MAPPER, params.toArray());
    }

    /** Chi tiết một học phí sinh viên kèm tên sinh viên, học kỳ, chương trình. */
    public StudentTuitionView getViewById(UUID id) {
        List<StudentTuitionView> result = jdbcTemplate.query(
                TUITION_VIEW_SELECT + " WHERE st.id = ? AND st.deleted_at IS NULL",
                TUITION_VIEW_MAPPER,
                id.toString());

        if (result.isEmpty()) {
            throw new BusinessException("Không tìm thấy học phí sinh viên với id: " + id);
        }

        return result.get(0);
    }

    private static UUID toUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private static Integer getInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    public List<StudentTuition> search(UUID studentId, UUID semesterId, Integer status) {
        return studentTuitionRepository.findByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .filter(item -> studentId == null || studentId.equals(item.getStudentId()))
                .filter(item -> semesterId == null || semesterId.equals(item.getSemesterId()))
                .filter(item -> status == null || status.equals(item.getStatus()))
                .toList();
    }

    public StudentTuition getById(UUID id) {
        StudentTuition studentTuition = studentTuitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy học phí sinh viên với id: " + id));

        if (studentTuition.getDeletedAt() != null) {
            throw new BusinessException("Học phí sinh viên đã bị xóa với id: " + id);
        }

        return studentTuition;
    }

    @Transactional
    public StudentTuition create(StudentTuition request) {
        validate(request);

        StudentTuition studentTuition = new StudentTuition();
        apply(studentTuition, request);
        studentTuition.setIsActive(true);

        calculateAmounts(studentTuition);
        refreshPaymentState(studentTuition);

        return studentTuitionRepository.save(studentTuition);
    }

    @Transactional
    public StudentTuition update(UUID id, StudentTuition request) {
        validate(request);

        StudentTuition studentTuition = getById(id);
        apply(studentTuition, request);

        calculateAmounts(studentTuition);
        refreshPaymentState(studentTuition);

        return studentTuitionRepository.save(studentTuition);
    }

    @Transactional
    public void delete(UUID id) {
        StudentTuition studentTuition = getById(id);

        studentTuition.setIsActive(false);
        studentTuition.setDeletedAt(LocalDateTime.now());

        studentTuitionRepository.save(studentTuition);
    }

    /**
     * Tính lại số tiền đã nộp, số tiền còn nợ và trạng thái của học phí
     * dựa trên lịch sử giao dịch thanh toán.
     */
    @Transactional
    public void syncPayments(UUID tuitionId) {
        studentTuitionRepository.findById(tuitionId).ifPresent(studentTuition -> {
            if (studentTuition.getDeletedAt() == null) {
                refreshPaymentState(studentTuition);
                studentTuitionRepository.save(studentTuition);
            }
        });
    }

    /**
     * Tiền gốc = đơn giá tín chỉ x số tín chỉ + học phí cố định mỗi kỳ.
     * Tiền thực nộp = tiền gốc - miễn giảm chính sách - học bổng.
     */
    private void calculateAmounts(StudentTuition studentTuition) {
        TuitionFee tuitionFee = tuitionFeeRepository.findById(studentTuition.getTuitionFeeId())
                .orElseThrow(() -> new BusinessException(
                        "Không tìm thấy định mức học phí với id: " + studentTuition.getTuitionFeeId()));

        int totalCredits = studentTuition.getTotalCredits() == null ? 0 : studentTuition.getTotalCredits();

        BigDecimal rawAmount = zeroIfNull(tuitionFee.getPricePerCredit())
                .multiply(BigDecimal.valueOf(totalCredits))
                .add(zeroIfNull(tuitionFee.getBaseTuition()));

        BigDecimal netAmount = rawAmount
                .subtract(zeroIfNull(studentTuition.getScholarshipDeduction()))
                .subtract(zeroIfNull(studentTuition.getExemptionAmount()));

        if (netAmount.signum() < 0) {
            netAmount = BigDecimal.ZERO;
        }

        studentTuition.setRawAmount(rawAmount);
        studentTuition.setNetAmount(netAmount);
    }

    /**
     * Cập nhật số tiền đã nộp, công nợ và trạng thái của học phí sinh viên.
     */
    private void refreshPaymentState(StudentTuition studentTuition) {
        BigDecimal paidAmount = studentTuition.getId() == null
                ? BigDecimal.ZERO
                : zeroIfNull(paymentRepository.sumSuccessfulAmount(studentTuition.getId()));

        BigDecimal netAmount = zeroIfNull(studentTuition.getNetAmount());

        studentTuition.setPaidAmount(paidAmount);
        studentTuition.setDebtAmount(netAmount.subtract(paidAmount));
        studentTuition.setStatus(computeStatus(netAmount, paidAmount, studentTuition.getDeadline()));
    }

    /**
     * 1-PAID: đã nộp đủ. 2-PARTIAL: nộp một phần.
     * 3-DEBT: chưa nộp. 4-OVERDUE: quá hạn mà chưa nộp đủ.
     */
    private int computeStatus(BigDecimal netAmount, BigDecimal paidAmount, LocalDate deadline) {
        if (paidAmount.compareTo(netAmount) >= 0) {
            return StudentTuition.STATUS_PAID;
        }

        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            return StudentTuition.STATUS_OVERDUE;
        }

        if (paidAmount.signum() > 0) {
            return StudentTuition.STATUS_PARTIAL;
        }

        return StudentTuition.STATUS_DEBT;
    }

    private void apply(StudentTuition studentTuition, StudentTuition request) {
        studentTuition.setStudentId(request.getStudentId());
        studentTuition.setSemesterId(request.getSemesterId());
        studentTuition.setTuitionFeeId(request.getTuitionFeeId());
        studentTuition.setTotalCredits(request.getTotalCredits());
        studentTuition.setScholarshipDeduction(request.getScholarshipDeduction());
        studentTuition.setExemptionAmount(request.getExemptionAmount());
        studentTuition.setDeadline(request.getDeadline());

        if (request.getIsActive() != null) {
            studentTuition.setIsActive(request.getIsActive());
        }
    }

    private void validate(StudentTuition request) {
        if (request.getStudentId() == null) {
            throw new BusinessException("Vui lòng nhập sinh viên");
        }

        if (request.getSemesterId() == null) {
            throw new BusinessException("Vui lòng nhập học kỳ");
        }

        if (request.getTuitionFeeId() == null) {
            throw new BusinessException("Vui lòng chọn định mức học phí");
        }

        if (request.getTotalCredits() == null || request.getTotalCredits() <= 0) {
            throw new BusinessException("Tổng số tín chỉ phải lớn hơn 0");
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
