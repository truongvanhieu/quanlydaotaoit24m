USE [it24m];
GO

-- 1. Backup old students table if exists and not yet backed up
IF OBJECT_ID('dbo.students', 'U') IS NOT NULL AND OBJECT_ID('dbo.students_old_backup', 'U') IS NULL
BEGIN
    SELECT * INTO dbo.students_old_backup FROM dbo.students;
    DROP TABLE dbo.students;
END
ELSE IF OBJECT_ID('dbo.students', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.students;
END
GO

-- 2. Drop existing nhom-2 tables if already present to ensure clean recreation
IF OBJECT_ID('dbo.student_status_histories', 'U') IS NOT NULL DROP TABLE dbo.student_status_histories;
IF OBJECT_ID('dbo.student_status', 'U') IS NOT NULL DROP TABLE dbo.student_status;
IF OBJECT_ID('dbo.advisor_classe_sections', 'U') IS NOT NULL DROP TABLE dbo.advisor_classe_sections;
IF OBJECT_ID('dbo.student_classe_sections', 'U') IS NOT NULL DROP TABLE dbo.student_classe_sections;
IF OBJECT_ID('dbo.student_classes', 'U') IS NOT NULL DROP TABLE dbo.student_classes;
GO

-- Table 1: students (Thông tin hồ sơ sinh viên)
CREATE TABLE [dbo].[students] (
    [id] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    [user_id] UNIQUEIDENTIFIER NULL,
    [code] VARCHAR(100) NULL,
    [full_name] NVARCHAR(255) NULL,
    [date_of_birth] DATE NULL,
    [gender] NVARCHAR(10) NULL,
    [personal_identification_number] VARCHAR(20) NULL,
    [date_of_issue] DATE NULL,
    [card_place] NVARCHAR(100) NULL,
    [address] NVARCHAR(300) NULL,
    [current_address] NVARCHAR(300) NULL,
    [academic_year_year] UNIQUEIDENTIFIER NULL,
    [department_id] UNIQUEIDENTIFIER NULL,
    [major_id] UNIQUEIDENTIFIER NULL,
    [training_program_id] UNIQUEIDENTIFIER NULL,
    [status] VARCHAR(50) NULL,
    [student_classess_id] UNIQUEIDENTIFIER NULL,
    [admission_year] DATETIME2(7) NULL,
    [created_at] DATETIME2(7) NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL DEFAULT 1,
    CONSTRAINT [PK_students] PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

-- Table 2: student_classes (Lớp hành chính / Lớp sinh hoạt)
CREATE TABLE [dbo].[student_classes] (
    [id] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    [code] NVARCHAR(100) NULL,
    [name] NVARCHAR(255) NULL,
    [academic_year_id] UNIQUEIDENTIFIER NULL,
    [department_id] UNIQUEIDENTIFIER NULL,
    [major_id] UNIQUEIDENTIFIER NULL,
    [training_program_id] UNIQUEIDENTIFIER NULL,
    [advisor_id] UNIQUEIDENTIFIER NULL,
    [created_at] DATETIME2(7) NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL DEFAULT 1,
    CONSTRAINT [PK_student_classes] PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

-- Table 3: student_classe_sections (Quản lý danh sách sinh viên trong lớp học hành chính)
CREATE TABLE [dbo].[student_classe_sections] (
    [id] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    [student_id] UNIQUEIDENTIFIER NULL,
    [student_classe_id] UNIQUEIDENTIFIER NULL,
    [status] VARCHAR(50) NULL,
    [note] NVARCHAR(255) NULL,
    [start_date] DATETIME2(7) NULL,
    [end_date] DATETIME2(7) NULL,
    [created_at] DATETIME2(7) NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL DEFAULT 1,
    CONSTRAINT [PK_student_classe_sections] PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

-- Table 4: advisor_classe_sections (Danh sách giảng viên CVHT phụ trách lớp hành chính)
CREATE TABLE [dbo].[advisor_classe_sections] (
    [id] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    [employee_id] UNIQUEIDENTIFIER NULL,
    [student_classe_id] UNIQUEIDENTIFIER NULL,
    [start_date] DATETIME2(7) NULL,
    [end_date] DATETIME2(7) NULL,
    [created_at] DATETIME2(7) NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL DEFAULT 1,
    CONSTRAINT [PK_advisor_classe_sections] PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

-- Table 5: student_status (Bảng trạng thái sinh viên)
CREATE TABLE [dbo].[student_status] (
    [id] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    [student_id] UNIQUEIDENTIFIER NULL,
    [code] NVARCHAR(100) NULL,
    [name] NVARCHAR(100) NULL,
    [description] NVARCHAR(255) NULL,
    [created_at] DATETIME2(7) NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL DEFAULT 1,
    CONSTRAINT [PK_student_status] PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

-- Table 6: student_status_histories (Lịch sử thay đổi trạng thái sinh viên)
CREATE TABLE [dbo].[student_status_histories] (
    [id] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    [student_id] UNIQUEIDENTIFIER NULL,
    [status_id] UNIQUEIDENTIFIER NULL,
    [start_date] DATE NULL,
    [end_date] DATE NULL,
    [reason] NVARCHAR(255) NULL,
    [decision_number] NVARCHAR(255) NULL,
    [decision_date] DATE NULL,
    [note] NVARCHAR(255) NULL,
    [created_at] DATETIME2(7) NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL DEFAULT 1,
    CONSTRAINT [PK_student_status_histories] PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

-- Sample Seed Data
DECLARE @ClassId UNIQUEIDENTIFIER = NEWID();
INSERT INTO [dbo].[student_classes] ([id], [code], [name], [is_active])
VALUES (@ClassId, N'IT24M', N'Lớp CNTT K24 Thạc sĩ', 1);

DECLARE @Student1Id UNIQUEIDENTIFIER = NEWID();
INSERT INTO [dbo].[students] (
    [id], [code], [full_name], [date_of_birth], [gender], [personal_identification_number],
    [address], [current_address], [status], [student_classess_id], [admission_year], [is_active]
) VALUES (
    @Student1Id, 'SV24001', N'Trương Văn Hiếu', '2001-05-15', N'Nam', '001201012345',
    N'Hà Nội', N'Cầu Giấy, Hà Nội', 'studying', @ClassId, '2024-09-05', 1
);

DECLARE @Student2Id UNIQUEIDENTIFIER = NEWID();
INSERT INTO [dbo].[students] (
    [id], [code], [full_name], [date_of_birth], [gender], [personal_identification_number],
    [address], [current_address], [status], [student_classess_id], [admission_year], [is_active]
) VALUES (
    @Student2Id, 'SV24002', N'Nguyễn Thị Mai', '2002-08-20', N'Nữ', '001202054321',
    N'Đà Nẵng', N'Hải Châu, Đà Nẵng', 'studying', @ClassId, '2024-09-05', 1
);

INSERT INTO [dbo].[student_classe_sections] ([id], [student_id], [student_classe_id], [status], [start_date], [is_active])
VALUES 
(NEWID(), @Student1Id, @ClassId, 'studying', '2024-09-05', 1),
(NEWID(), @Student2Id, @ClassId, 'studying', '2024-09-05', 1);

DECLARE @StatusActive UNIQUEIDENTIFIER = NEWID();
DECLARE @StatusReserved UNIQUEIDENTIFIER = NEWID();
INSERT INTO [dbo].[student_status] ([id], [code], [name], [description], [is_active])
VALUES 
(@StatusActive, N'DANG_HOC', N'Đang học', N'Sinh viên đang theo học bình thường', 1),
(@StatusReserved, N'BAO_LUU', N'Bảo lưu', N'Sinh viên đang trong thời gian bảo lưu', 1);

INSERT INTO [dbo].[student_status_histories] ([id], [student_id], [status_id], [start_date], [reason], [decision_number], [decision_date], [is_active])
VALUES 
(NEWID(), @Student1Id, @StatusActive, '2024-09-05', N'Nhập học đợt 1', N'QĐ-2024/01', '2024-09-01', 1);
GO
