-- ============================================================================
-- DATABASE SCRIPT: exam_management
-- Hệ thống Quản lý Thi - Khảo thí
-- Phiên bản: SQL Server 2019 / 2022 / 2025
-- ============================================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'exam_management')
BEGIN
    CREATE DATABASE exam_management COLLATE Vietnamese_CI_AS;
END
GO

USE exam_management;
GO

-- ============================================================================
-- DROP CÁC BẢNG NẾU ĐÃ TỒN TẠI (Theo thứ tự khóa ngoại để tránh xung đột)
-- ============================================================================
IF OBJECT_ID('dbo.exam_results', 'U') IS NOT NULL DROP TABLE dbo.exam_results;
IF OBJECT_ID('dbo.exam_papers', 'U') IS NOT NULL DROP TABLE dbo.exam_papers;
IF OBJECT_ID('dbo.exam_registrations', 'U') IS NOT NULL DROP TABLE dbo.exam_registrations;
IF OBJECT_ID('dbo.exam_invigilators', 'U') IS NOT NULL DROP TABLE dbo.exam_invigilators;
IF OBJECT_ID('dbo.invigilators', 'U') IS NOT NULL DROP TABLE dbo.invigilators;
IF OBJECT_ID('dbo.exam_rooms', 'U') IS NOT NULL DROP TABLE dbo.exam_rooms;
IF OBJECT_ID('dbo.exams', 'U') IS NOT NULL DROP TABLE dbo.exams;
IF OBJECT_ID('dbo.exam_types', 'U') IS NOT NULL DROP TABLE dbo.exam_types;

-- Bảng phụ phục vụ FK
IF OBJECT_ID('dbo.students', 'U') IS NOT NULL DROP TABLE dbo.students;
IF OBJECT_ID('dbo.rooms', 'U') IS NOT NULL DROP TABLE dbo.rooms;
IF OBJECT_ID('dbo.semesters', 'U') IS NOT NULL DROP TABLE dbo.semesters;
IF OBJECT_ID('dbo.courses', 'U') IS NOT NULL DROP TABLE dbo.courses;
GO

-- ============================================================================
-- BẢNG PHỤ TỐI GIẢN (Để phục vụ FOREIGN KEY)
-- ============================================================================

-- Bảng phụ: courses (Học phần / Lớp học phần)
CREATE TABLE dbo.courses (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    course_code VARCHAR(50) NOT NULL UNIQUE,
    course_name NVARCHAR(200) NOT NULL,
    credits INT NOT NULL DEFAULT 3,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    is_active BIT NOT NULL DEFAULT 1
);

-- Bảng phụ: semesters (Học kỳ)
CREATE TABLE dbo.semesters (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    semester_code VARCHAR(50) NOT NULL UNIQUE,
    semester_name NVARCHAR(100) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    is_active BIT NOT NULL DEFAULT 1
);

-- Bảng phụ: rooms (Phòng học / Phòng thi vật lý)
CREATE TABLE dbo.rooms (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    room_number NVARCHAR(50) NOT NULL UNIQUE,
    building NVARCHAR(100) NOT NULL,
    capacity INT NOT NULL DEFAULT 40,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    is_active BIT NOT NULL DEFAULT 1
);

-- Bảng phụ: students (Sinh viên)
CREATE TABLE dbo.students (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    student_code VARCHAR(20) NOT NULL UNIQUE,
    full_name NVARCHAR(150) NOT NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    class_name NVARCHAR(50) NULL,
    birth_date DATE NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    is_active BIT NOT NULL DEFAULT 1
);
GO

-- ============================================================================
-- 6 BẢNG NGHIỆP VỤ CHÍNH THEO SCHEMA CHÍNH THỨC (BÁM SÁT 100%)
-- ============================================================================

-- Bảng 1: exam_types (Loại kỳ thi)
CREATE TABLE dbo.exam_types (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    created_by UNIQUEIDENTIFIER NULL,
    updated_by UNIQUEIDENTIFIER NULL,
    deleted_at DATETIME2 NULL,
    deleted_by UNIQUEIDENTIFIER NULL,
    is_active BIT NOT NULL DEFAULT 1
);

-- Bảng 2: exams (Lịch thi)
CREATE TABLE dbo.exams (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    exam_type_id UNIQUEIDENTIFIER NOT NULL,
    course_class_id UNIQUEIDENTIFIER NOT NULL,
    semester_id UNIQUEIDENTIFIER NOT NULL,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    duration_minutes SMALLINT NOT NULL,
    end_time TIME NOT NULL,
    exam_format NVARCHAR(50) NOT NULL,
    exam_status NVARCHAR(50) NOT NULL,
    supervisor_count TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    created_by UNIQUEIDENTIFIER NULL,
    updated_by UNIQUEIDENTIFIER NULL,
    deleted_at DATETIME2 NULL,
    deleted_by UNIQUEIDENTIFIER NULL,
    is_active BIT NOT NULL DEFAULT 1,

    CONSTRAINT FK_exams_exam_types FOREIGN KEY (exam_type_id) REFERENCES dbo.exam_types(id),
    CONSTRAINT FK_exams_courses FOREIGN KEY (course_class_id) REFERENCES dbo.courses(id),
    CONSTRAINT FK_exams_semesters FOREIGN KEY (semester_id) REFERENCES dbo.semesters(id)
);

-- Bảng 3: exam_rooms (Phòng thi)
CREATE TABLE dbo.exam_rooms (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    exam_id UNIQUEIDENTIFIER NOT NULL,
    room_id UNIQUEIDENTIFIER NOT NULL,
    capacity INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    created_by UNIQUEIDENTIFIER NULL,
    updated_by UNIQUEIDENTIFIER NULL,
    deleted_at DATETIME2 NULL,
    deleted_by UNIQUEIDENTIFIER NULL,
    is_active BIT NOT NULL DEFAULT 1,

    CONSTRAINT FK_exam_rooms_exams FOREIGN KEY (exam_id) REFERENCES dbo.exams(id),
    CONSTRAINT FK_exam_rooms_rooms FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
);

-- Bảng 4: exam_registrations (Danh sách thí sinh)
CREATE TABLE dbo.exam_registrations (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    exam_id UNIQUEIDENTIFIER NOT NULL,
    exam_room_id UNIQUEIDENTIFIER NOT NULL,
    student_id UNIQUEIDENTIFIER NOT NULL,
    roll_number VARCHAR(20) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    created_by UNIQUEIDENTIFIER NULL,
    updated_by UNIQUEIDENTIFIER NULL,
    deleted_at DATETIME2 NULL,
    deleted_by UNIQUEIDENTIFIER NULL,
    is_active BIT NOT NULL DEFAULT 1,

    CONSTRAINT FK_exam_registrations_exams FOREIGN KEY (exam_id) REFERENCES dbo.exams(id),
    CONSTRAINT FK_exam_registrations_exam_rooms FOREIGN KEY (exam_room_id) REFERENCES dbo.exam_rooms(id),
    CONSTRAINT FK_exam_registrations_students FOREIGN KEY (student_id) REFERENCES dbo.students(id)
);

-- Bảng 5: exam_results (Kết quả thi)
CREATE TABLE dbo.exam_results (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    registration_id UNIQUEIDENTIFIER NOT NULL,
    score DECIMAL(4,2) NULL,
    status NVARCHAR(50) NOT NULL,
    graded_by UNIQUEIDENTIFIER NULL,
    graded_at DATETIME NULL,
    is_locked BIT NOT NULL DEFAULT 0,
    appeal_status NVARCHAR(50) NOT NULL DEFAULT N'Không',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    created_by UNIQUEIDENTIFIER NULL,
    updated_by UNIQUEIDENTIFIER NULL,
    deleted_at DATETIME2 NULL,
    deleted_by UNIQUEIDENTIFIER NULL,
    is_active BIT NOT NULL DEFAULT 1,

    CONSTRAINT FK_exam_results_registrations FOREIGN KEY (registration_id) REFERENCES dbo.exam_registrations(id)
);

-- Bảng 6: exam_papers (Đề thi)
CREATE TABLE dbo.exam_papers (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    exam_id UNIQUEIDENTIFIER NOT NULL,
    paper_code VARCHAR(20) NOT NULL,
    file_url NVARCHAR(500) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    created_by UNIQUEIDENTIFIER NULL,
    updated_by UNIQUEIDENTIFIER NULL,
    deleted_at DATETIME2 NULL,
    deleted_by UNIQUEIDENTIFIER NULL,
    is_active BIT NOT NULL DEFAULT 1,

    CONSTRAINT FK_exam_papers_exams FOREIGN KEY (exam_id) REFERENCES dbo.exams(id)
);

-- Bảng 7: invigilators (Cán bộ coi thi)
CREATE TABLE dbo.invigilators (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_invigilators_id DEFAULT NEWID() CONSTRAINT PK_invigilators PRIMARY KEY,
    staff_code NVARCHAR(30) NOT NULL,
    full_name NVARCHAR(150) NOT NULL,
    department NVARCHAR(150) NULL,
    phone NVARCHAR(20) NULL,
    is_active BIT NOT NULL CONSTRAINT DF_invigilators_active DEFAULT (1),
    created_at DATETIME2 NOT NULL CONSTRAINT DF_invigilators_created DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_invigilators_staff_code UNIQUE (staff_code)
);

-- Bảng 8: exam_invigilators (Phân công cán bộ coi thi)
CREATE TABLE dbo.exam_invigilators (
    exam_id UNIQUEIDENTIFIER NOT NULL,
    invigilator_id UNIQUEIDENTIFIER NOT NULL,
    assigned_at DATETIME2 NOT NULL CONSTRAINT DF_exam_invigilators_created DEFAULT SYSDATETIME(),
    CONSTRAINT PK_exam_invigilators PRIMARY KEY (exam_id, invigilator_id),
    CONSTRAINT FK_exam_invigilators_exam FOREIGN KEY (exam_id) REFERENCES dbo.exams(id),
    CONSTRAINT FK_exam_invigilators_staff FOREIGN KEY (invigilator_id) REFERENCES dbo.invigilators(id)
);
GO

-- ============================================================================
-- SEED DATA (DỮ LIỆU MẪU ĐẦY ĐỦ ĐỂ TEST HỆ THỐNG)
-- ============================================================================

-- Biến lưu UUID cố định để tạo quan hệ nhất quán
DECLARE @AdminUser UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';

-- 1. Bảng phụ: SEMESTERS
DECLARE @Sem1 UNIQUEIDENTIFIER = 'A1111111-0000-0000-0000-000000000001';
DECLARE @Sem2 UNIQUEIDENTIFIER = 'A1111111-0000-0000-0000-000000000002';
INSERT INTO dbo.semesters (id, semester_code, semester_name, academic_year) VALUES
(@Sem1, 'HK1-2025', N'Học kỳ 1', '2025-2026'),
(@Sem2, 'HK2-2025', N'Học kỳ 2', '2025-2026');

-- 2. Bảng phụ: COURSES
DECLARE @Course1 UNIQUEIDENTIFIER = 'B1111111-0000-0000-0000-000000000001';
DECLARE @Course2 UNIQUEIDENTIFIER = 'B1111111-0000-0000-0000-000000000002';
DECLARE @Course3 UNIQUEIDENTIFIER = 'B1111111-0000-0000-0000-000000000003';
DECLARE @Course4 UNIQUEIDENTIFIER = 'B1111111-0000-0000-0000-000000000004';
DECLARE @Course5 UNIQUEIDENTIFIER = 'B1111111-0000-0000-0000-000000000005';
INSERT INTO dbo.courses (id, course_code, course_name, credits) VALUES
(@Course1, 'INT1306', N'Lập trình Web nâng cao', 3),
(@Course2, 'INT1202', N'Cơ sở dữ liệu & SQL', 3),
(@Course3, 'INT1103', N'Cấu trúc dữ liệu & Giải thuật', 4),
(@Course4, 'INT1340', N'Mạng máy tính & Truyền thông', 3),
(@Course5, 'INT1405', N'An toàn và Bảo mật thông tin', 3);

-- 3. Bảng phụ: ROOMS (3 phòng theo yêu cầu)
DECLARE @Room1 UNIQUEIDENTIFIER = 'C1111111-0000-0000-0000-000000000001';
DECLARE @Room2 UNIQUEIDENTIFIER = 'C1111111-0000-0000-0000-000000000002';
DECLARE @Room3 UNIQUEIDENTIFIER = 'C1111111-0000-0000-0000-000000000003';
INSERT INTO dbo.rooms (id, room_number, building, capacity) VALUES
(@Room1, 'P.301-A1', N'Tòa nhà Giảng đường A1', 40),
(@Room2, 'P.402-B2', N'Tòa nhà B2 (Phòng Máy tính)', 35),
(@Room3, N'Hội trường C1', N'Nhà văn hóa sinh viên C1', 80);

-- 4. Bảng phụ: STUDENTS (10 sinh viên theo yêu cầu)
DECLARE @St1 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000001';
DECLARE @St2 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000002';
DECLARE @St3 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000003';
DECLARE @St4 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000004';
DECLARE @St5 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000005';
DECLARE @St6 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000006';
DECLARE @St7 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000007';
DECLARE @St8 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000008';
DECLARE @St9 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000009';
DECLARE @St10 UNIQUEIDENTIFIER = 'D1111111-0000-0000-0000-000000000010';

INSERT INTO dbo.students (id, student_code, full_name, email, phone, class_name) VALUES
(@St1, 'B21DCCN001', N'Nguyễn Văn An', 'an.nv@student.edu.vn', '0901234567', N'D21CQCN01-B'),
(@St2, 'B21DCCN002', N'Trần Thị Bích', 'bich.tt@student.edu.vn', '0912345678', N'D21CQCN01-B'),
(@St3, 'B21DCCN003', N'Lê Hoàng Cường', 'cuong.lh@student.edu.vn', '0923456789', N'D21CQCN02-B'),
(@St4, 'B21DCCN004', N'Phạm Minh Dũng', 'dung.pm@student.edu.vn', '0934567890', N'D21CQCN02-B'),
(@St5, 'B21DCCN005', N'Hoàng Thùy Linh', 'linh.ht@student.edu.vn', '0945678901', N'D21CQCN03-B'),
(@St6, 'B21DCAT006', N'Đỗ Tuấn Kiệt', 'kiet.dt@student.edu.vn', '0956789012', N'D21CQAT01-B'),
(@St7, 'B21DCAT007', N'Vũ Phương Mai', 'mai.vp@student.edu.vn', '0967890123', N'D21CQAT01-B'),
(@St8, 'B21DCMR008', N'Bùi Đức Nam', 'nam.bd@student.edu.vn', '0978901234', N'D21CQMR01-B'),
(@St9, 'B21DCMR009', N'Ngô Lan Phương', 'phuong.nl@student.edu.vn', '0989012345', N'D21CQMR01-B'),
(@St10, 'B21DCHT010', N'Đinh Quang Thắng', 'thang.dq@student.edu.vn', '0990123456', N'D21CQHT01-B');

-- ============================================================================
-- 5. BẢNG 1: exam_types (3 loại kỳ thi)
-- ============================================================================
DECLARE @TypeMid UNIQUEIDENTIFIER = 'E1111111-0000-0000-0000-000000000001';
DECLARE @TypeFinal UNIQUEIDENTIFIER = 'E1111111-0000-0000-0000-000000000002';
DECLARE @TypeRetake UNIQUEIDENTIFIER = 'E1111111-0000-0000-0000-000000000003';

INSERT INTO dbo.exam_types (id, name, description, created_by, is_active) VALUES
(@TypeMid, N'Thi giữa kỳ', N'Kỳ thi đánh giá giữa kỳ các học phần chuyên môn', @AdminUser, 1),
(@TypeFinal, N'Thi cuối kỳ', N'Kỳ thi chính thức kết thúc học phần', @AdminUser, 1),
(@TypeRetake, N'Thi lại / Cải thiện', N'Kỳ thi dành cho sinh viên thi lại hoặc có nhu cầu nâng điểm', @AdminUser, 1);

-- ============================================================================
-- 6. BẢNG 2: exams (5 kỳ thi theo yêu cầu)
-- ============================================================================
DECLARE @Exam1 UNIQUEIDENTIFIER = 'F1111111-0000-0000-0000-000000000001';
DECLARE @Exam2 UNIQUEIDENTIFIER = 'F1111111-0000-0000-0000-000000000002';
DECLARE @Exam3 UNIQUEIDENTIFIER = 'F1111111-0000-0000-0000-000000000003';
DECLARE @Exam4 UNIQUEIDENTIFIER = 'F1111111-0000-0000-0000-000000000004';
DECLARE @Exam5 UNIQUEIDENTIFIER = 'F1111111-0000-0000-0000-000000000005';

INSERT INTO dbo.exams (id, exam_type_id, course_class_id, semester_id, exam_date, start_time, duration_minutes, end_time, exam_format, exam_status, supervisor_count, created_by, is_active) VALUES
(@Exam1, @TypeFinal, @Course1, @Sem1, '2026-11-10', '08:00:00', 90, '09:30:00', N'thi máy', N'Đã lên lịch', 2, @AdminUser, 1),
(@Exam2, @TypeFinal, @Course2, @Sem1, '2026-11-12', '13:30:00', 90, '15:00:00', N'trắc nghiệm', N'Đã lên lịch', 2, @AdminUser, 1),
(@Exam3, @TypeMid, @Course3, @Sem1, '2026-11-15', '07:30:00', 60, '08:30:00', N'thực hành', N'Đang diễn ra', 1, @AdminUser, 1),
(@Exam4, @TypeFinal, @Course4, @Sem1, '2026-11-18', '09:45:00', 90, '11:15:00', N'vấn đáp', N'Đã lên lịch', 2, @AdminUser, 1),
(@Exam5, @TypeRetake, @Course5, @Sem2, '2026-11-20', '14:00:00', 90, '15:30:00', N'thi máy', N'Kết thúc', 2, @AdminUser, 1);

-- ============================================================================
-- 7. BẢNG 3: exam_rooms (Gán phòng thi)
-- ============================================================================
DECLARE @ExamRoom1 UNIQUEIDENTIFIER = '01111111-0000-0000-0000-000000000001';
DECLARE @ExamRoom2 UNIQUEIDENTIFIER = '01111111-0000-0000-0000-000000000002';
DECLARE @ExamRoom3 UNIQUEIDENTIFIER = '01111111-0000-0000-0000-000000000003';
DECLARE @ExamRoom4 UNIQUEIDENTIFIER = '01111111-0000-0000-0000-000000000004';
DECLARE @ExamRoom5 UNIQUEIDENTIFIER = '01111111-0000-0000-0000-000000000005';

INSERT INTO dbo.exam_rooms (id, exam_id, room_id, capacity, created_by, is_active) VALUES
(@ExamRoom1, @Exam1, @Room2, 35, @AdminUser, 1), -- Exam 1 tại phòng máy P.402-B2
(@ExamRoom2, @Exam2, @Room1, 40, @AdminUser, 1), -- Exam 2 tại P.301-A1
(@ExamRoom3, @Exam3, @Room2, 35, @AdminUser, 1), -- Exam 3 tại P.402-B2
(@ExamRoom4, @Exam4, @Room1, 40, @AdminUser, 1), -- Exam 4 tại P.301-A1
(@ExamRoom5, @Exam5, @Room3, 80, @AdminUser, 1); -- Exam 5 tại Hội trường C1

-- ============================================================================
-- 8. BẢNG 4: exam_registrations (Danh sách thí sinh)
-- ============================================================================
DECLARE @Reg1 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000001';
DECLARE @Reg2 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000002';
DECLARE @Reg3 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000003';
DECLARE @Reg4 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000004';
DECLARE @Reg5 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000005';
DECLARE @Reg6 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000006';
DECLARE @Reg7 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000007';
DECLARE @Reg8 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000008';
DECLARE @Reg9 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000009';
DECLARE @Reg10 UNIQUEIDENTIFIER = '12111111-0000-0000-0000-000000000010';

INSERT INTO dbo.exam_registrations (id, exam_id, exam_room_id, student_id, roll_number, created_by, is_active) VALUES
(@Reg1, @Exam1, @ExamRoom1, @St1, 'SBD-WEB-001', @AdminUser, 1),
(@Reg2, @Exam1, @ExamRoom1, @St2, 'SBD-WEB-002', @AdminUser, 1),
(@Reg3, @Exam1, @ExamRoom1, @St3, 'SBD-WEB-003', @AdminUser, 1),
(@Reg4, @Exam1, @ExamRoom1, @St4, 'SBD-WEB-004', @AdminUser, 1),
(@Reg5, @Exam2, @ExamRoom2, @St1, 'SBD-SQL-001', @AdminUser, 1),
(@Reg6, @Exam2, @ExamRoom2, @St5, 'SBD-SQL-002', @AdminUser, 1),
(@Reg7, @Exam2, @ExamRoom2, @St6, 'SBD-SQL-003', @AdminUser, 1),
(@Reg8, @Exam3, @ExamRoom3, @St7, 'SBD-DSA-001', @AdminUser, 1),
(@Reg9, @Exam4, @ExamRoom4, @St8, 'SBD-NET-001', @AdminUser, 1),
(@Reg10, @Exam5, @ExamRoom5, @St10, 'SBD-SEC-001', @AdminUser, 1);

-- ============================================================================
-- 9. BẢNG 5: exam_results (Kết quả thi - có cả điểm chốt và chưa chốt, phúc khảo)
-- ============================================================================
DECLARE @Res1 UNIQUEIDENTIFIER = '22111111-0000-0000-0000-000000000001';
DECLARE @Res2 UNIQUEIDENTIFIER = '22111111-0000-0000-0000-000000000002';
DECLARE @Res3 UNIQUEIDENTIFIER = '22111111-0000-0000-0000-000000000003';
DECLARE @Res4 UNIQUEIDENTIFIER = '22111111-0000-0000-0000-000000000004';
DECLARE @Res5 UNIQUEIDENTIFIER = '22111111-0000-0000-0000-000000000005';
DECLARE @Res6 UNIQUEIDENTIFIER = '22111111-0000-0000-0000-000000000006';

INSERT INTO dbo.exam_results (id, registration_id, score, status, graded_by, graded_at, is_locked, appeal_status, created_by, is_active) VALUES
(@Res1, @Reg1, 8.50, N'Đạt', @AdminUser, '2026-10-25 10:00:00', 0, N'Không', @AdminUser, 1),
(@Res2, @Reg2, 9.00, N'Đạt', @AdminUser, '2026-10-25 10:15:00', 1, N'Không', @AdminUser, 1), -- Đã chốt điểm (is_locked = 1)
(@Res3, @Reg3, 3.50, N'Không đạt', @AdminUser, '2026-10-25 10:30:00', 0, N'Chờ xử lý', @AdminUser, 1), -- Đang phúc khảo
(@Res4, @Reg4, NULL, N'Vắng', @AdminUser, '2026-10-25 10:45:00', 1, N'Không', @AdminUser, 1), -- Đã chốt vắng
(@Res5, @Reg5, 7.75, N'Đạt', @AdminUser, '2026-10-26 14:00:00', 0, N'Không', @AdminUser, 1),
(@Res6, @Reg10, 8.00, N'Đạt', @AdminUser, '2026-10-27 16:30:00', 1, N'Đã duyệt', @AdminUser, 1);

-- ============================================================================
-- 10. BẢNG 6: exam_papers (Đề thi)
-- ============================================================================
DECLARE @Paper1 UNIQUEIDENTIFIER = '33111111-0000-0000-0000-000000000001';
DECLARE @Paper2 UNIQUEIDENTIFIER = '33111111-0000-0000-0000-000000000002';
DECLARE @Paper3 UNIQUEIDENTIFIER = '33111111-0000-0000-0000-000000000003';
DECLARE @Paper4 UNIQUEIDENTIFIER = '33111111-0000-0000-0000-000000000004';

INSERT INTO dbo.exam_papers (id, exam_id, paper_code, file_url, created_by, is_active) VALUES
(@Paper1, @Exam1, 'DE-WEB-01', '/uploads/papers/DE-WEB-01.pdf', @AdminUser, 1),
(@Paper2, @Exam1, 'DE-WEB-02', '/uploads/papers/DE-WEB-02.pdf', @AdminUser, 1),
(@Paper3, @Exam2, 'DE-SQL-101', '/uploads/papers/DE-SQL-101.pdf', @AdminUser, 1),
(@Paper4, @Exam3, 'DE-DSA-A', '/uploads/papers/DE-DSA-A.pdf', @AdminUser, 1);
GO

-- ============================================================================
-- HOÀN TẤT VÀ KIỂM TRA TỔNG SỐ BẢNG
-- ============================================================================
SELECT 
    t.name AS TableName,
    COUNT(c.name) AS ColumnCount
FROM sys.tables t
INNER JOIN sys.columns c ON t.object_id = c.object_id
GROUP BY t.name
ORDER BY t.name;
GO
