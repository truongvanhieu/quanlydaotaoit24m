USE it24m;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    ;WITH all_students AS (
        SELECT
            legacy.id,
            legacy.student_code AS current_code,
            1 AS source_order,
            CASE
                WHEN legacy.student_code LIKE N'SV%'
                    THEN TRY_CONVERT(INT, SUBSTRING(legacy.student_code, 3, 100))
                ELSE TRY_CONVERT(INT, legacy.student_code)
            END AS code_order
        FROM dbo.students AS legacy
        WHERE NOT EXISTS (
            SELECT 1 FROM dbo.students_ AS academic WHERE academic.id = legacy.id
        )

        UNION ALL

        SELECT
            academic.id,
            academic.code AS current_code,
            0 AS source_order,
            CASE
                WHEN academic.code LIKE N'SV%'
                    THEN TRY_CONVERT(INT, SUBSTRING(academic.code, 3, 100))
                ELSE TRY_CONVERT(INT, academic.code)
            END AS code_order
        FROM dbo.students_ AS academic
        WHERE academic.deleted_at IS NULL
    ), numbered_students AS (
        SELECT
            id,
            current_code,
            ROW_NUMBER() OVER (
                ORDER BY source_order, code_order, current_code, id
            ) AS row_num
        FROM all_students
    )
    SELECT
        id,
        current_code,
        N'SV' + RIGHT(N'0000' + CONVERT(NVARCHAR(10), row_num), 4) AS new_code
    INTO #student_code_map
    FROM numbered_students;

    IF EXISTS (SELECT 1 FROM #student_code_map WHERE LEN(new_code) <> 6)
        THROW 50001, 'Student count exceeds the four-digit SV code range.', 1;

    -- Move academic codes out of the target range first to avoid unique-key collisions.
    UPDATE academic
    SET academic.code = N'TMP-' + CONVERT(NVARCHAR(36), academic.id)
    FROM dbo.students_ AS academic
    JOIN #student_code_map AS mapping ON mapping.id = academic.id;

    UPDATE academic
    SET academic.code = mapping.new_code
    FROM dbo.students_ AS academic
    JOIN #student_code_map AS mapping ON mapping.id = academic.id;

    UPDATE legacy
    SET legacy.student_code = mapping.new_code
    FROM dbo.students AS legacy
    JOIN #student_code_map AS mapping ON mapping.id = legacy.id;

    COMMIT TRANSACTION;

    SELECT COUNT(*) AS synchronized_student_codes FROM #student_code_map;
    SELECT TOP (10) mapping.current_code, mapping.new_code
    FROM #student_code_map AS mapping
    ORDER BY mapping.new_code;
    DROP TABLE #student_code_map;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    IF OBJECT_ID('tempdb..#student_code_map') IS NOT NULL DROP TABLE #student_code_map;
    THROW;
END CATCH;
GO
