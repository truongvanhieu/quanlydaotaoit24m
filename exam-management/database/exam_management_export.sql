-- ============================================================================
-- EXPORTED DATABASE: exam_management (Schema + Full Live Data)
-- Exported at: 2026-11-28 14:12:45
-- ============================================================================

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
-- ============================================================================
-- LIVE DATA EXPORT FROM SQL SERVER (.\SQLEXPRESS)
-- ============================================================================

-- Table: dbo.semesters (2 rows)
INSERT INTO dbo.[semesters] ([id], [semester_code], [semester_name], [academic_year], [created_at], [is_active]) VALUES ('a1111111-0000-0000-0000-000000000001', N'HK1-2025', N'Học kỳ 1', N'2025-2026', '2026-11-27 13:45:28.138', 1);
INSERT INTO dbo.[semesters] ([id], [semester_code], [semester_name], [academic_year], [created_at], [is_active]) VALUES ('a1111111-0000-0000-0000-000000000002', N'HK2-2025', N'Học kỳ 2', N'2025-2026', '2026-11-27 13:45:28.138', 1);
GO

-- Table: dbo.courses (5 rows)
INSERT INTO dbo.[courses] ([id], [course_code], [course_name], [credits], [created_at], [is_active]) VALUES ('b1111111-0000-0000-0000-000000000001', N'INT1306', N'Lập trình Web nâng cao', 3, '2026-11-27 13:45:28.139', 1);
INSERT INTO dbo.[courses] ([id], [course_code], [course_name], [credits], [created_at], [is_active]) VALUES ('b1111111-0000-0000-0000-000000000002', N'INT1202', N'Cơ sở dữ liệu & SQL', 3, '2026-11-27 13:45:28.139', 1);
INSERT INTO dbo.[courses] ([id], [course_code], [course_name], [credits], [created_at], [is_active]) VALUES ('b1111111-0000-0000-0000-000000000003', N'INT1103', N'Cấu trúc dữ liệu & Giải thuật', 4, '2026-11-27 13:45:28.139', 1);
INSERT INTO dbo.[courses] ([id], [course_code], [course_name], [credits], [created_at], [is_active]) VALUES ('b1111111-0000-0000-0000-000000000004', N'INT1340', N'Mạng máy tính & Truyền thông', 3, '2026-11-27 13:45:28.139', 1);
INSERT INTO dbo.[courses] ([id], [course_code], [course_name], [credits], [created_at], [is_active]) VALUES ('b1111111-0000-0000-0000-000000000005', N'INT1405', N'An toàn và Bảo mật thông tin', 3, '2026-11-27 13:45:28.139', 1);
GO

-- Table: dbo.rooms (4 rows)
INSERT INTO dbo.[rooms] ([id], [room_number], [building], [capacity], [created_at], [is_active]) VALUES ('c1111111-0000-0000-0000-000000000001', N'P.301-A1', N'Tòa nhà Giảng đường A1', 40, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[rooms] ([id], [room_number], [building], [capacity], [created_at], [is_active]) VALUES ('c1111111-0000-0000-0000-000000000002', N'P.402-B2', N'Tòa nhà B2 (Phòng Máy tính)', 35, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[rooms] ([id], [room_number], [building], [capacity], [created_at], [is_active]) VALUES ('c1111111-0000-0000-0000-000000000003', N'Hội trường C1', N'Nhà văn hóa sinh viên C1', 80, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[rooms] ([id], [room_number], [building], [capacity], [created_at], [is_active]) VALUES ('dacfa226-c744-46c3-a245-aa7a45d8465f', N'P.505-A2', N'Tòa nhà A2', 45, '2026-11-27 14:36:54.518', 1);
GO

-- Table: dbo.students (10 rows)
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000001', N'B21DCCN001', N'Nguyễn Văn An', N'an.nv@student.edu.vn', N'0901234567', N'D21CQCN01-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000002', N'B21DCCN002', N'Trần Thị Bích', N'bich.tt@student.edu.vn', N'0912345678', N'D21CQCN01-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000003', N'B21DCCN003', N'Lê Hoàng Cường', N'cuong.lh@student.edu.vn', N'0923456789', N'D21CQCN02-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000004', N'B21DCCN004', N'Phạm Minh Dũng', N'dung.pm@student.edu.vn', N'0934567890', N'D21CQCN02-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000005', N'B21DCCN005', N'Hoàng Thùy Linh', N'linh.ht@student.edu.vn', N'0945678901', N'D21CQCN03-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000006', N'B21DCAT006', N'Đỗ Tuấn Kiệt', N'kiet.dt@student.edu.vn', N'0956789012', N'D21CQAT01-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000007', N'B21DCAT007', N'Vũ Phương Mai', N'mai.vp@student.edu.vn', N'0967890123', N'D21CQAT01-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000008', N'B21DCMR008', N'Bùi Đức Nam', N'nam.bd@student.edu.vn', N'0978901234', N'D21CQMR01-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000009', N'B21DCMR009', N'Ngô Lan Phương', N'phuong.nl@student.edu.vn', N'0989012345', N'D21CQMR01-B', NULL, '2026-11-27 13:45:28.140', 1);
INSERT INTO dbo.[students] ([id], [student_code], [full_name], [email], [phone], [class_name], [birth_date], [created_at], [is_active]) VALUES ('d1111111-0000-0000-0000-000000000010', N'B21DCHT010', N'Đinh Quang Thắng', N'thang.dq@student.edu.vn', N'0990123456', N'D21CQHT01-B', NULL, '2026-11-27 13:45:28.140', 1);
GO

-- Table: dbo.exam_types (5 rows)
INSERT INTO dbo.[exam_types] ([id], [name], [description], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('e1111111-0000-0000-0000-000000000001', N'Thi giữa kỳ', N'Kỳ thi đánh giá giữa kỳ các học phần chuyên môn', '2026-11-27 13:45:28.141', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_types] ([id], [name], [description], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('e1111111-0000-0000-0000-000000000002', N'Thi cuối kỳ', N'Kỳ thi chính thức kết thúc học phần', '2026-11-27 13:45:28.141', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_types] ([id], [name], [description], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('e1111111-0000-0000-0000-000000000003', N'Thi lại / Cải thiện', N'Kỳ thi dành cho sinh viên thi lại hoặc có nhu cầu nâng điểm', '2026-11-27 13:45:28.141', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_types] ([id], [name], [description], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('03bff539-a273-4951-9694-0bd73ddac0d6', N'Thi đánh giá năng lực', N'Kiểm tra đánh giá đầu ra - Đã sửa', '2026-11-27 14:35:26.073', '2026-11-27 14:35:28.680', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', '2026-11-27 14:35:29.630', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exam_types] ([id], [name], [description], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('c13cd43e-752a-4323-b5f5-697bcc9e32d3', N'Thi thực hành tổng hợp (E2E)', N'Test luồng 1-7', '2026-11-27 14:43:46.896', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 14:47:54.936', '11111111-1111-1111-1111-111111111111', 0);
GO

-- Table: dbo.exams (10 rows)
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('f1111111-0000-0000-0000-000000000001', 'e1111111-0000-0000-0000-000000000002', 'b1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000001', '2026-11-10 00:00:00.000', '08:00:00', 90, '09:30:00', N'thi máy', N'Kết thúc', 2, '2026-11-27 13:45:28.141', '2026-11-27 14:23:04.446', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NULL, NULL, 1);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('f1111111-0000-0000-0000-000000000002', 'e1111111-0000-0000-0000-000000000002', 'b1111111-0000-0000-0000-000000000002', 'a1111111-0000-0000-0000-000000000001', '2026-11-12 00:00:00.000', '13:30:00', 90, '15:00:00', N'trắc nghiệm', N'Kết thúc', 2, '2026-11-27 13:45:28.141', '2026-11-27 14:22:59.327', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NULL, NULL, 1);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('f1111111-0000-0000-0000-000000000003', 'e1111111-0000-0000-0000-000000000001', 'b1111111-0000-0000-0000-000000000003', 'a1111111-0000-0000-0000-000000000001', '2026-11-15 00:00:00.000', '07:30:00', 60, '08:30:00', N'thực hành', N'Kết thúc', 1, '2026-11-27 13:45:28.141', '2026-11-27 14:22:53.658', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NULL, NULL, 1);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('f1111111-0000-0000-0000-000000000004', 'e1111111-0000-0000-0000-000000000002', 'b1111111-0000-0000-0000-000000000004', 'a1111111-0000-0000-0000-000000000001', '2026-11-18 00:00:00.000', '09:45:00', 90, '11:15:00', N'vấn đáp', N'Kết thúc', 2, '2026-11-27 13:45:28.141', '2026-11-27 14:23:08.859', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NULL, NULL, 1);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('f1111111-0000-0000-0000-000000000005', 'e1111111-0000-0000-0000-000000000003', 'b1111111-0000-0000-0000-000000000005', 'a1111111-0000-0000-0000-000000000002', '2026-11-20 00:00:00.000', '14:00:00', 90, '15:30:00', N'thi máy', N'Kết thúc', 2, '2026-11-27 13:45:28.141', '2026-11-27 14:36:28.446', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NULL, NULL, 1);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('78802c5a-8212-4296-9b20-0f5a1f6a4590', 'c13cd43e-752a-4323-b5f5-697bcc9e32d3', 'b1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000002', '2026-12-15 00:00:00.000', '08:00:00', 90, '09:30:00', N'thi máy', N'Đã lên lịch', 1, '2026-11-27 14:44:27.739', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 14:47:53.167', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('850ac8af-c273-4d94-a366-21f6b40cfca8', 'e1111111-0000-0000-0000-000000000002', 'b1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000002', '2026-12-21 00:00:00.000', '08:00:00', 90, '09:30:00', N'thi máy', N'Đã lên lịch', 2, '2026-11-27 16:06:45.811', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 16:08:27.392', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('7fb96fdd-06eb-4a33-9b35-5cabfe9de222', 'e1111111-0000-0000-0000-000000000002', 'b1111111-0000-0000-0000-000000000002', 'a1111111-0000-0000-0000-000000000002', '2026-12-20 00:00:00.000', '08:00:00', 90, '09:30:00', N'thi máy', N'Đã lên lịch', 2, '2026-11-27 15:36:40.522', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 15:37:46.558', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('50d83964-10bb-4c1d-bea8-d3fe623e72f9', 'e1111111-0000-0000-0000-000000000002', 'b1111111-0000-0000-0000-000000000002', 'a1111111-0000-0000-0000-000000000002', '2026-12-21 00:00:00.000', '08:00:00', 90, '09:30:00', N'thi máy', N'Đã lên lịch', 2, '2026-11-27 16:06:54.796', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 16:08:28.021', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exams] ([id], [exam_type_id], [course_class_id], [semester_id], [exam_date], [start_time], [duration_minutes], [end_time], [exam_format], [exam_status], [supervisor_count], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('3b8294aa-2571-47c3-bb70-f7c9ad84e4ee', 'e1111111-0000-0000-0000-000000000002', 'b1111111-0000-0000-0000-000000000001', 'a1111111-0000-0000-0000-000000000002', '2026-12-20 00:00:00.000', '08:00:00', 90, '09:30:00', N'thi máy', N'Đã lên lịch', 2, '2026-11-27 15:36:34.565', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 15:37:47.613', '11111111-1111-1111-1111-111111111111', 0);
GO

-- Table: dbo.exam_rooms (11 rows)
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('01111111-0000-0000-0000-000000000001', 'f1111111-0000-0000-0000-000000000001', 'c1111111-0000-0000-0000-000000000002', 35, '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('01111111-0000-0000-0000-000000000002', 'f1111111-0000-0000-0000-000000000002', 'c1111111-0000-0000-0000-000000000001', 40, '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('01111111-0000-0000-0000-000000000003', 'f1111111-0000-0000-0000-000000000003', 'c1111111-0000-0000-0000-000000000002', 35, '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('01111111-0000-0000-0000-000000000004', 'f1111111-0000-0000-0000-000000000004', 'c1111111-0000-0000-0000-000000000001', 40, '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('01111111-0000-0000-0000-000000000005', 'f1111111-0000-0000-0000-000000000005', 'c1111111-0000-0000-0000-000000000003', 80, '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('d368590b-0d16-45de-8e8d-281764cb1b68', '7fb96fdd-06eb-4a33-9b35-5cabfe9de222', 'c1111111-0000-0000-0000-000000000001', 40, '2026-11-27 15:37:22.525', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 15:37:44.542', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('ab40123c-b2b2-4133-ba46-42c313c76d15', '50d83964-10bb-4c1d-bea8-d3fe623e72f9', 'c1111111-0000-0000-0000-000000000002', 35, '2026-11-27 16:07:18.505', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 16:08:24.938', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('c30bba09-0947-4e53-87f9-456a7eda8f86', '3b8294aa-2571-47c3-bb70-f7c9ad84e4ee', 'c1111111-0000-0000-0000-000000000001', 40, '2026-11-27 15:37:25.506', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 15:37:43.564', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('a89e365e-4d2a-4d88-91da-668e79007271', '78802c5a-8212-4296-9b20-0f5a1f6a4590', 'c1111111-0000-0000-0000-000000000001', 40, '2026-11-27 14:44:48.406', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 14:47:51.262', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('476b270c-8462-4d29-806a-7d294d175185', 'f1111111-0000-0000-0000-000000000005', 'dacfa226-c744-46c3-a245-aa7a45d8465f', 45, '2026-11-27 14:36:54.893', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_rooms] ([id], [exam_id], [room_id], [capacity], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('a4ea42d0-03d5-4500-b151-8cb891afb4e1', '850ac8af-c273-4d94-a366-21f6b40cfca8', 'c1111111-0000-0000-0000-000000000001', 40, '2026-11-27 16:07:12.453', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 16:08:25.480', '11111111-1111-1111-1111-111111111111', 0);
GO

-- Table: dbo.exam_registrations (16 rows)
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000001', 'f1111111-0000-0000-0000-000000000001', '01111111-0000-0000-0000-000000000001', 'd1111111-0000-0000-0000-000000000001', N'SBD-WEB-001', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000002', 'f1111111-0000-0000-0000-000000000001', '01111111-0000-0000-0000-000000000001', 'd1111111-0000-0000-0000-000000000002', N'SBD-WEB-002', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000003', 'f1111111-0000-0000-0000-000000000001', '01111111-0000-0000-0000-000000000001', 'd1111111-0000-0000-0000-000000000003', N'SBD-WEB-003', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000004', 'f1111111-0000-0000-0000-000000000001', '01111111-0000-0000-0000-000000000001', 'd1111111-0000-0000-0000-000000000004', N'SBD-WEB-004', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000005', 'f1111111-0000-0000-0000-000000000002', '01111111-0000-0000-0000-000000000002', 'd1111111-0000-0000-0000-000000000001', N'SBD-SQL-001', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000006', 'f1111111-0000-0000-0000-000000000002', '01111111-0000-0000-0000-000000000002', 'd1111111-0000-0000-0000-000000000005', N'SBD-SQL-002', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000007', 'f1111111-0000-0000-0000-000000000002', '01111111-0000-0000-0000-000000000002', 'd1111111-0000-0000-0000-000000000006', N'SBD-SQL-003', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000008', 'f1111111-0000-0000-0000-000000000003', '01111111-0000-0000-0000-000000000003', 'd1111111-0000-0000-0000-000000000007', N'SBD-DSA-001', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000009', 'f1111111-0000-0000-0000-000000000004', '01111111-0000-0000-0000-000000000004', 'd1111111-0000-0000-0000-000000000008', N'SBD-NET-001', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('12111111-0000-0000-0000-000000000010', 'f1111111-0000-0000-0000-000000000005', '01111111-0000-0000-0000-000000000005', 'd1111111-0000-0000-0000-000000000010', N'SBD-SEC-001', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('495aa2b3-e11d-40a7-a057-16226d7a2c36', '50d83964-10bb-4c1d-bea8-d3fe623e72f9', 'ab40123c-b2b2-4133-ba46-42c313c76d15', 'd1111111-0000-0000-0000-000000000001', N'SBD-CONFLICT-02', '2026-11-27 16:07:37.124', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 16:08:23.241', '11111111-1111-1111-1111-111111111111', 0, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('33e86743-65e0-4cb9-9802-250d16bb1c7c', '78802c5a-8212-4296-9b20-0f5a1f6a4590', 'a89e365e-4d2a-4d88-91da-668e79007271', 'd1111111-0000-0000-0000-000000000001', N'SBD-E2E-001', '2026-11-27 14:45:07.404', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 14:47:16.345', '11111111-1111-1111-1111-111111111111', 0, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('9474a0fb-0a09-4897-b1b4-80afa8134d9e', 'f1111111-0000-0000-0000-000000000005', '01111111-0000-0000-0000-000000000005', 'd1111111-0000-0000-0000-000000000009', N'SBD-INT1405-003', '2026-11-27 14:37:45.004', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, 'ac1621f2-9faa-46b0-9c7c-34fb61f75063');
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('1ea28fd9-f0d8-4705-94f7-ae49af1ff25e', 'f1111111-0000-0000-0000-000000000005', '01111111-0000-0000-0000-000000000005', 'd1111111-0000-0000-0000-000000000002', N'SBD-INT1405-002', '2026-11-27 14:37:45.004', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, 'ac1621f2-9faa-46b0-9c7c-34fb61f75063');
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('be25cd22-007e-4d5d-8593-afefb068a7eb', '850ac8af-c273-4d94-a366-21f6b40cfca8', 'a4ea42d0-03d5-4500-b151-8cb891afb4e1', 'd1111111-0000-0000-0000-000000000001', N'SBD-CONFLICT-01', '2026-11-27 16:07:35.091', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 16:08:23.756', '11111111-1111-1111-1111-111111111111', 0, NULL);
INSERT INTO dbo.[exam_registrations] ([id], [exam_id], [exam_room_id], [student_id], [roll_number], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active], [merged_group_id]) VALUES ('05f43a5f-70d5-4e44-b4b9-c9dc518eff37', 'f1111111-0000-0000-0000-000000000005', '01111111-0000-0000-0000-000000000005', 'd1111111-0000-0000-0000-000000000006', N'SBD-INT1405-001', '2026-11-27 14:37:45.003', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1, 'ac1621f2-9faa-46b0-9c7c-34fb61f75063');
GO

-- Table: dbo.exam_results (8 rows)
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('22111111-0000-0000-0000-000000000001', '12111111-0000-0000-0000-000000000001', 8.50, N'Đạt', '11111111-1111-1111-1111-111111111111', '2026-10-25 10:00:00.000', 0, N'Không', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('22111111-0000-0000-0000-000000000002', '12111111-0000-0000-0000-000000000002', 9.00, N'Đạt', '11111111-1111-1111-1111-111111111111', '2026-10-25 10:15:00.000', 1, N'Không', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('22111111-0000-0000-0000-000000000003', '12111111-0000-0000-0000-000000000003', 3.50, N'Không đạt', '11111111-1111-1111-1111-111111111111', '2026-10-25 10:30:00.000', 0, N'Chờ xử lý', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('22111111-0000-0000-0000-000000000004', '12111111-0000-0000-0000-000000000004', NULL, N'Vắng', '11111111-1111-1111-1111-111111111111', '2026-10-25 10:45:00.000', 1, N'Không', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('22111111-0000-0000-0000-000000000005', '12111111-0000-0000-0000-000000000005', 7.75, N'Đạt', '11111111-1111-1111-1111-111111111111', '2026-10-26 14:00:00.000', 0, N'Không', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('22111111-0000-0000-0000-000000000006', '12111111-0000-0000-0000-000000000010', 8.00, N'Đạt', '11111111-1111-1111-1111-111111111111', '2026-10-27 16:30:00.000', 1, N'Đã duyệt', '2026-11-27 13:45:28.142', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('985929d6-5910-4bfa-bc0d-f48676e01d64', '33e86743-65e0-4cb9-9802-250d16bb1c7c', 9.25, N'Đạt', '11111111-1111-1111-1111-111111111111', '2026-11-27 14:46:21.187', 0, N'Không', '2026-11-27 14:46:21.186', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 14:47:12.434', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exam_results] ([id], [registration_id], [score], [status], [graded_by], [graded_at], [is_locked], [appeal_status], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('4fb2f0c8-464a-4964-91c0-ff44add89b7c', '12111111-0000-0000-0000-000000000006', 7.50, N'Đạt', '11111111-1111-1111-1111-111111111111', '2026-11-27 14:38:30.547', 0, N'Chờ xử lý', '2026-11-27 14:38:30.547', '2026-11-27 14:38:44.151', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NULL, NULL, 1);
GO

-- Table: dbo.exam_papers (7 rows)
INSERT INTO dbo.[exam_papers] ([id], [exam_id], [paper_code], [file_url], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('33111111-0000-0000-0000-000000000001', 'f1111111-0000-0000-0000-000000000001', N'DE-WEB-01', N'/uploads/papers/DE-WEB-01.pdf', '2026-11-27 13:45:28.143', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_papers] ([id], [exam_id], [paper_code], [file_url], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('33111111-0000-0000-0000-000000000002', 'f1111111-0000-0000-0000-000000000001', N'DE-WEB-02', N'/uploads/papers/DE-WEB-02.pdf', '2026-11-27 13:45:28.143', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_papers] ([id], [exam_id], [paper_code], [file_url], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('33111111-0000-0000-0000-000000000003', 'f1111111-0000-0000-0000-000000000002', N'DE-SQL-101', N'/uploads/papers/DE-SQL-101.pdf', '2026-11-27 13:45:28.143', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_papers] ([id], [exam_id], [paper_code], [file_url], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('33111111-0000-0000-0000-000000000004', 'f1111111-0000-0000-0000-000000000003', N'DE-DSA-A', N'/uploads/papers/DE-DSA-A.pdf', '2026-11-27 13:45:28.143', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_papers] ([id], [exam_id], [paper_code], [file_url], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('e6e0d68c-2acb-4b3b-9f00-2558958b5e57', 'f1111111-0000-0000-0000-000000000004', N'DE-NET-01', N'/uploads/papers/DE-NET-01.pdf', '2026-11-27 14:38:59.905', NULL, '11111111-1111-1111-1111-111111111111', NULL, NULL, NULL, 1);
INSERT INTO dbo.[exam_papers] ([id], [exam_id], [paper_code], [file_url], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('568da316-ab4a-427d-85c7-b1c04bb474cd', '78802c5a-8212-4296-9b20-0f5a1f6a4590', N'DE-E2E-01', N'/uploads/papers/DE-E2E-01.pdf', '2026-11-27 14:46:34.984', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 14:47:09.162', '11111111-1111-1111-1111-111111111111', 0);
INSERT INTO dbo.[exam_papers] ([id], [exam_id], [paper_code], [file_url], [created_at], [updated_at], [created_by], [updated_by], [deleted_at], [deleted_by], [is_active]) VALUES ('3e888940-553d-4e92-8028-c795770b5ecb', 'f1111111-0000-0000-0000-000000000001', N'DE-URL-TEST', N'javascript:void(0)', '2026-11-27 16:08:47.314', NULL, '11111111-1111-1111-1111-111111111111', NULL, '2026-11-27 16:08:49.115', '11111111-1111-1111-1111-111111111111', 0);
GO

-- Table: dbo.invigilators (1 rows)
INSERT INTO dbo.[invigilators] ([id], [staff_code], [full_name], [department], [phone], [is_active], [created_at]) VALUES ('77f27afb-e486-4cf6-b473-4943e73e2de8', N'CB001', N'TS. Nguyễn Minh Quân', N'Khoa CNTT', N'0911222333', 1, '2026-11-27 14:36:27.647');
GO

-- Table: dbo.exam_invigilators (1 rows)
INSERT INTO dbo.[exam_invigilators] ([exam_id], [invigilator_id], [assigned_at]) VALUES ('f1111111-0000-0000-0000-000000000005', '77f27afb-e486-4cf6-b473-4943e73e2de8', '2026-11-27 14:36:28.475');
GO

