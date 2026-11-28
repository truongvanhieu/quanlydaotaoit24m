USE it24m;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    INSERT INTO dbo.students (
        id,
        student_code,
        full_name,
        email,
        phone,
        class_name
    )
    SELECT
        academic.id,
        academic.code,
        COALESCE(NULLIF(LTRIM(RTRIM(academic.full_name)), N''), academic.code),
        COALESCE(users.email, N''),
        COALESCE(users.phone, N''),
        COALESCE(student_class.name, N'')
    FROM dbo.students_ AS academic
    LEFT JOIN dbo.users AS users ON users.id = academic.user_id
    LEFT JOIN dbo.student_classes AS student_class
        ON student_class.id = academic.student_classess_id
    WHERE academic.is_active = 1
      AND academic.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1
          FROM dbo.students AS current_student
          WHERE current_student.id = academic.id
             OR current_student.student_code = academic.code
      );

    DECLARE @InsertedRows INT = @@ROWCOUNT;
    COMMIT TRANSACTION;

    SELECT @InsertedRows AS inserted_students;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
