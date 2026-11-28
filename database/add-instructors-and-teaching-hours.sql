USE [QuanLyDaoTao];
GO

IF OBJECT_ID(N'dbo.instructors', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.instructors
    (
        id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_instructors PRIMARY KEY,
        code NVARCHAR(50) NOT NULL,
        name NVARCHAR(255) NOT NULL,
        created_at DATETIME2(7) NOT NULL
            CONSTRAINT DF_instructors_created_at DEFAULT (SYSUTCDATETIME()),
        is_active BIT NOT NULL
            CONSTRAINT DF_instructors_is_active DEFAULT (1)
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.instructors')
      AND name = N'UX_instructors_code'
)
BEGIN
    CREATE UNIQUE INDEX UX_instructors_code
        ON dbo.instructors(code);
END;
GO

IF COL_LENGTH(N'dbo.lecturer_course_classes', N'teaching_hours') IS NULL
BEGIN
    ALTER TABLE dbo.lecturer_course_classes
        ADD teaching_hours INT NULL;
END;
GO

INSERT INTO dbo.instructors (id, code, name, created_at, is_active)
SELECT DISTINCT
    lcc.employee_id,
    CONVERT(NVARCHAR(50), lcc.employee_id),
    N'Chưa cập nhật',
    SYSUTCDATETIME(),
    1
FROM dbo.lecturer_course_classes lcc
WHERE lcc.employee_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.instructors i
      WHERE i.id = lcc.employee_id
         OR i.code = CONVERT(NVARCHAR(50), lcc.employee_id)
  );
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'FK_lecturer_course_classes_instructors'
      AND parent_object_id = OBJECT_ID(N'dbo.lecturer_course_classes')
)
BEGIN
    ALTER TABLE dbo.lecturer_course_classes WITH CHECK
        ADD CONSTRAINT FK_lecturer_course_classes_instructors
        FOREIGN KEY (employee_id) REFERENCES dbo.instructors(id);
END;
GO
