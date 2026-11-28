/*
   Apply this script in the it24m database.
   It creates the student-to-course-section and lecturer-to-course-class
   association tables only when they are missing.
*/
USE it24m;
GO

IF OBJECT_ID(N'dbo.students_', N'U') IS NULL
    THROW 50001, 'Missing prerequisite table dbo.students_.', 1;
IF OBJECT_ID(N'dbo.course_sections', N'U') IS NULL
    THROW 50002, 'Missing prerequisite table dbo.course_sections.', 1;
IF OBJECT_ID(N'dbo.lecturers', N'U') IS NULL
    THROW 50003, 'Missing prerequisite table dbo.lecturers.', 1;
IF OBJECT_ID(N'dbo.courses_classes', N'U') IS NULL
    THROW 50004, 'Missing prerequisite table dbo.courses_classes.', 1;
GO

IF OBJECT_ID(N'dbo.student_course_sections', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.student_course_sections (
        id uniqueidentifier NOT NULL
            CONSTRAINT DF_student_course_sections_id DEFAULT NEWSEQUENTIALID(),
        student_id uniqueidentifier NOT NULL,
        course_section_id uniqueidentifier NOT NULL,
        registration_date datetime NULL,
        registration_type nvarchar(100) NULL,
        status nvarchar(100) NULL,
        attempt_number nvarchar(100) NULL,
        note nvarchar(255) NULL,
        created_at datetime2(7) NULL
            CONSTRAINT DF_student_course_sections_created_at DEFAULT SYSDATETIME(),
        updated_at datetime2(7) NULL,
        created_by uniqueidentifier NULL,
        updated_by uniqueidentifier NULL,
        deleted_at datetime2(7) NULL,
        deleted_by uniqueidentifier NULL,
        is_active bit NULL
            CONSTRAINT DF_student_course_sections_is_active DEFAULT (1),
        CONSTRAINT PK_student_course_sections PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_student_course_sections_student FOREIGN KEY (student_id)
            REFERENCES dbo.students_ (id),
        CONSTRAINT FK_student_course_sections_section FOREIGN KEY (course_section_id)
            REFERENCES dbo.course_sections (id)
    );

    CREATE INDEX IX_student_course_sections_student
        ON dbo.student_course_sections (student_id, course_section_id)
        INCLUDE (is_active, deleted_at);
END;
GO

IF OBJECT_ID(N'dbo.lecturer_course_classes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lecturer_course_classes (
        id uniqueidentifier NOT NULL
            CONSTRAINT DF_lecturer_course_classes_id DEFAULT NEWSEQUENTIALID(),
        lecturer_id uniqueidentifier NOT NULL,
        course_class_id uniqueidentifier NOT NULL,
        role nvarchar(100) NULL,
        created_at datetime2(7) NULL
            CONSTRAINT DF_lecturer_course_classes_created_at DEFAULT SYSDATETIME(),
        updated_at datetime2(7) NULL,
        created_by uniqueidentifier NULL,
        updated_by uniqueidentifier NULL,
        deleted_at datetime2(7) NULL,
        deleted_by uniqueidentifier NULL,
        is_active bit NULL
            CONSTRAINT DF_lecturer_course_classes_is_active DEFAULT (1),
        CONSTRAINT PK_lecturer_course_classes PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_lecturer_course_classes_lecturer FOREIGN KEY (lecturer_id)
            REFERENCES dbo.lecturers (id),
        CONSTRAINT FK_lecturer_course_classes_class FOREIGN KEY (course_class_id)
            REFERENCES dbo.courses_classes (id)
    );

    CREATE INDEX IX_lecturer_course_classes_lecturer
        ON dbo.lecturer_course_classes (lecturer_id, course_class_id)
        INCLUDE (role, is_active, deleted_at);
END;
GO
