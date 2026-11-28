-- Bổ sung đủ 30 sinh viên cho EE24M & IT24A, đủ 3 học phần mỗi ngành và đăng ký đủ sinh viên vào từng học phần
USE [QuanLyDiem];
GO

DECLARE @MoreStudents TABLE (
    stt INT,
    student_code NVARCHAR(50),
    full_name NVARCHAR(255),
    date_of_birth NVARCHAR(20),
    email NVARCHAR(255),
    phone NVARCHAR(20),
    class_name NVARCHAR(100)
);

-- Thêm 15 sinh viên cho lớp EE24M (tổng 30 SV: 110801 - 110830)
INSERT INTO @MoreStudents (stt, student_code, full_name, date_of_birth, email, phone, class_name) VALUES
(63, N'110816', N'Nguyễn Tiến An',       N'03/02/2006', N'110816@donga.edu.vn', N'0905110816', N'EE24M'),
(64, N'110817', N'Trần Quốc Bình',       N'15/04/2006', N'110817@donga.edu.vn', N'0905110817', N'EE24M'),
(65, N'110818', N'Lê Văn Cường',         N'21/06/2006', N'110818@donga.edu.vn', N'0905110818', N'EE24M'),
(66, N'110819', N'Phạm Minh Đức',        N'08/09/2006', N'110819@donga.edu.vn', N'0905110819', N'EE24M'),
(67, N'110820', N'Hoàng Thanh Hải',      N'19/11/2006', N'110820@donga.edu.vn', N'0905110820', N'EE24M'),
(68, N'110821', N'Vũ Trọng Hiếu',        N'12/01/2006', N'110821@donga.edu.vn', N'0905110821', N'EE24M'),
(69, N'110822', N'Đặng Nhật Hoàng',      N'25/03/2006', N'110822@donga.edu.vn', N'0905110822', N'EE24M'),
(70, N'110823', N'Bùi Đình Hưng',        N'17/05/2006', N'110823@donga.edu.vn', N'0905110823', N'EE24M'),
(71, N'110824', N'Ngô Minh Khang',       N'29/07/2006', N'110824@donga.edu.vn', N'0905110824', N'EE24M'),
(72, N'110825', N'Dương Văn Khoa',       N'11/10/2006', N'110825@donga.edu.vn', N'0905110825', N'EE24M'),
(73, N'110826', N'Lý Hoàng Lâm',         N'04/12/2005', N'110826@donga.edu.vn', N'0905110826', N'EE24M'),
(74, N'110827', N'Phan Văn Lộc',         N'22/02/2006', N'110827@donga.edu.vn', N'0905110827', N'EE24M'),
(75, N'110828', N'Tô Quang Minh',        N'14/06/2006', N'110828@donga.edu.vn', N'0905110828', N'EE24M'),
(76, N'110829', N'Võ Hữu Nghĩa',         N'09/08/2006', N'110829@donga.edu.vn', N'0905110829', N'EE24M'),
(77, N'110830', N'Hồ Tấn Tài',           N'28/10/2006', N'110830@donga.edu.vn', N'0905110830', N'EE24M'),

-- Thêm 15 sinh viên cho lớp IT24A (tổng 30 SV: 110901 - 110930)
(78, N'110916', N'Nguyễn Hoàng Anh',     N'07/01/2006', N'110916@donga.edu.vn', N'0905110916', N'IT24A'),
(79, N'110917', N'Trần Thúy Bình',       N'18/03/2006', N'110917@donga.edu.vn', N'0905110917', N'IT24A'),
(80, N'110918', N'Lê Quốc Bảo',          N'24/05/2006', N'110918@donga.edu.vn', N'0905110918', N'IT24A'),
(81, N'110919', N'Phạm Gia Hân',         N'11/07/2006', N'110919@donga.edu.vn', N'0905110919', N'IT24A'),
(82, N'110920', N'Huỳnh Minh Hiếu',      N'02/09/2006', N'110920@donga.edu.vn', N'0905110920', N'IT24A'),
(83, N'110921', N'Đoàn Ngọc Huyền',      N'16/11/2006', N'110921@donga.edu.vn', N'0905110921', N'IT24A'),
(84, N'110922', N'Nguyễn Tuấn Khải',     N'20/02/2006', N'110922@donga.edu.vn', N'0905110922', N'IT24A'),
(85, N'110923', N'Trương Bảo Khánh',     N'05/04/2006', N'110923@donga.edu.vn', N'0905110923', N'IT24A'),
(86, N'110924', N'Lê Hoàng Khoa',        N'13/06/2006', N'110924@donga.edu.vn', N'0905110924', N'IT24A'),
(87, N'110925', N'Bùi Phương Linh',      N'27/08/2006', N'110925@donga.edu.vn', N'0905110925', N'IT24A'),
(88, N'110926', N'Vũ Thành Long',        N'09/10/2006', N'110926@donga.edu.vn', N'0905110926', N'IT24A'),
(89, N'110927', N'Đinh Thảo My',         N'22/12/2005', N'110927@donga.edu.vn', N'0905110927', N'IT24A'),
(90, N'110928', N'Mai Hải Nam',          N'14/03/2006', N'110928@donga.edu.vn', N'0905110928', N'IT24A'),
(91, N'110929', N'Cao Bảo Ngọc',         N'30/05/2006', N'110929@donga.edu.vn', N'0905110929', N'IT24A'),
(92, N'110930', N'Tăng Đức Thắng',       N'18/09/2006', N'110930@donga.edu.vn', N'0905110930', N'IT24A');

INSERT INTO dbo.students (id, student_code, full_name, date_of_birth, email, phone, class_name, stt)
SELECT
    CAST(HASHBYTES('MD5', ms.student_code) AS UNIQUEIDENTIFIER),
    ms.student_code, ms.full_name, ms.date_of_birth, ms.email, ms.phone, ms.class_name, ms.stt
FROM @MoreStudents ms
WHERE NOT EXISTS (SELECT 1 FROM dbo.students s WHERE s.student_code = ms.student_code);

INSERT INTO dbo.students_ (id, code, full_name, is_active)
SELECT
    CAST(HASHBYTES('MD5', ms.student_code) AS UNIQUEIDENTIFIER),
    ms.student_code, ms.full_name, 1
FROM @MoreStudents ms
WHERE NOT EXISTS (SELECT 1 FROM dbo.students_ st WHERE st.code = ms.student_code);

-- Thêm các lớp học phần để mỗi ngành/lớp có đủ 3 lớp học phần
IF NOT EXISTS (SELECT 1 FROM dbo.courses_classes WHERE code = 'EE24M-DKTD')
    INSERT INTO dbo.courses_classes (id, code, name, is_active)
    VALUES ('E3000000-0000-0000-0000-000000000003', 'EE24M-DKTD', N'Điều khiển tự động - Lớp EE24M', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.courses_classes WHERE code = 'IT24A-CSDL')
    INSERT INTO dbo.courses_classes (id, code, name, is_active)
    VALUES ('A2000000-0000-0000-0000-000000000002', 'IT24A-CSDL', N'Hệ quản trị CSDL - Lớp IT24A', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.courses_classes WHERE code = 'IT24A-CTDL')
    INSERT INTO dbo.courses_classes (id, code, name, is_active)
    VALUES ('A3000000-0000-0000-0000-000000000003', 'IT24A-CTDL', N'Cấu trúc dữ liệu & Giải thuật - Lớp IT24A', 1);

-- Khởi tạo thành phần điểm mặc định (20% - 20% - 60%) cho các lớp mới
INSERT INTO dbo.grade_components (id, course_class_id, component_code, component_name, weight_percentage, min_score, max_score, is_required, input_order, is_active, created_at)
SELECT NEWID(), c.id, v.code, v.cname, v.w, 0.00, 10.00, 1, v.ord, 1, CURRENT_TIMESTAMP
FROM dbo.courses_classes c
CROSS JOIN (VALUES
    ('CC', N'Chuyên cần', 20.00, 1),
    ('GK', N'Kiểm tra giữa kỳ', 20.00, 2),
    ('CK', N'Thi kết thúc học phần', 60.00, 3)
) AS v(code, cname, w, ord)
WHERE c.code IN ('EE24M-DKTD', 'IT24A-CSDL', 'IT24A-CTDL')
  AND NOT EXISTS (SELECT 1 FROM dbo.grade_components gc WHERE gc.course_class_id = c.id AND gc.deleted_at IS NULL);

-- Đăng ký đủ 32 SV IT24M vào tất cả học phần IT24M
INSERT INTO dbo.course_registrations (id, course_class_id, student_id, is_active, created_at)
SELECT NEWID(), c.id, s.id, 1, CURRENT_TIMESTAMP
FROM dbo.courses_classes c
JOIN dbo.students s ON s.class_name = N'IT24M'
WHERE c.code LIKE 'IT24M-%' AND c.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM dbo.course_registrations cr WHERE cr.course_class_id = c.id AND cr.student_id = s.id AND cr.deleted_at IS NULL);

-- Đăng ký đủ 30 SV EE24M vào tất cả học phần EE24M
INSERT INTO dbo.course_registrations (id, course_class_id, student_id, is_active, created_at)
SELECT NEWID(), c.id, s.id, 1, CURRENT_TIMESTAMP
FROM dbo.courses_classes c
JOIN dbo.students s ON s.class_name = N'EE24M'
WHERE c.code LIKE 'EE24M-%' AND c.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM dbo.course_registrations cr WHERE cr.course_class_id = c.id AND cr.student_id = s.id AND cr.deleted_at IS NULL);

-- Đăng ký đủ 30 SV IT24A vào tất cả học phần IT24A
INSERT INTO dbo.course_registrations (id, course_class_id, student_id, is_active, created_at)
SELECT NEWID(), c.id, s.id, 1, CURRENT_TIMESTAMP
FROM dbo.courses_classes c
JOIN dbo.students s ON s.class_name = N'IT24A'
WHERE c.code LIKE 'IT24A-%' AND c.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM dbo.course_registrations cr WHERE cr.course_class_id = c.id AND cr.student_id = s.id AND cr.deleted_at IS NULL);
GO
