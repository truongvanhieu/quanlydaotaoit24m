package com.example.quanlydaotao.n9.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n9.dto.PaymentReceipt;
import com.example.quanlydaotao.n9.dto.SemesterDebtReport;
import com.example.quanlydaotao.n9.dto.TuitionSummary;
import com.example.quanlydaotao.n9.entity.StudentTuition;
import com.example.quanlydaotao.n9.exception.BusinessException;

/**
 * Báo cáo - thống kê học phí: tổng quan, công nợ theo học kỳ, biên lai, xuất Excel.
 * Dùng JdbcTemplate để đọc dữ liệu kết hợp với các bảng dùng chung (sinh viên, học kỳ...).
 */
@Service
public class TuitionReportService {

    private final JdbcTemplate jdbcTemplate;

    public TuitionReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================
    // THỐNG KÊ TỔNG QUAN
    // =========================

    public TuitionSummary getSummary() {
        String sql = """
                SELECT COUNT(*) AS total_records,
                       ISNULL(SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END), 0)       AS paid_count,
                       ISNULL(SUM(CASE WHEN status IN (3, 4) THEN 1 ELSE 0 END), 0) AS debtor_count,
                       ISNULL(SUM(net_amount), 0)  AS total_net,
                       ISNULL(SUM(paid_amount), 0) AS total_paid,
                       ISNULL(SUM(debt_amount), 0) AS total_debt
                FROM student_tuition
                WHERE deleted_at IS NULL AND is_active = 1
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            BigDecimal totalNet = rs.getBigDecimal("total_net");
            BigDecimal totalPaid = rs.getBigDecimal("total_paid");
            return new TuitionSummary(
                    rs.getLong("total_records"),
                    rs.getLong("paid_count"),
                    rs.getLong("debtor_count"),
                    totalNet,
                    totalPaid,
                    rs.getBigDecimal("total_debt"),
                    collectionRate(totalPaid, totalNet));
        });
    }

    // =========================
    // CÔNG NỢ THEO HỌC KỲ
    // =========================

    public List<SemesterDebtReport> getReportBySemester() {
        String sql = """
                SELECT s.id AS semester_id,
                       s.name AS semester_name,
                       COUNT(st.id) AS student_count,
                       ISNULL(SUM(CASE WHEN st.status IN (3, 4) THEN 1 ELSE 0 END), 0) AS debtor_count,
                       ISNULL(SUM(st.net_amount), 0)  AS total_net,
                       ISNULL(SUM(st.paid_amount), 0) AS total_paid,
                       ISNULL(SUM(st.debt_amount), 0) AS total_debt
                FROM semesters s
                LEFT JOIN student_tuition st
                       ON st.semester_id = s.id
                      AND st.deleted_at IS NULL
                      AND st.is_active = 1
                WHERE s.deleted_at IS NULL
                GROUP BY s.id, s.name
                ORDER BY s.name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SemesterDebtReport(
                UUID.fromString(rs.getString("semester_id")),
                rs.getString("semester_name"),
                rs.getLong("student_count"),
                rs.getLong("debtor_count"),
                rs.getBigDecimal("total_net"),
                rs.getBigDecimal("total_paid"),
                rs.getBigDecimal("total_debt")));
    }

    // =========================
    // BIÊN LAI THU TIỀN
    // =========================

    public PaymentReceipt getReceipt(UUID paymentId) {
        String sql = """
                SELECT p.id AS payment_id,
                       p.payment_date,
                       p.amount_paid,
                       p.payment_method,
                       p.payment_status,
                       p.transaction_ref,
                       p.notes,
                       sv.code      AS student_code,
                       sv.full_name AS student_name,
                       sem.name     AS semester_name,
                       tp.name      AS program_name,
                       tf.course_year,
                       st.net_amount,
                       st.paid_amount,
                       st.debt_amount,
                       st.status    AS tuition_status
                FROM payments p
                JOIN student_tuition st ON st.id = p.tuition_id
                LEFT JOIN students_ sv  ON sv.id = st.student_id
                LEFT JOIN semesters sem ON sem.id = st.semester_id
                LEFT JOIN tuition_fees tf  ON tf.id = st.tuition_fee_id
                LEFT JOIN training_programs tp ON tp.id = tf.program_id
                WHERE p.id = ?
                """;

        List<PaymentReceipt> found = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integer method = getInteger(rs, "payment_method");
            Integer tuitionStatus = getInteger(rs, "tuition_status");
            return new PaymentReceipt(
                    UUID.fromString(rs.getString("payment_id")),
                    toLocalDateTime(rs.getTimestamp("payment_date")),
                    rs.getBigDecimal("amount_paid"),
                    method,
                    paymentMethodName(method),
                    rs.getString("payment_status"),
                    rs.getString("transaction_ref"),
                    rs.getString("notes"),
                    rs.getString("student_code"),
                    rs.getString("student_name"),
                    rs.getString("semester_name"),
                    rs.getString("program_name"),
                    rs.getString("course_year"),
                    rs.getBigDecimal("net_amount"),
                    rs.getBigDecimal("paid_amount"),
                    rs.getBigDecimal("debt_amount"),
                    tuitionStatus,
                    StudentTuition.statusName(tuitionStatus));
        }, paymentId);

        if (found.isEmpty()) {
            throw new BusinessException("Không tìm thấy giao dịch thanh toán");
        }
        return found.get(0);
    }

    // =========================
    // XUẤT EXCEL
    // =========================

    public byte[] exportExcel(String type) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            switch (type == null ? "" : type) {
                case "tuition-fees" -> writeTuitionFeesSheet(workbook);
                case "student-tuitions" -> writeStudentTuitionsSheet(workbook);
                case "payments" -> writePaymentsSheet(workbook);
                default -> throw new BusinessException("Loại dữ liệu xuất không hợp lệ: " + type);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException("Không tạo được file Excel: " + ex.getMessage());
        }
    }

    private void writeTuitionFeesSheet(XSSFWorkbook workbook) {
        String sql = """
                SELECT tp.name AS program_name, tf.course_year, tf.price_per_credit, tf.base_tuition,
                       tf.effective_date, tf.is_active
                FROM tuition_fees tf
                LEFT JOIN training_programs tp ON tp.id = tf.program_id
                WHERE tf.deleted_at IS NULL
                ORDER BY tp.name, tf.course_year
                """;

        String[] headers = { "STT", "Chương trình đào tạo", "Khóa học", "Đơn giá / tín chỉ",
                "Học phí cơ bản", "Ngày áp dụng", "Hiệu lực" };
        SheetContext ctx = newSheet(workbook, "Dinh muc hoc phi", "DANH SÁCH ĐỊNH MỨC HỌC PHÍ", headers,
                new int[] { 6, 42, 12, 18, 18, 14, 15 });

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] {
                null,
                rs.getString("program_name"),
                rs.getString("course_year"),
                rs.getBigDecimal("price_per_credit"),
                rs.getBigDecimal("base_tuition"),
                toLocalDate(rs.getDate("effective_date")),
                rs.getBoolean("is_active") ? "Đang áp dụng" : "Ngừng áp dụng"
        });

        writeRows(ctx, rows, new int[] { 3, 4 });
    }

    private void writeStudentTuitionsSheet(XSSFWorkbook workbook) {
        String sql = """
                SELECT sv.code AS student_code, sv.full_name AS student_name,
                       sem.name AS semester_name, tf.course_year, st.total_credits,
                       st.raw_amount, st.scholarship_deduction, st.exemption_amount, st.net_amount,
                       st.paid_amount, st.debt_amount, st.status, st.deadline
                FROM student_tuition st
                LEFT JOIN students_ sv  ON sv.id = st.student_id
                LEFT JOIN semesters sem ON sem.id = st.semester_id
                LEFT JOIN tuition_fees tf ON tf.id = st.tuition_fee_id
                WHERE st.deleted_at IS NULL
                ORDER BY sem.name DESC, sv.code
                """;

        String[] headers = { "STT", "Mã SV", "Họ tên", "Học kỳ", "Khóa", "Tín chỉ", "Tiền gốc",
                "Học bổng", "Miễn giảm", "Thực nộp", "Đã nộp", "Còn nợ", "Trạng thái", "Hạn chót" };
        SheetContext ctx = newSheet(workbook, "Hoc phi sinh vien", "DANH SÁCH HỌC PHÍ SINH VIÊN", headers,
                new int[] { 6, 14, 28, 30, 10, 9, 16, 14, 14, 16, 16, 16, 13, 13 });

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integer status = getInteger(rs, "status");
            return new Object[] {
                    null,
                    rs.getString("student_code"),
                    rs.getString("student_name"),
                    rs.getString("semester_name"),
                    rs.getString("course_year"),
                    getInteger(rs, "total_credits"),
                    rs.getBigDecimal("raw_amount"),
                    rs.getBigDecimal("scholarship_deduction"),
                    rs.getBigDecimal("exemption_amount"),
                    rs.getBigDecimal("net_amount"),
                    rs.getBigDecimal("paid_amount"),
                    rs.getBigDecimal("debt_amount"),
                    StudentTuition.statusName(status),
                    toLocalDate(rs.getDate("deadline"))
            };
        });

        writeRows(ctx, rows, new int[] { 6, 7, 8, 9, 10, 11 }, new int[] { 13 });
    }

    private void writePaymentsSheet(XSSFWorkbook workbook) {
        String sql = """
                SELECT p.payment_date, sv.code AS student_code, sv.full_name AS student_name,
                       sem.name AS semester_name, p.amount_paid, p.payment_method,
                       p.payment_status, p.transaction_ref, p.notes, p.is_active
                FROM payments p
                JOIN student_tuition st ON st.id = p.tuition_id
                LEFT JOIN students_ sv  ON sv.id = st.student_id
                LEFT JOIN semesters sem ON sem.id = st.semester_id
                WHERE p.deleted_at IS NULL
                ORDER BY p.payment_date DESC
                """;

        String[] headers = { "STT", "Ngày thu", "Mã SV", "Họ tên", "Học kỳ", "Số tiền nộp",
                "Phương thức", "Trạng thái", "Mã giao dịch", "Ghi chú", "Hiệu lực" };
        SheetContext ctx = newSheet(workbook, "Giao dich thanh toan", "LỊCH SỬ GIAO DỊCH THANH TOÁN", headers,
                new int[] { 6, 18, 14, 28, 30, 16, 15, 13, 16, 30, 11 });

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integer method = getInteger(rs, "payment_method");
            return new Object[] {
                    null,
                    toLocalDateTime(rs.getTimestamp("payment_date")),
                    rs.getString("student_code"),
                    rs.getString("student_name"),
                    rs.getString("semester_name"),
                    rs.getBigDecimal("amount_paid"),
                    paymentMethodName(method),
                    rs.getString("payment_status"),
                    rs.getString("transaction_ref"),
                    rs.getString("notes"),
                    rs.getBoolean("is_active") ? "Hợp lệ" : "Đã hủy"
            };
        });

        writeRows(ctx, rows, new int[] { 5 }, new int[] { 1 });
    }

    // =========================
    // TIỆN ÍCH XUẤT EXCEL
    // =========================

    private record SheetContext(Sheet sheet, CellStyle moneyStyle, CellStyle dateTimeStyle,
            CellStyle textStyle) {
    }

    private SheetContext newSheet(XSSFWorkbook workbook, String sheetName, String title,
            String[] headers, int[] widths) {

        Sheet sheet = workbook.createSheet(sheetName);

        CreationHelper creationHelper = workbook.getCreationHelper();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }

        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0"));

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy hh:mm"));

        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy"));

        return new SheetContext(sheet, moneyStyle, dateStyle, textStyle);
    }

    private void writeRows(SheetContext ctx, List<Object[]> rows, int[] moneyColumns) {
        writeRows(ctx, rows, moneyColumns, new int[0]);
    }

    private void writeRows(SheetContext ctx, List<Object[]> rows, int[] moneyColumns, int[] dateColumns) {
        int rowIndex = 2;
        for (Object[] values : rows) {
            Row row = ctx.sheet().createRow(rowIndex);
            for (int col = 0; col < values.length; col++) {
                Cell cell = row.createCell(col);
                Object value = values[col];

                if (col == 0) {
                    cell.setCellValue(rowIndex - 1);
                    continue;
                }
                if (value == null) {
                    cell.setCellValue("");
                    continue;
                }
                if (value instanceof BigDecimal number) {
                    cell.setCellValue(number.doubleValue());
                } else if (value instanceof Integer number) {
                    cell.setCellValue(number);
                } else if (value instanceof LocalDateTime dateTime) {
                    cell.setCellValue(dateTime);
                } else if (value instanceof LocalDate date) {
                    cell.setCellValue(date);
                } else {
                    cell.setCellValue(String.valueOf(value));
                }
            }
            for (int col : moneyColumns) {
                row.getCell(col).setCellStyle(ctx.moneyStyle());
            }
            for (int col : dateColumns) {
                row.getCell(col).setCellStyle(ctx.dateTimeStyle());
            }
            rowIndex++;
        }

        if (rows.isEmpty()) {
            ctx.sheet().createRow(2).createCell(0).setCellValue("Không có dữ liệu");
        }
    }

    // =========================
    // TIỆN ÍCH CHUNG
    // =========================

    private BigDecimal collectionRate(BigDecimal paid, BigDecimal net) {
        if (net == null || net.signum() == 0 || paid == null) {
            return BigDecimal.ZERO;
        }
        return paid.multiply(BigDecimal.valueOf(100)).divide(net, 1, RoundingMode.HALF_UP);
    }

    private String paymentMethodName(Integer method) {
        if (method == null) {
            return "";
        }
        return switch (method) {
            case 1 -> "Chuyển khoản";
            case 2 -> "Tiền mặt";
            case 3 -> "Ví điện tử";
            default -> "Khác";
        };
    }

    private static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
