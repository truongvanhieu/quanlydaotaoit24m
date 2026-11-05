package com.example.quanlydaotao.n9.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n9.dto.TuitionFeeView;
import com.example.quanlydaotao.n9.entity.TuitionFee;
import com.example.quanlydaotao.n9.exception.BusinessException;
import com.example.quanlydaotao.n9.repository.StudentTuitionRepository;
import com.example.quanlydaotao.n9.repository.TuitionFeeRepository;

@Service
public class TuitionFeeService {

    /** Câu truy vấn nền: lấy định mức kèm tên chương trình đào tạo. */
    private static final String FEE_VIEW_SELECT = """
            SELECT tf.id, tf.program_id, tp.code AS program_code, tp.name AS program_name,
                   tf.course_year, tf.price_per_credit, tf.base_tuition, tf.effective_date, tf.is_active
              FROM tuition_fees tf
              LEFT JOIN training_programs tp ON tp.id = tf.program_id
            """;

    private static final RowMapper<TuitionFeeView> FEE_VIEW_MAPPER = (rs, rowNum) -> new TuitionFeeView(
            UUID.fromString(rs.getString("id")),
            toUuid(rs.getString("program_id")),
            rs.getString("program_code"),
            rs.getString("program_name"),
            rs.getString("course_year"),
            rs.getBigDecimal("price_per_credit"),
            rs.getBigDecimal("base_tuition"),
            rs.getDate("effective_date") == null ? null : rs.getDate("effective_date").toLocalDate(),
            rs.getObject("is_active") == null ? null : rs.getBoolean("is_active"));

    private final TuitionFeeRepository tuitionFeeRepository;
    private final StudentTuitionRepository studentTuitionRepository;
    private final JdbcTemplate jdbcTemplate;

    public TuitionFeeService(
            TuitionFeeRepository tuitionFeeRepository,
            StudentTuitionRepository studentTuitionRepository,
            JdbcTemplate jdbcTemplate) {

        this.tuitionFeeRepository = tuitionFeeRepository;
        this.studentTuitionRepository = studentTuitionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Danh sách định mức kèm tên chương trình đào tạo để hiển thị lên bảng. */
    public List<TuitionFeeView> searchView(String keyword) {
        StringBuilder sql = new StringBuilder(FEE_VIEW_SELECT).append(" WHERE tf.deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (tf.course_year LIKE ? OR tp.name LIKE ?)");
            params.add(like);
            params.add(like);
        }

        sql.append(" ORDER BY tf.effective_date DESC");

        return jdbcTemplate.query(sql.toString(), FEE_VIEW_MAPPER, params.toArray());
    }

    /** Chi tiết một định mức kèm tên chương trình đào tạo. */
    public TuitionFeeView getViewById(UUID id) {
        List<TuitionFeeView> result = jdbcTemplate.query(
                FEE_VIEW_SELECT + " WHERE tf.id = ? AND tf.deleted_at IS NULL",
                FEE_VIEW_MAPPER,
                id.toString());

        if (result.isEmpty()) {
            throw new BusinessException("Không tìm thấy định mức học phí với id: " + id);
        }

        return result.get(0);
    }

    private static UUID toUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    public List<TuitionFee> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return tuitionFeeRepository.findByDeletedAtIsNullOrderByEffectiveDateDesc();
        }

        return tuitionFeeRepository
                .findByDeletedAtIsNullAndCourseYearContainingIgnoreCaseOrderByEffectiveDateDesc(keyword.trim());
    }

    public TuitionFee getById(UUID id) {
        TuitionFee tuitionFee = tuitionFeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy định mức học phí với id: " + id));

        if (tuitionFee.getDeletedAt() != null) {
            throw new BusinessException("Định mức học phí đã bị xóa với id: " + id);
        }

        return tuitionFee;
    }

    @Transactional
    public TuitionFee create(TuitionFee request) {
        validate(request);

        TuitionFee tuitionFee = new TuitionFee();
        apply(tuitionFee, request);
        tuitionFee.setIsActive(true);

        return tuitionFeeRepository.save(tuitionFee);
    }

    @Transactional
    public TuitionFee update(UUID id, TuitionFee request) {
        validate(request);

        TuitionFee tuitionFee = getById(id);
        apply(tuitionFee, request);

        return tuitionFeeRepository.save(tuitionFee);
    }

    @Transactional
    public void delete(UUID id) {
        TuitionFee tuitionFee = getById(id);

        if (!studentTuitionRepository.findByTuitionFeeIdAndDeletedAtIsNull(id).isEmpty()) {
            throw new BusinessException("Không thể xóa: định mức học phí đang được sử dụng cho học phí sinh viên");
        }

        tuitionFee.setIsActive(false);
        tuitionFee.setDeletedAt(LocalDateTime.now());

        tuitionFeeRepository.save(tuitionFee);
    }

    private void apply(TuitionFee tuitionFee, TuitionFee request) {
        tuitionFee.setProgramId(request.getProgramId());
        tuitionFee.setCourseYear(request.getCourseYear());
        tuitionFee.setPricePerCredit(request.getPricePerCredit());
        tuitionFee.setBaseTuition(request.getBaseTuition());
        tuitionFee.setEffectiveDate(request.getEffectiveDate());

        if (request.getIsActive() != null) {
            tuitionFee.setIsActive(request.getIsActive());
        }
    }

    private void validate(TuitionFee request) {
        if (request.getProgramId() == null) {
            throw new BusinessException("Vui lòng nhập chương trình đào tạo");
        }

        if (request.getPricePerCredit() == null) {
            throw new BusinessException("Vui lòng nhập đơn giá tín chỉ");
        }

        if (request.getPricePerCredit().signum() < 0) {
            throw new BusinessException("Đơn giá tín chỉ không được âm");
        }

        if (request.getEffectiveDate() == null) {
            throw new BusinessException("Vui lòng nhập ngày bắt đầu áp dụng");
        }
    }
}
