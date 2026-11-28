-- Thêm 2 lớp mới (EE24M và IT24A), mỗi lớp 15 sinh viên + các lớp học phần tương ứng
USE [QuanLyDiem];
GO

DECLARE @NewStudents TABLE (
    stt INT,
    student_code NVARCHAR(50),
    full_name NVARCHAR(255),
    date_of_birth NVARCHAR(20),
    email NVARCHAR(255),
    phone NVARCHAR(20),
    class_name NVARCHAR(100)
);

-- 15 sinh viên lớp EE24M
INSERT INTO @NewStudents (stt, student_code, full_name, date_of_birth, email, phone, class_name) VALUES
(33, N'110801', N'Nguyễn Văn Bảo',      N'12/03/2006', N'110801@donga.edu.vn', N'0905110801', N'EE24M'),
(34, N'110802', N'Trần Minh Chiến',     N'24/05/2006', N'110802@donga.edu.vn', N'0905110802', N'EE24M'),
(35, N'110803', N'Lê Hoàng Danh',       N'09/07/2006', N'110803@donga.edu.vn', N'0905110803', N'EE24M'),
(36, N'110804', N'Phạm Quốc Đạt',       N'18/08/2006', N'110804@donga.edu.vn', N'0905110804', N'EE24M'),
(37, N'110805', N'Hoàng Gia Huy',       N'02/11/2006', N'110805@donga.edu.vn', N'0905110805', N'EE24M'),
(38, N'110806', N'Vũ Ngọc Khải',        N'15/01/2006', N'110806@donga.edu.vn', N'0905110806', N'EE24M'),
(39, N'110807', N'Đặng Thành Long',     N'27/04/2006', N'110807@donga.edu.vn', N'0905110807', N'EE24M'),
(40, N'110808', N'Bùi Quang Mạnh',      N'06/09/2006', N'110808@donga.edu.vn', N'0905110808', N'EE24M'),
(41, N'110809', N'Ngô Đức Nghĩa',       N'19/10/2006', N'110809@donga.edu.vn', N'0905110809', N'EE24M'),
(42, N'110810', N'Dương Tấn Phát',      N'30/12/2005', N'110810@donga.edu.vn', N'0905110810', N'EE24M'),
(43, N'110811', N'Lý Văn Quân',         N'11/02/2006', N'110811@donga.edu.vn', N'0905110811', N'EE24M'),
(44, N'110812', N'Phan Hữu Sang',       N'22/06/2006', N'110812@donga.edu.vn', N'0905110812', N'EE24M'),
(45, N'110813', N'Tô Minh Tâm',         N'04/08/2006', N'110813@donga.edu.vn', N'0905110813', N'EE24M'),
(46, N'110814', N'Võ Đình Toàn',        N'14/03/2006', N'110814@donga.edu.vn', N'0905110814', N'EE24M'),
(47, N'110815', N'Hồ Anh Tuấn',         N'29/05/2006', N'110815@donga.edu.vn', N'0905110815', N'EE24M'),

-- 15 sinh viên lớp IT24A
(48, N'110901', N'Nguyễn Thị Ngọc Ánh', N'05/01/2006', N'110901@donga.edu.vn', N'0905110901', N'IT24A'),
(49, N'110902', N'Trần Gia Bách',       N'17/04/2006', N'110902@donga.edu.vn', N'0905110902', N'IT24A'),
(50, N'110903', N'Lê Minh Châu',        N'28/07/2006', N'110903@donga.edu.vn', N'0905110903', N'IT24A'),
(51, N'110904', N'Phạm Tiến Đạt',       N'10/10/2006', N'110904@donga.edu.vn', N'0905110904', N'IT24A'),
(52, N'110905', N'Huỳnh Bảo Hân',       N'21/03/2006', N'110905@donga.edu.vn', N'0905110905', N'IT24A'),
(53, N'110906', N'Đoàn Trung Hiếu',     N'13/06/2006', N'110906@donga.edu.vn', N'0905110906', N'IT24A'),
(54, N'110907', N'Nguyễn Khánh Linh',   N'03/09/2006', N'110907@donga.edu.vn', N'0905110907', N'IT24A'),
(55, N'110908', N'Trương Hoàng Long',   N'26/11/2006', N'110908@donga.edu.vn', N'0905110908', N'IT24A'),
(56, N'110909', N'Lê Thảo My',          N'08/02/2006', N'110909@donga.edu.vn', N'0905110909', N'IT24A'),
(57, N'110910', N'Bùi Nhật Nam',        N'19/05/2006', N'110910@donga.edu.vn', N'0905110910', N'IT24A'),
(58, N'110911', N'Vũ Phương Nhi',       N'01/08/2006', N'110911@donga.edu.vn', N'0905110911', N'IT24A'),
(59, N'110912', N'Đinh Hoàng Phúc',     N'14/12/2005', N'110912@donga.edu.vn', N'0905110912', N'IT24A'),
(60, N'110913', N'Mai Thanh Sơn',       N'25/04/2006', N'110913@donga.edu.vn', N'0905110913', N'IT24A'),
(61, N'110914', N'Cao Minh Thư',        N'07/07/2006', N'110914@donga.edu.vn', N'0905110914', N'IT24A'),
(62, N'110915', N'Tăng Quốc Việt',      N'20/10/2006', N'110915@donga.edu.vn', N'0905110915', N'IT24A');

-- Chèn sinh viên chưa tồn tại vào bảng students và students_
INSERT INTO dbo.students (id, student_code, full_name, date_of_birth, email, phone, class_name, stt)
SELECT
    CAST(HASHBYTES('MD5', ns.student_code) AS UNIQUEIDENTIFIER),
    ns.student_code, ns.full_name, ns.date_of_birth, ns.email, ns.phone, ns.class_name, ns.stt
FROM @NewStudents ns
WHERE NOT EXISTS (SELECT 1 FROM dbo.students s WHERE s.student_code = ns.student_code);

INSERT INTO dbo.students_ (id, code, full_name, is_active)
SELECT
    CAST(HASHBYTES('MD5', ns.student_code) AS UNIQUEIDENTIFIER),
    ns.student_code, ns.full_name, 1
FROM @NewStudents ns
WHERE NOT EXISTS (SELECT 1 FROM dbo.students_ st WHERE st.code = ns.student_code);

-- Thêm các lớp học phần mới cho EE24M và IT24A
DECLARE @EeMachId UNIQUEIDENTIFIER = 'E1000000-0000-0000-0000-000000000001';
DECLARE @EePlcId  UNIQUEIDENTIFIER = 'E2000000-0000-0000-0000-000000000002';
DECLARE @ItAJavaId UNIQUEIDENTIFIER = 'A1000000-0000-0000-0000-000000000001';

IF NOT EXISTS (SELECT 1 FROM dbo.courses_classes WHERE code = 'EE24M-MACH')
    INSERT INTO dbo.courses_classes (id, code, name, is_active) VALUES (@EeMachId, 'EE24M-MACH', N'Mạch điện tử cơ bản - Lớp EE24M', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.courses_classes WHERE code = 'EE24M-PLC')
    INSERT INTO dbo.courses_classes (id, code, name, is_active) VALUES (@EePlcId, 'EE24M-PLC', N'Điều khiển lập trình PLC - Lớp EE24M', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.courses_classes WHERE code = 'IT24A-JAVA')
    INSERT INTO dbo.courses_classes (id, code, name, is_active) VALUES (@ItAJavaId, 'IT24A-JAVA', N'Lập trình Web Java - Lớp IT24A', 1);

-- Khởi tạo thành phần điểm mặc định (CC 20%, GK 20%, CK 60%) cho các lớp học phần mới nếu chưa có
INSERT INTO dbo.grade_components (id, course_class_id, component_code, component_name, weight_percentage, min_score, max_score, is_required, input_order, is_active, created_at)
SELECT NEWID(), c.id, v.code, v.cname, v.w, 0.00, 10.00, 1, v.ord, 1, CURRENT_TIMESTAMP
FROM dbo.courses_classes c
CROSS JOIN (VALUES
    ('CC', N'Chuyên cần', 20.00, 1),
    ('GK', N'Kiểm tra giữa kỳ', 20.00, 2),
    ('CK', N'Thi kết thúc học phần', 60.00, 3)
) AS v(code, cname, w, ord)
WHERE c.code IN ('EE24M-MACH', 'EE24M-PLC', 'IT24A-JAVA')
  AND NOT EXISTS (SELECT 1 FROM dbo.grade_components gc WHERE gc.course_class_id = c.id AND gc.deleted_at IS NULL);

-- Đăng ký sinh viên EE24M vào EE24M-MACH và EE24M-PLC
INSERT INTO dbo.course_registrations (id, course_class_id, student_id, is_active, created_at)
SELECT NEWID(), c.id, s.id, 1, CURRENT_TIMESTAMP
FROM dbo.courses_classes c
JOIN dbo.students s ON s.class_name = N'EE24M'
WHERE c.code IN ('EE24M-MACH', 'EE24M-PLC')
  AND NOT EXISTS (SELECT 1 FROM dbo.course_registrations cr WHERE cr.course_class_id = c.id AND cr.student_id = s.id AND cr.deleted_at IS NULL);

-- Đăng ký sinh viên IT24A vào IT24A-JAVA
INSERT INTO dbo.course_registrations (id, course_class_id, student_id, is_active, created_at)
SELECT NEWID(), c.id, s.id, 1, CURRENT_TIMESTAMP
FROM dbo.courses_classes c
JOIN dbo.students s ON s.class_name = N'IT24A'
WHERE c.code = 'IT24A-JAVA'
  AND NOT EXISTS (SELECT 1 FROM dbo.course_registrations cr WHERE cr.course_class_id = c.id AND cr.student_id = s.id AND cr.deleted_at IS NULL);
GO
