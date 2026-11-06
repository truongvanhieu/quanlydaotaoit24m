-- Cập nhật danh sách sinh viên lớp IT24M (32 sinh viên thực tế, đã loại bỏ sinh viên nghỉ học)
USE [QuanLyDiem];
GO

-- 1. Xóa dữ liệu cũ của sinh viên mẫu
DELETE FROM [dbo].[student_component_grades];
DELETE FROM [dbo].[student_summaries];
DELETE FROM [dbo].[course_registrations];
DELETE FROM [dbo].[students];
DELETE FROM [dbo].[students_];
GO

-- 2. Thêm cột date_of_birth và stt vào students nếu chưa có
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'students' AND COLUMN_NAME = 'date_of_birth')
BEGIN
    ALTER TABLE [dbo].[students] ADD [date_of_birth] NVARCHAR(20);
END
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'students' AND COLUMN_NAME = 'stt')
BEGIN
    ALTER TABLE [dbo].[students] ADD [stt] INT;
END
GO

-- 3. Tạo bảng tạm chứa danh sách 32 sinh viên IT24M
DECLARE @StudentsData TABLE (
    stt INT,
    student_code NVARCHAR(50),
    full_name NVARCHAR(255),
    date_of_birth NVARCHAR(20),
    email NVARCHAR(255),
    phone NVARCHAR(20),
    class_name NVARCHAR(100)
);

INSERT INTO @StudentsData (stt, student_code, full_name, date_of_birth, email, phone, class_name) VALUES
(1,  N'109745', N'Nguyễn Hùng Ái',         N'08/02/2006', N'109745@donga.edu.vn', N'0905109745', N'IT24M'),
(2,  N'108217', N'Trần Công Anh',          N'16/08/2006', N'108217@donga.edu.vn', N'0905108217', N'IT24M'),
(3,  N'107350', N'Trương Kỳ Gia Bảo',      N'22/09/2006', N'107350@donga.edu.vn', N'0905107350', N'IT24M'),
(4,  N'107313', N'Đỗ Lâm Nhật Bình',       N'25/11/2006', N'107313@donga.edu.vn', N'0905107313', N'IT24M'),
(5,  N'107320', N'Vũ Tiến Dũng',           N'16/12/2005', N'107320@donga.edu.vn', N'0905107320', N'IT24M'),
(6,  N'110452', N'Nguyễn Minh Hải',        N'04/10/2006', N'110452@donga.edu.vn', N'0905110452', N'IT24M'),
(7,  N'110732', N'Hứa Việt Hoàng',         N'23/03/2005', N'110732@donga.edu.vn', N'0905110732', N'IT24M'),
(8,  N'110210', N'Nguyễn Phước Hoàng',     N'19/11/2006', N'110210@donga.edu.vn', N'0905110210', N'IT24M'),
(9,  N'109789', N'Trịnh Thanh Học',        N'04/12/2005', N'109789@donga.edu.vn', N'0905109789', N'IT24M'),
(10, N'109799', N'Trần Quốc Hưng',         N'08/12/2006', N'109799@donga.edu.vn', N'0905109799', N'IT24M'),
(11, N'109169', N'Trần Quốc Hương',        N'28/03/2006', N'109169@donga.edu.vn', N'0905109169', N'IT24M'),
(12, N'107673', N'Võ Nguyễn Minh Khanh',   N'20/05/2006', N'107673@donga.edu.vn', N'0905107673', N'IT24M'),
(13, N'107204', N'Bùi Anh Khoa',           N'25/11/2006', N'107204@donga.edu.vn', N'0905107204', N'IT24M'),
(14, N'108211', N'Lê Nguyễn Minh Khoa',    N'11/07/2006', N'108211@donga.edu.vn', N'0905108211', N'IT24M'),
(15, N'109066', N'Nguyễn Anh Khoa',        N'06/12/2006', N'109066@donga.edu.vn', N'0905109066', N'IT24M'),
(16, N'109740', N'Nguyễn Đăng Khoa',       N'07/02/2005', N'109740@donga.edu.vn', N'0905109740', N'IT24M'),
(17, N'108631', N'Nguyễn Đình Lộc',        N'10/04/2006', N'108631@donga.edu.vn', N'0905108631', N'IT24M'),
(18, N'109045', N'Bùi Nguyễn Hoàng Minh',  N'06/09/2006', N'109045@donga.edu.vn', N'0905109045', N'IT24M'),
(19, N'110065', N'Đặng Quốc Nam',          N'30/05/2006', N'110065@donga.edu.vn', N'0905110065', N'IT24M'),
(20, N'110695', N'Y BING ALIÔ NIÊ',        N'11/09/2006', N'110695@donga.edu.vn', N'0905110695', N'IT24M'),
(21, N'107496', N'Ma Hữu Ninh',            N'28/06/2006', N'107496@donga.edu.vn', N'0905107496', N'IT24M'),
(22, N'107328', N'Lê Trung Phong',         N'03/07/2001', N'107328@donga.edu.vn', N'0905107328', N'IT24M'),
(23, N'109984', N'Lê Bùi Hoàng Phúc',      N'05/10/2006', N'109984@donga.edu.vn', N'0905109984', N'IT24M'),
(24, N'109443', N'Lê Duy Tài',             N'23/04/2006', N'109443@donga.edu.vn', N'0905109443', N'IT24M'),
(25, N'108848', N'Lục Quang Thắng',        N'19/03/2006', N'108848@donga.edu.vn', N'0905108848', N'IT24M'),
(26, N'109802', N'Lường Văn Thiện',        N'29/01/2006', N'109802@donga.edu.vn', N'0905109802', N'IT24M'),
(27, N'110431', N'KPĂ Trần Quang Thịnh',   N'16/05/2006', N'110431@donga.edu.vn', N'0905110431', N'IT24M'),
(28, N'109214', N'Phan Thái Thuận',        N'02/06/2006', N'109214@donga.edu.vn', N'0905109214', N'IT24M'),
(29, N'108740', N'Lê Minh Trí',            N'27/07/2006', N'108740@donga.edu.vn', N'0905108740', N'IT24M'),
(30, N'110599', N'Trần Đức Trí',           N'21/09/2003', N'110599@donga.edu.vn', N'0905110599', N'IT24M'),
(31, N'108815', N'Đỗ Đinh Minh Trọng',     N'28/03/2006', N'108815@donga.edu.vn', N'0905108815', N'IT24M'),
(32, N'109687', N'Huỳnh Chí Vĩ',           N'30/01/2006', N'109687@donga.edu.vn', N'0905109687', N'IT24M');

-- 4. Chèn vào bảng students và students_ với ID tương ứng
INSERT INTO [dbo].[students] (id, student_code, full_name, date_of_birth, email, phone, class_name, stt)
SELECT 
    CAST(HASHBYTES('MD5', student_code) AS UNIQUEIDENTIFIER) AS id,
    student_code,
    full_name,
    date_of_birth,
    email,
    phone,
    class_name,
    stt
FROM @StudentsData;

INSERT INTO [dbo].[students_] (id, code, full_name, is_active)
SELECT 
    CAST(HASHBYTES('MD5', student_code) AS UNIQUEIDENTIFIER) AS id,
    student_code,
    full_name,
    1
FROM @StudentsData;

-- 5. Đăng ký các sinh viên vào các lớp học phần hiện có
-- Lớp IT24M-JAVA
INSERT INTO [dbo].[course_registrations] (id, course_class_id, student_id, is_active, created_at)
SELECT 
    NEWID(),
    c.id,
    s.id,
    1,
    CURRENT_TIMESTAMP
FROM [dbo].[courses_classes] c
CROSS JOIN [dbo].[students] s
WHERE c.code = 'IT24M-JAVA';

-- Lớp IT24M-CSDL
INSERT INTO [dbo].[course_registrations] (id, course_class_id, student_id, is_active, created_at)
SELECT 
    NEWID(),
    c.id,
    s.id,
    1,
    CURRENT_TIMESTAMP
FROM [dbo].[courses_classes] c
CROSS JOIN [dbo].[students] s
WHERE c.code = 'IT24M-CSDL';

-- 6. Tạo một số điểm mẫu cho các sinh viên đầu tiên ở lớp Java để giao diện có số liệu mẫu đẹp mắt
DECLARE @JavaClassId UNIQUEIDENTIFIER = 'C1000000-0000-0000-0000-000000000001';
DECLARE @CompCC UNIQUEIDENTIFIER = 'D1000000-0000-0000-0000-000000000001'; -- Chuyên cần 10%
DECLARE @CompGK UNIQUEIDENTIFIER = 'D2000000-0000-0000-0000-000000000002'; -- Giữa kỳ 30%
DECLARE @CompCK UNIQUEIDENTIFIER = 'D3000000-0000-0000-0000-000000000003'; -- Cuối kỳ 60%

-- Nhập mẫu điểm cho sinh viên đầu tiên (Nguyễn Hùng Ái)
INSERT INTO [dbo].[student_component_grades] (id, registration_id, component_id, score, is_retake, is_locked, is_active, created_at)
SELECT NEWID(), cr.id, @CompCC, 9.0, 0, 0, 1, CURRENT_TIMESTAMP
FROM [dbo].[course_registrations] cr
JOIN [dbo].[students] s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '109745';

INSERT INTO [dbo].[student_component_grades] (id, registration_id, component_id, score, is_retake, is_locked, is_active, created_at)
SELECT NEWID(), cr.id, @CompGK, 8.5, 0, 0, 1, CURRENT_TIMESTAMP
FROM [dbo].[course_registrations] cr
JOIN [dbo].[students] s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '109745';

INSERT INTO [dbo].[student_component_grades] (id, registration_id, component_id, score, is_retake, is_locked, is_active, created_at)
SELECT NEWID(), cr.id, @CompCK, 8.0, 0, 0, 1, CURRENT_TIMESTAMP
FROM [dbo].[course_registrations] cr
JOIN [dbo].[students] s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '109745';

-- Nhập mẫu điểm cho KPĂ Trần Quang Thịnh (110431)
INSERT INTO [dbo].[student_component_grades] (id, registration_id, component_id, score, is_retake, is_locked, is_active, created_at)
SELECT NEWID(), cr.id, @CompCC, 10.0, 0, 0, 1, CURRENT_TIMESTAMP
FROM [dbo].[course_registrations] cr
JOIN [dbo].[students] s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '110431';

INSERT INTO [dbo].[student_component_grades] (id, registration_id, component_id, score, is_retake, is_locked, is_active, created_at)
SELECT NEWID(), cr.id, @CompGK, 9.0, 0, 0, 1, CURRENT_TIMESTAMP
FROM [dbo].[course_registrations] cr
JOIN [dbo].[students] s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '110431';

INSERT INTO [dbo].[student_component_grades] (id, registration_id, component_id, score, is_retake, is_locked, is_active, created_at)
SELECT NEWID(), cr.id, @CompCK, 9.5, 0, 0, 1, CURRENT_TIMESTAMP
FROM [dbo].[course_registrations] cr
JOIN [dbo].[students] s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '110431';

GO
