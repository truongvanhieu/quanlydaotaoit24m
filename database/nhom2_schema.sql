USE [it24m];
GO
SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

-- ============================================================================
-- NHOM 2 DATABASE SCHEMA - QUAN LY SINH VIEN & LOP HANH CHINH
-- Gồm 6 bảng chính có đầy đủ Primary Key, Foreign Key, Unique Index & Index
-- ============================================================================

-- 1. Drop tables theo đúng thứ tự bảng con -> bảng cha để tránh lỗi khóa ngoại
IF OBJECT_ID('dbo.student_status_histories', 'U') IS NOT NULL DROP TABLE [dbo].[student_status_histories];
IF OBJECT_ID('dbo.student_status', 'U') IS NOT NULL DROP TABLE [dbo].[student_status];
IF OBJECT_ID('dbo.advisor_classe_sections', 'U') IS NOT NULL DROP TABLE [dbo].[advisor_classe_sections];
IF OBJECT_ID('dbo.student_classe_sections', 'U') IS NOT NULL DROP TABLE [dbo].[student_classe_sections];
IF OBJECT_ID('dbo.students', 'U') IS NOT NULL DROP TABLE [dbo].[students];
IF OBJECT_ID('dbo.student_classes', 'U') IS NOT NULL DROP TABLE [dbo].[student_classes];
GO

-- ============================================================================
-- 2. Tạo các bảng theo thứ tự bảng cha -> bảng con
-- ============================================================================

-- Table 1: student_classes (Lớp hành chính / Lớp sinh hoạt)
CREATE TABLE [dbo].[student_classes] (
    [id] UNIQUEIDENTIFIER NOT NULL CONSTRAINT [DF_student_classes_id] DEFAULT NEWID(),
    [code] NVARCHAR(100) NULL,
    [name] NVARCHAR(255) NULL,
    [academic_year_id] UNIQUEIDENTIFIER NULL,
    [department_id] UNIQUEIDENTIFIER NULL,
    [major_id] UNIQUEIDENTIFIER NULL,
    [training_program_id] UNIQUEIDENTIFIER NULL,
    [advisor_id] UNIQUEIDENTIFIER NULL,
    [created_at] DATETIME2(7) NULL CONSTRAINT [DF_student_classes_created_at] DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL CONSTRAINT [DF_student_classes_is_active] DEFAULT 1,
    CONSTRAINT [PK_student_classes] PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE UNIQUE NONCLUSTERED INDEX [UQ_student_classes_code]
    ON [dbo].[student_classes] ([code])
    WHERE [code] IS NOT NULL;
GO

-- Table 2: students (Thông tin hồ sơ sinh viên)
CREATE TABLE [dbo].[students] (
    [id] UNIQUEIDENTIFIER NOT NULL CONSTRAINT [DF_students_id] DEFAULT NEWID(),
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
    [created_at] DATETIME2(7) NULL CONSTRAINT [DF_students_created_at] DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL CONSTRAINT [DF_students_is_active] DEFAULT 1,
    CONSTRAINT [PK_students] PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [FK_students_student_classes]
        FOREIGN KEY ([student_classess_id]) REFERENCES [dbo].[student_classes] ([id])
);
GO

CREATE UNIQUE NONCLUSTERED INDEX [UQ_students_code]
    ON [dbo].[students] ([code])
    WHERE [code] IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX [IX_students_student_classess_id]
    ON [dbo].[students] ([student_classess_id]);
GO

-- Table 3: student_classe_sections (Quản lý danh sách sinh viên trong lớp hành chính)
CREATE TABLE [dbo].[student_classe_sections] (
    [id] UNIQUEIDENTIFIER NOT NULL CONSTRAINT [DF_student_classe_sections_id] DEFAULT NEWID(),
    [student_id] UNIQUEIDENTIFIER NOT NULL,
    [student_classe_id] UNIQUEIDENTIFIER NOT NULL,
    [status] VARCHAR(50) NULL,
    [note] NVARCHAR(255) NULL,
    [start_date] DATETIME2(7) NULL,
    [end_date] DATETIME2(7) NULL,
    [created_at] DATETIME2(7) NULL CONSTRAINT [DF_student_classe_sections_created_at] DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL CONSTRAINT [DF_student_classe_sections_is_active] DEFAULT 1,
    CONSTRAINT [PK_student_classe_sections] PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [FK_student_classe_sections_students]
        FOREIGN KEY ([student_id]) REFERENCES [dbo].[students] ([id]),
    CONSTRAINT [FK_student_classe_sections_student_classes]
        FOREIGN KEY ([student_classe_id]) REFERENCES [dbo].[student_classes] ([id])
);
GO

CREATE NONCLUSTERED INDEX [IX_student_classe_sections_student_id]
    ON [dbo].[student_classe_sections] ([student_id]);
GO

CREATE NONCLUSTERED INDEX [IX_student_classe_sections_student_classe_id]
    ON [dbo].[student_classe_sections] ([student_classe_id]);
GO

-- Table 4: advisor_classe_sections (Danh sách giảng viên CVHT phụ trách lớp hành chính)
CREATE TABLE [dbo].[advisor_classe_sections] (
    [id] UNIQUEIDENTIFIER NOT NULL CONSTRAINT [DF_advisor_classe_sections_id] DEFAULT NEWID(),
    [employee_id] UNIQUEIDENTIFIER NULL,
    [student_classe_id] UNIQUEIDENTIFIER NOT NULL,
    [start_date] DATETIME2(7) NULL,
    [end_date] DATETIME2(7) NULL,
    [created_at] DATETIME2(7) NULL CONSTRAINT [DF_advisor_classe_sections_created_at] DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL CONSTRAINT [DF_advisor_classe_sections_is_active] DEFAULT 1,
    CONSTRAINT [PK_advisor_classe_sections] PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [FK_advisor_classe_sections_student_classes]
        FOREIGN KEY ([student_classe_id]) REFERENCES [dbo].[student_classes] ([id])
);
GO

CREATE NONCLUSTERED INDEX [IX_advisor_classe_sections_student_classe_id]
    ON [dbo].[advisor_classe_sections] ([student_classe_id]);
GO

-- Table 5: student_status (Danh mục trạng thái sinh viên)
CREATE TABLE [dbo].[student_status] (
    [id] UNIQUEIDENTIFIER NOT NULL CONSTRAINT [DF_student_status_id] DEFAULT NEWID(),
    [student_id] UNIQUEIDENTIFIER NULL,
    [code] NVARCHAR(100) NULL,
    [name] NVARCHAR(100) NULL,
    [description] NVARCHAR(255) NULL,
    [created_at] DATETIME2(7) NULL CONSTRAINT [DF_student_status_created_at] DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL CONSTRAINT [DF_student_status_is_active] DEFAULT 1,
    CONSTRAINT [PK_student_status] PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [FK_student_status_students]
        FOREIGN KEY ([student_id]) REFERENCES [dbo].[students] ([id])
);
GO

CREATE UNIQUE NONCLUSTERED INDEX [UQ_student_status_code]
    ON [dbo].[student_status] ([code])
    WHERE [code] IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX [IX_student_status_student_id]
    ON [dbo].[student_status] ([student_id]);
GO

-- Table 6: student_status_histories (Lịch sử thay đổi trạng thái sinh viên)
CREATE TABLE [dbo].[student_status_histories] (
    [id] UNIQUEIDENTIFIER NOT NULL CONSTRAINT [DF_student_status_histories_id] DEFAULT NEWID(),
    [student_id] UNIQUEIDENTIFIER NOT NULL,
    [status_id] UNIQUEIDENTIFIER NULL,
    [start_date] DATE NULL,
    [end_date] DATE NULL,
    [reason] NVARCHAR(255) NULL,
    [decision_number] NVARCHAR(255) NULL,
    [decision_date] DATE NULL,
    [note] NVARCHAR(255) NULL,
    [created_at] DATETIME2(7) NULL CONSTRAINT [DF_student_status_histories_created_at] DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] UNIQUEIDENTIFIER NULL,
    [updated_by] UNIQUEIDENTIFIER NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] UNIQUEIDENTIFIER NULL,
    [is_active] BIT NULL CONSTRAINT [DF_student_status_histories_is_active] DEFAULT 1,
    CONSTRAINT [PK_student_status_histories] PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [FK_student_status_histories_students]
        FOREIGN KEY ([student_id]) REFERENCES [dbo].[students] ([id]),
    CONSTRAINT [FK_student_status_histories_student_status]
        FOREIGN KEY ([status_id]) REFERENCES [dbo].[student_status] ([id])
);
GO

CREATE NONCLUSTERED INDEX [IX_student_status_histories_student_id]
    ON [dbo].[student_status_histories] ([student_id]);
GO

CREATE NONCLUSTERED INDEX [IX_student_status_histories_status_id]
    ON [dbo].[student_status_histories] ([status_id]);
GO

-- ============================================================================
-- 3. Dữ liệu mẫu (Sample Seed Data)
-- ============================================================================
DECLARE @ClassId UNIQUEIDENTIFIER = NEWID();
DECLARE @AdvisorId UNIQUEIDENTIFIER = NEWID();

INSERT INTO [dbo].[student_classes] ([id], [code], [name], [advisor_id], [is_active])
VALUES (@ClassId, N'IT24M', N'Lớp CNTT K24 Thạc sĩ', @AdvisorId, 1);

INSERT INTO [dbo].[advisor_classe_sections] ([id], [employee_id], [student_classe_id], [start_date], [is_active])
VALUES (NEWID(), @AdvisorId, @ClassId, '2024-09-01', 1);

DECLARE @StatusActive UNIQUEIDENTIFIER = NEWID();
DECLARE @StatusReserved UNIQUEIDENTIFIER = NEWID();
DECLARE @StatusDropped UNIQUEIDENTIFIER = NEWID();
DECLARE @StatusGraduated UNIQUEIDENTIFIER = NEWID();

INSERT INTO [dbo].[student_status] ([id], [code], [name], [description], [is_active])
VALUES
(@StatusActive, N'DANG_HOC', N'Đang học', N'Sinh viên đang theo học bình thường', 1),
(@StatusReserved, N'BAO_LUU', N'Bảo lưu', N'Sinh viên đang trong thời gian bảo lưu', 1),
(@StatusDropped, N'THOI_HOC', N'Thôi học', N'Sinh viên đã thôi học', 1),
(@StatusGraduated, N'TOT_NGHIEP', N'Đã tốt nghiệp', N'Sinh viên đã hoàn thành chương trình đào tạo', 1);

DECLARE @Student1Id UNIQUEIDENTIFIER = NEWID();
INSERT INTO [dbo].[students] (
    [id], [code], [full_name], [date_of_birth], [gender], [personal_identification_number],
    [address], [current_address], [status], [student_classess_id], [admission_year], [is_active]
) VALUES (
    @Student1Id, 'SV24001', N'Trương Văn Hiếu', '2001-05-15', N'Nam', '001201012345',
    N'Hà Nội', N'Cầu Giấy, Hà Nội', 'DANG_HOC', @ClassId, '2024-09-05', 1
);

DECLARE @Student2Id UNIQUEIDENTIFIER = NEWID();
INSERT INTO [dbo].[students] (
    [id], [code], [full_name], [date_of_birth], [gender], [personal_identification_number],
    [address], [current_address], [status], [student_classess_id], [admission_year], [is_active]
) VALUES (
    @Student2Id, 'SV24002', N'Nguyễn Thị Mai', '2002-08-20', N'Nữ', '001202054321',
    N'Đà Nẵng', N'Hải Châu, Đà Nẵng', 'DANG_HOC', @ClassId, '2024-09-05', 1
);

INSERT INTO [dbo].[student_classe_sections] ([id], [student_id], [student_classe_id], [status], [start_date], [is_active])
VALUES
(NEWID(), @Student1Id, @ClassId, 'studying', '2024-09-05', 1),
(NEWID(), @Student2Id, @ClassId, 'studying', '2024-09-05', 1);

INSERT INTO [dbo].[student_status_histories] ([id], [student_id], [status_id], [start_date], [reason], [decision_number], [decision_date], [is_active])
VALUES
(NEWID(), @Student1Id, @StatusActive, '2024-09-05', N'Nhập học đợt 1', N'QĐ-2024/01', '2024-09-01', 1),
(NEWID(), @Student2Id, @StatusActive, '2024-09-05', N'Nhập học đợt 1', N'QĐ-2024/01', '2024-09-01', 1);
GO
