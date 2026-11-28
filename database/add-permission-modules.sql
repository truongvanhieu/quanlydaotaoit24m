USE it24m;
GO

IF OBJECT_ID(N'dbo.permission_modules', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.permission_modules (
        id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_permission_modules PRIMARY KEY
            DEFAULT NEWID(),
        code NVARCHAR(100) NOT NULL,
        name NVARCHAR(150) NOT NULL,
        description NVARCHAR(255) NULL,
        created_at DATETIME2 NOT NULL
            CONSTRAINT DF_permission_modules_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        deleted_at DATETIME2 NULL,
        is_active BIT NOT NULL
            CONSTRAINT DF_permission_modules_is_active DEFAULT (1),
        CONSTRAINT UQ_permission_modules_code UNIQUE (code)
    );
END;
GO

WITH ExistingModules AS (
    SELECT DISTINCT UPPER(LTRIM(RTRIM(module))) AS code
    FROM dbo.permissions
    WHERE NULLIF(LTRIM(RTRIM(module)), N'') IS NOT NULL
)
INSERT INTO dbo.permission_modules (code, name)
SELECT
    existing.code,
    CASE existing.code
        WHEN N'ACADEMIC' THEN N'Đào tạo'
        WHEN N'ATTENDANCE' THEN N'Điểm danh'
        WHEN N'CLASSES' THEN N'Lớp hành chính'
        WHEN N'COURSES' THEN N'Học phần'
        WHEN N'CURRICULUM' THEN N'Chương trình đào tạo'
        WHEN N'ENROLLMENT' THEN N'Lớp học phần'
        WHEN N'EXAMS' THEN N'Lịch thi'
        WHEN N'FACILITIES' THEN N'Cơ sở vật chất'
        WHEN N'GRADES' THEN N'Điểm số'
        WHEN N'REGISTRATION' THEN N'Đăng ký học phần'
        WHEN N'REPORTS' THEN N'Báo cáo'
        WHEN N'SCHEDULE' THEN N'Thời khóa biểu'
        WHEN N'STUDENTS' THEN N'Sinh viên'
        WHEN N'TRANSCRIPTS' THEN N'Bảng điểm'
        WHEN N'TUITION' THEN N'Học phí'
        WHEN N'USERS' THEN N'Người dùng'
        WHEN N'SETTINGS' THEN N'Cài đặt'
        ELSE existing.code
    END
FROM ExistingModules AS existing
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.permission_modules AS saved
    WHERE saved.code = existing.code
);
GO
