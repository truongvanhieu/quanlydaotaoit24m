/* =====================================================================
   DỮ LIỆU MẪU - NHÓM IX: HỌC PHÍ - TÀI CHÍNH (N9)
   Các bảng: tuition_fees, student_tuition, payments

   Cách chạy:
     - SSMS  : mở file này rồi bấm Execute
     - Dòng lệnh: sqlcmd -S localhost -U sa -P 123123 -C -d it24m -i n9_seed.sql

   LƯU Ý: script sẽ XÓA toàn bộ dữ liệu hiện có của 3 bảng trên rồi nạp lại.
   Số lượng: 8 định mức học phí, 60 học phí sinh viên, 120 giao dịch thanh toán.
   ===================================================================== */

USE [it24m];
GO

SET NOCOUNT ON;
GO

/* ---------------------------------------------------------------------
   0. Dọn dữ liệu cũ (theo thứ tự khóa ngoại: payments -> student_tuition -> tuition_fees)
   --------------------------------------------------------------------- */
DELETE FROM payments;
DELETE FROM student_tuition;
DELETE FROM tuition_fees;
GO

/* ---------------------------------------------------------------------
   1. ĐỊNH MỨC HỌC PHÍ (tuition_fees) - mỗi chương trình đào tạo 1 mức phí
   --------------------------------------------------------------------- */
WITH r AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY name) AS rn
    FROM training_programs
)
INSERT INTO tuition_fees
    (id, program_id, course_year, price_per_credit, base_tuition, effective_date, is_active, created_at)
SELECT
    NEWID(),
    r.id,
    CASE r.rn % 4 WHEN 0 THEN 'K21' WHEN 1 THEN 'K22' WHEN 2 THEN 'K23' ELSE 'K24' END,
    400000 + (r.rn - 1) * 25000,      -- 400.000 -> 575.000 đồng / tín chỉ
    1000000 + (r.rn - 1) * 100000,    -- 1.000.000 -> 1.700.000 đồng / học kỳ
    '2025-09-01',
    1,
    GETDATE()
FROM r;
GO

/* ---------------------------------------------------------------------
   2. HỌC PHÍ SINH VIÊN THEO HỌC KỲ (student_tuition) - 60 bản ghi
      - Mỗi sinh viên dùng đúng định mức của chương trình mình đang học
      - Chia đều 4 trạng thái: 1-PAID, 2-PARTIAL, 3-DEBT, 4-OVERDUE
      - Sinh viên có số thứ tự chia hết cho 5 được giảm 10% học bổng
   --------------------------------------------------------------------- */
IF OBJECT_ID('tempdb..#plan1') IS NOT NULL DROP TABLE #plan1;
IF OBJECT_ID('tempdb..#plan')  IS NOT NULL DROP TABLE #plan;

;WITH sv AS (
    SELECT TOP 60
           s.id                  AS student_id,
           s.training_program_id AS program_id,
           ROW_NUMBER() OVER (ORDER BY NEWID()) AS rn
    FROM students_ s
    WHERE s.deleted_at IS NULL
      AND s.training_program_id IS NOT NULL
),
sem AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY name DESC) AS rn
    FROM semesters
),
b AS (
    SELECT sv.rn,
           sv.student_id,
           sem.id AS semester_id,
           f.id   AS fee_id,
           15 + (sv.rn % 11) AS credits,
           f.price_per_credit * (15 + (sv.rn % 11)) + ISNULL(f.base_tuition, 0) AS raw_amount
    FROM sv
    JOIN sem ON sem.rn = (sv.rn % 4) + 1
    JOIN tuition_fees f ON f.program_id = sv.program_id
),
c AS (
    SELECT b.*,
           CASE WHEN b.rn % 5 = 0 THEN ROUND(b.raw_amount * 0.10, 0) ELSE 0 END AS scholarship
    FROM b
),
d AS (
    SELECT c.*, c.raw_amount - c.scholarship AS net_amount
    FROM c
)
SELECT
    NEWID() AS st_id,
    d.rn,
    d.student_id,
    d.semester_id,
    d.fee_id,
    d.credits,
    d.raw_amount,
    d.scholarship,
    CAST(0 AS DECIMAL(15, 2)) AS exemption_amount,
    d.net_amount,
    CASE d.rn % 4
         WHEN 0 THEN d.net_amount                      -- đã nộp đủ
         WHEN 1 THEN ROUND(d.net_amount * 0.45, 0)     -- nộp một phần
         WHEN 2 THEN 0                                 -- chưa nộp
         ELSE ROUND(d.net_amount * 0.30, 0)            -- quá hạn, nộp một phần
    END AS paid_amount
INTO #plan1
FROM d;

SELECT
    *,
    (net_amount - paid_amount) AS debt_amount,
    CASE rn % 4 WHEN 0 THEN 1 WHEN 1 THEN 2 WHEN 2 THEN 3 ELSE 4 END AS status,
    CASE rn % 4 WHEN 3 THEN CAST('2026-06-30' AS DATE) ELSE CAST('2026-12-31' AS DATE) END AS deadline
INTO #plan
FROM #plan1;

INSERT INTO student_tuition
    (id, student_id, semester_id, tuition_fee_id, total_credits, raw_amount,
     scholarship_deduction, exemption_amount, net_amount, paid_amount, debt_amount,
     status, deadline, is_active, created_at)
SELECT
    st_id, student_id, semester_id, fee_id, credits, raw_amount,
    scholarship, exemption_amount, net_amount, paid_amount, debt_amount,
    status, deadline, 1, DATEADD(DAY, -rn, GETDATE())
FROM #plan;
GO

/* ---------------------------------------------------------------------
   3. GIAO DỊCH THANH TOÁN (payments) - 2 giao dịch / học phí = 120 bản ghi
        PAID    : 1 SUCCESS (60%) + 1 SUCCESS (phần còn lại)
        PARTIAL : 1 SUCCESS (đúng số đã nộp) + 1 FAILED
        DEBT    : 1 FAILED + 1 PENDING
        OVERDUE : 1 SUCCESS (đúng số đã nộp) + 1 CANCELLED
   --------------------------------------------------------------------- */
INSERT INTO payments
    (id, tuition_id, amount_paid, payment_date, payment_method, payment_status,
     transaction_ref, notes, is_active, created_at)
SELECT
    NEWID(),
    p.st_id,
    CASE
        WHEN n.n = 1 THEN
            CASE p.status
                 WHEN 1 THEN ROUND(p.net_amount * 0.60, 0)
                 WHEN 2 THEN p.paid_amount
                 WHEN 3 THEN ROUND(p.net_amount * 0.50, 0)
                 ELSE p.paid_amount
            END
        ELSE
            CASE p.status
                 WHEN 1 THEN p.net_amount - ROUND(p.net_amount * 0.60, 0)
                 WHEN 2 THEN ROUND(p.net_amount * 0.35, 0)
                 WHEN 3 THEN ROUND(p.net_amount * 0.50, 0)
                 ELSE ROUND(p.net_amount * 0.20, 0)
            END
    END,
    DATEADD(DAY, -(p.rn * 2 + n.n), GETDATE()),
    ((p.rn + n.n) % 3) + 1,                                  -- 1 chuyển khoản, 2 tiền mặt, 3 ví điện tử
    CASE p.status
         WHEN 1 THEN 'SUCCESS'
         WHEN 2 THEN CASE WHEN n.n = 1 THEN 'SUCCESS' ELSE 'FAILED' END
         WHEN 3 THEN CASE WHEN n.n = 1 THEN 'FAILED'  ELSE 'PENDING' END
         ELSE        CASE WHEN n.n = 1 THEN 'SUCCESS' ELSE 'CANCELLED' END
    END,
    'PT' + RIGHT('000000' + CAST(p.rn * 10 + n.n AS varchar(10)), 6),
    N'Thu học phí ' + CASE p.status WHEN 1 THEN N'đủ' WHEN 2 THEN N'một phần' WHEN 3 THEN N'chưa nộp' ELSE N'quá hạn' END,
    CASE WHEN p.status = 4 AND n.n = 2 THEN 0 ELSE 1 END,    -- giao dịch CANCELLED thì is_active = 0
    GETDATE()
FROM #plan p
CROSS JOIN (VALUES (1), (2)) AS n(n);
GO

/* ---------------------------------------------------------------------
   4. KIỂM TRA KẾT QUẢ
   --------------------------------------------------------------------- */
SELECT N'tuition_fees' AS bang, COUNT(*) AS so_dong FROM tuition_fees
UNION ALL SELECT N'student_tuition', COUNT(*) FROM student_tuition
UNION ALL SELECT N'payments', COUNT(*) FROM payments;

SELECT status,
       CASE status WHEN 1 THEN N'PAID' WHEN 2 THEN N'PARTIAL' WHEN 3 THEN N'DEBT' ELSE N'OVERDUE' END AS trang_thai,
       COUNT(*) AS so_luong
FROM student_tuition
GROUP BY status
ORDER BY status;

/* Số dòng lệch phải = 0 (paid_amount khớp tổng giao dịch SUCCESS) */
SELECT COUNT(*) AS so_dong_lech
FROM student_tuition st
WHERE st.paid_amount <> ISNULL((
        SELECT SUM(p.amount_paid)
        FROM payments p
        WHERE p.tuition_id = st.id
          AND p.payment_status = 'SUCCESS'
          AND p.is_active = 1
          AND p.deleted_at IS NULL
      ), 0);
GO

DROP TABLE #plan;
DROP TABLE #plan1;
GO
