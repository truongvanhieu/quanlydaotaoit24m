-- ======================================================================================
-- Dá»° ĂN QUáº¢N LĂ ÄĂ€O Táº O & ÄĂNH GIĂ ÄIá»‚M Há»ŒC PHáº¦N (NHĂ“M 8 - Lá»P IT24M)
-- DATABASE: QuanLyDiem (Microsoft SQL Server)
-- MĂ£ hĂ³a file: UTF-8 with BOM (Há»— trá»£ tiáº¿ng Viá»‡t cĂ³ dáº¥u 100%)
-- ======================================================================================

IF NOT EXISTS (SELECT 1 FROM sys.databases WHERE name = N'QuanLyDiem')
BEGIN
    CREATE DATABASE [QuanLyDiem];
END
GO

USE [QuanLyDiem];
GO

-- 1. XĂ“A CĂC Báº¢NG THEO THá»¨ Tá»° Náº¾U ÄĂƒ Tá»’N Táº I
IF OBJECT_ID(N'dbo.student_summaries', N'U') IS NOT NULL DROP TABLE dbo.student_summaries;
IF OBJECT_ID(N'dbo.student_component_grades', N'U') IS NOT NULL DROP TABLE dbo.student_component_grades;
IF OBJECT_ID(N'dbo.course_registrations', N'U') IS NOT NULL DROP TABLE dbo.course_registrations;
IF OBJECT_ID(N'dbo.grade_components', N'U') IS NOT NULL DROP TABLE dbo.grade_components;
IF OBJECT_ID(N'dbo.grade_scales', N'U') IS NOT NULL DROP TABLE dbo.grade_scales;
IF OBJECT_ID(N'dbo.courses_classes', N'U') IS NOT NULL DROP TABLE dbo.courses_classes;
IF OBJECT_ID(N'dbo.students_', N'U') IS NOT NULL DROP TABLE dbo.students_;
IF OBJECT_ID(N'dbo.students', N'U') IS NOT NULL DROP TABLE dbo.students;
GO

-- 2. Táº O Cáº¤U TRĂC CĂC Báº¢NG

-- Báº£ng sinh viĂªn (Quáº£n lĂ½ há»“ sÆ¡ sinh viĂªn)
CREATE TABLE dbo.students (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    stt INT NULL,
    student_code NVARCHAR(50) NOT NULL UNIQUE,
    full_name NVARCHAR(255) NOT NULL,
    date_of_birth NVARCHAR(20) NULL,
    email NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    class_name NVARCHAR(100) NOT NULL
);

-- Báº£ng Ä‘á»“ng bá»™ sinh viĂªn phá»¥c vá»¥ module Ä‘iá»ƒm
CREATE TABLE dbo.students_ (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    code NVARCHAR(100) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    deleted_at DATETIME2 NULL
);

-- Báº£ng lá»›p há»c pháº§n
CREATE TABLE dbo.courses_classes (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    code NVARCHAR(100) NOT NULL UNIQUE,
    name NVARCHAR(255) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    deleted_at DATETIME2 NULL
);

-- Báº£ng thang Ä‘iá»ƒm quy Ä‘á»•i GPA vĂ  chá»¯
CREATE TABLE dbo.grade_scales (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    letter_grade VARCHAR(2) NOT NULL,
    gpa_value DECIMAL(3, 2) NOT NULL,
    min_score DECIMAL(4, 2) NOT NULL,
    max_score DECIMAL(4, 2) NOT NULL,
    is_pass BIT NOT NULL,
    description NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1
);

-- Báº£ng cáº¥u hĂ¬nh cĂ¡c thĂ nh pháº§n Ä‘iá»ƒm cá»§a tá»«ng lá»›p há»c pháº§n
CREATE TABLE dbo.grade_components (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    course_class_id UNIQUEIDENTIFIER NOT NULL,
    component_code NVARCHAR(20) NOT NULL,
    component_name NVARCHAR(100) NOT NULL,
    weight_percentage DECIMAL(5, 2) NOT NULL,
    min_score DECIMAL(4, 2) NOT NULL DEFAULT 0.00,
    max_score DECIMAL(4, 2) NOT NULL DEFAULT 10.00,
    is_required BIT NOT NULL DEFAULT 1,
    input_order INT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Báº£ng sinh viĂªn Ä‘Äƒng kĂ½ lá»›p há»c pháº§n
CREATE TABLE dbo.course_registrations (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    course_class_id UNIQUEIDENTIFIER NOT NULL,
    student_id UNIQUEIDENTIFIER NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME2 NULL
);

-- Báº£ng lÆ°u Ä‘iá»ƒm thĂ nh pháº§n chi tiáº¿t cá»§a tá»«ng sinh viĂªn
CREATE TABLE dbo.student_component_grades (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    registration_id UNIQUEIDENTIFIER NOT NULL,
    component_id UNIQUEIDENTIFIER NOT NULL,
    score DECIMAL(4, 2) NOT NULL,
    is_retake BIT NOT NULL DEFAULT 0,
    is_locked BIT NOT NULL DEFAULT 0,
    note NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NULL
);

-- Báº£ng tá»•ng káº¿t Ä‘iá»ƒm há»c pháº§n
CREATE TABLE dbo.student_summaries (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    registration_id UNIQUEIDENTIFIER NOT NULL UNIQUE,
    total_score DECIMAL(4, 2) NULL,
    scale_id UNIQUEIDENTIFIER NULL,
    letter_grade VARCHAR(2) NULL,
    gpa_value DECIMAL(3, 2) NULL,
    result VARCHAR(10) NULL,
    is_finalized BIT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NULL
);
GO

-- ======================================================================================
-- 3. CHĂˆN Dá»® LIá»†U BAN Äáº¦U
-- ======================================================================================

-- A. Dá»¯ liá»‡u thang Ä‘iá»ƒm chuáº©n (A, B, C, D, F)
INSERT INTO dbo.grade_scales (id, letter_grade, gpa_value, min_score, max_score, is_pass, description) VALUES
('11111111-1111-1111-1111-111111111111', 'A', 4.00, 8.50, 10.00, 1, N'Giá»i / Xuáº¥t sáº¯c'),
('22222222-2222-2222-2222-222222222222', 'B', 3.00, 7.00,  8.49, 1, N'KhĂ¡'),
('33333333-3333-3333-3333-333333333333', 'C', 2.00, 5.50,  6.99, 1, N'Trung bĂ¬nh'),
('44444444-4444-4444-4444-444444444444', 'D', 1.00, 4.00,  5.49, 1, N'Yáº¿u'),
('55555555-5555-5555-5555-555555555555', 'F', 0.00, 0.00,  3.99, 0, N'KĂ©m / KhĂ´ng Ä‘áº¡t');

-- B. Dá»¯ liá»‡u cĂ¡c lá»›p há»c pháº§n
INSERT INTO dbo.courses_classes (id, code, name) VALUES
('C1000000-0000-0000-0000-000000000001', 'IT24M-JAVA',   N'Láº­p trĂ¬nh Web Java - Lá»›p IT24M'),
('C2000000-0000-0000-0000-000000000002', 'IT24M-CSDL',   N'Há»‡ quáº£n trá»‹ CSDL - Lá»›p IT24M'),
('77846F66-7DAC-41E1-A6E6-CF1BF4E82300', 'IT24M-PYTHON', N'Láº­p trĂ¬nh Python - Lá»›p IT24M');

-- C. Dá»¯ liá»‡u thĂ nh pháº§n Ä‘iá»ƒm (ChuyĂªn cáº§n, Giá»¯a ká»³, Cuá»‘i ká»³)
INSERT INTO dbo.grade_components (id, course_class_id, component_code, component_name, weight_percentage, min_score, max_score, is_required, input_order) VALUES
-- Lá»›p Web Java (10% - 30% - 60%)
('D1000000-0000-0000-0000-000000000001', 'C1000000-0000-0000-0000-000000000001', 'CC', N'Äiá»ƒm chuyĂªn cáº§n',       10.00, 0.00, 10.00, 1, 1),
('D2000000-0000-0000-0000-000000000002', 'C1000000-0000-0000-0000-000000000001', 'GK', N'Äiá»ƒm giá»¯a ká»³',          30.00, 0.00, 10.00, 1, 2),
('D3000000-0000-0000-0000-000000000003', 'C1000000-0000-0000-0000-000000000001', 'CK', N'Äiá»ƒm cuá»‘i ká»³',          60.00, 0.00, 10.00, 1, 3),

-- Lá»›p CSDL (20% - 20% - 60%)
('D4000000-0000-0000-0000-000000000004', 'C2000000-0000-0000-0000-000000000002', 'CC', N'ChuyĂªn cáº§n',             20.00, 0.00, 10.00, 1, 1),
('D5000000-0000-0000-0000-000000000005', 'C2000000-0000-0000-0000-000000000002', 'GK', N'Kiá»ƒm tra giá»¯a ká»³',       20.00, 0.00, 10.00, 1, 2),
('D6000000-0000-0000-0000-000000000006', 'C2000000-0000-0000-0000-000000000002', 'CK', N'Thi káº¿t thĂºc há»c pháº§n', 60.00, 0.00, 10.00, 1, 3),

-- Lá»›p Python (20% - 20% - 60%)
('28775DA7-5536-4D72-8F62-F9C7C1EFC3A7', '77846F66-7DAC-41E1-A6E6-CF1BF4E82300', 'CC', N'ChuyĂªn cáº§n',             20.00, 0.00, 10.00, 1, 1),
('E355DDB9-459C-4DF6-96BA-FA2EB0FB3EF9', '77846F66-7DAC-41E1-A6E6-CF1BF4E82300', 'GK', N'Kiá»ƒm tra giá»¯a ká»³',       20.00, 0.00, 10.00, 1, 2),
('77D0AA4F-BE0C-4F24-BB6D-9A8B27FD7BF7', '77846F66-7DAC-41E1-A6E6-CF1BF4E82300', 'CK', N'Thi káº¿t thĂºc há»c pháº§n', 60.00, 0.00, 10.00, 1, 3);

-- D. Dá»¯ liá»‡u 32 sinh viĂªn thá»±c táº¿ lá»›p IT24M (loáº¡i bá» sinh viĂªn nghá»‰ há»c)
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
(1,  N'109745', N'Nguyá»…n HĂ¹ng Ăi',         N'08/02/2006', N'109745@donga.edu.vn', N'0905109745', N'IT24M'),
(2,  N'108217', N'Tráº§n CĂ´ng Anh',          N'16/08/2006', N'108217@donga.edu.vn', N'0905108217', N'IT24M'),
(3,  N'107350', N'TrÆ°Æ¡ng Ká»³ Gia Báº£o',      N'22/09/2006', N'107350@donga.edu.vn', N'0905107350', N'IT24M'),
(4,  N'107313', N'Äá»— LĂ¢m Nháº­t BĂ¬nh',       N'25/11/2006', N'107313@donga.edu.vn', N'0905107313', N'IT24M'),
(5,  N'107320', N'VÅ© Tiáº¿n DÅ©ng',           N'16/12/2005', N'107320@donga.edu.vn', N'0905107320', N'IT24M'),
(6,  N'110452', N'Nguyá»…n Minh Háº£i',        N'04/10/2006', N'110452@donga.edu.vn', N'0905110452', N'IT24M'),
(7,  N'110732', N'Há»©a Viá»‡t HoĂ ng',         N'23/03/2005', N'110732@donga.edu.vn', N'0905110732', N'IT24M'),
(8,  N'110210', N'Nguyá»…n PhÆ°á»›c HoĂ ng',     N'19/11/2006', N'110210@donga.edu.vn', N'0905110210', N'IT24M'),
(9,  N'109789', N'Trá»‹nh Thanh Há»c',        N'04/12/2005', N'109789@donga.edu.vn', N'0905109789', N'IT24M'),
(10, N'109799', N'Tráº§n Quá»‘c HÆ°ng',         N'08/12/2006', N'109799@donga.edu.vn', N'0905109799', N'IT24M'),
(11, N'109169', N'Tráº§n Quá»‘c HÆ°Æ¡ng',        N'28/03/2006', N'109169@donga.edu.vn', N'0905109169', N'IT24M'),
(12, N'107673', N'VĂµ Nguyá»…n Minh Khanh',   N'20/05/2006', N'107673@donga.edu.vn', N'0905107673', N'IT24M'),
(13, N'107204', N'BĂ¹i Anh Khoa',           N'25/11/2006', N'107204@donga.edu.vn', N'0905107204', N'IT24M'),
(14, N'108211', N'LĂª Nguyá»…n Minh Khoa',    N'11/07/2006', N'108211@donga.edu.vn', N'0905108211', N'IT24M'),
(15, N'109066', N'Nguyá»…n Anh Khoa',        N'06/12/2006', N'109066@donga.edu.vn', N'0905109066', N'IT24M'),
(16, N'109740', N'Nguyá»…n ÄÄƒng Khoa',       N'07/02/2005', N'109740@donga.edu.vn', N'0905109740', N'IT24M'),
(17, N'108631', N'Nguyá»…n ÄĂ¬nh Lá»™c',        N'10/04/2006', N'108631@donga.edu.vn', N'0905108631', N'IT24M'),
(18, N'109045', N'BĂ¹i Nguyá»…n HoĂ ng Minh',  N'06/09/2006', N'109045@donga.edu.vn', N'0905109045', N'IT24M'),
(19, N'110065', N'Äáº·ng Quá»‘c Nam',          N'30/05/2006', N'110065@donga.edu.vn', N'0905110065', N'IT24M'),
(20, N'110695', N'Y BING ALIĂ” NIĂ',        N'11/09/2006', N'110695@donga.edu.vn', N'0905110695', N'IT24M'),
(21, N'107496', N'Ma Há»¯u Ninh',            N'28/06/2006', N'107496@donga.edu.vn', N'0905107496', N'IT24M'),
(22, N'107328', N'LĂª Trung Phong',         N'03/07/2001', N'107328@donga.edu.vn', N'0905107328', N'IT24M'),
(23, N'109984', N'LĂª BĂ¹i HoĂ ng PhĂºc',      N'05/10/2006', N'109984@donga.edu.vn', N'0905109984', N'IT24M'),
(24, N'109443', N'LĂª Duy TĂ i',             N'23/04/2006', N'109443@donga.edu.vn', N'0905109443', N'IT24M'),
(25, N'108848', N'Lá»¥c Quang Tháº¯ng',        N'19/03/2006', N'108848@donga.edu.vn', N'0905108848', N'IT24M'),
(26, N'109802', N'LÆ°á»ng VÄƒn Thiá»‡n',        N'29/01/2006', N'109802@donga.edu.vn', N'0905109802', N'IT24M'),
(27, N'110431', N'KPÄ‚ Tráº§n Quang Thá»‹nh',   N'16/05/2006', N'110431@donga.edu.vn', N'0905110431', N'IT24M'),
(28, N'109214', N'Phan ThĂ¡i Thuáº­n',        N'02/06/2006', N'109214@donga.edu.vn', N'0905109214', N'IT24M'),
(29, N'108740', N'LĂª Minh TrĂ­',            N'27/07/2006', N'108740@donga.edu.vn', N'0905108740', N'IT24M'),
(30, N'110599', N'Tráº§n Äá»©c TrĂ­',           N'21/09/2003', N'110599@donga.edu.vn', N'0905110599', N'IT24M'),
(31, N'108815', N'Äá»— Äinh Minh Trá»ng',     N'28/03/2006', N'108815@donga.edu.vn', N'0905108815', N'IT24M'),
(32, N'109687', N'Huá»³nh ChĂ­ VÄ©',           N'30/01/2006', N'109687@donga.edu.vn', N'0905109687', N'IT24M');

-- ChĂ¨n vĂ o báº£ng students
INSERT INTO dbo.students (id, student_code, full_name, date_of_birth, email, phone, class_name, stt)
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

-- ChĂ¨n vĂ o báº£ng students_
INSERT INTO dbo.students_ (id, code, full_name, is_active)
SELECT 
    CAST(HASHBYTES('MD5', student_code) AS UNIQUEIDENTIFIER) AS id,
    student_code,
    full_name,
    1
FROM @StudentsData;

-- E. ÄÄƒng kĂ½ 32 sinh viĂªn vĂ o cĂ¡c lá»›p há»c pháº§n
-- Lá»›p Web Java
INSERT INTO dbo.course_registrations (id, course_class_id, student_id, is_active, created_at)
SELECT 
    NEWID(),
    c.id,
    s.id,
    1,
    CURRENT_TIMESTAMP
FROM dbo.courses_classes c
CROSS JOIN dbo.students s
WHERE c.code = 'IT24M-JAVA';

-- Lá»›p Há»‡ quáº£n trá»‹ CSDL
INSERT INTO dbo.course_registrations (id, course_class_id, student_id, is_active, created_at)
SELECT 
    NEWID(),
    c.id,
    s.id,
    1,
    CURRENT_TIMESTAMP
FROM dbo.courses_classes c
CROSS JOIN dbo.students s
WHERE c.code = 'IT24M-CSDL';

-- F. ChĂ¨n máº«u Ä‘iá»ƒm Ä‘Ă¡nh giĂ¡ cho sinh viĂªn
DECLARE @JavaClassId UNIQUEIDENTIFIER = 'C1000000-0000-0000-0000-000000000001';
DECLARE @CompCC UNIQUEIDENTIFIER = 'D1000000-0000-0000-0000-000000000001'; -- 10%
DECLARE @CompGK UNIQUEIDENTIFIER = 'D2000000-0000-0000-0000-000000000002'; -- 30%
DECLARE @CompCK UNIQUEIDENTIFIER = 'D3000000-0000-0000-0000-000000000003'; -- 60%

-- Äiá»ƒm máº«u cho Nguyá»…n HĂ¹ng Ăi (109745)
INSERT INTO dbo.student_component_grades (id, registration_id, component_id, score, is_retake, is_locked, is_active)
SELECT NEWID(), cr.id, @CompCC, 9.00, 0, 0, 1
FROM dbo.course_registrations cr
JOIN dbo.students s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '109745';

INSERT INTO dbo.student_component_grades (id, registration_id, component_id, score, is_retake, is_locked, is_active)
SELECT NEWID(), cr.id, @CompGK, 8.50, 0, 0, 1
FROM dbo.course_registrations cr
JOIN dbo.students s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '109745';

INSERT INTO dbo.student_component_grades (id, registration_id, component_id, score, is_retake, is_locked, is_active)
SELECT NEWID(), cr.id, @CompCK, 8.00, 0, 0, 1
FROM dbo.course_registrations cr
JOIN dbo.students s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '109745';

-- Äiá»ƒm máº«u cho KPÄ‚ Tráº§n Quang Thá»‹nh (110431)
INSERT INTO dbo.student_component_grades (id, registration_id, component_id, score, is_retake, is_locked, is_active)
SELECT NEWID(), cr.id, @CompCC, 10.00, 0, 0, 1
FROM dbo.course_registrations cr
JOIN dbo.students s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '110431';

INSERT INTO dbo.student_component_grades (id, registration_id, component_id, score, is_retake, is_locked, is_active)
SELECT NEWID(), cr.id, @CompGK, 9.00, 0, 0, 1
FROM dbo.course_registrations cr
JOIN dbo.students s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '110431';

INSERT INTO dbo.student_component_grades (id, registration_id, component_id, score, is_retake, is_locked, is_active)
SELECT NEWID(), cr.id, @CompCK, 9.50, 0, 0, 1
FROM dbo.course_registrations cr
JOIN dbo.students s ON s.id = cr.student_id
WHERE cr.course_class_id = @JavaClassId AND s.student_code = '110431';

GO

PRINT N'===> Náº P DATABASE QUANLYDIEM THĂ€NH CĂ”NG (32 SINH VIĂN IT24M, 3 Lá»P Há»ŒC PHáº¦N, Äáº¦Y Äá»¦ Cáº¤U TRĂC VĂ€ ÄIá»‚M Sá») <===';
GO
