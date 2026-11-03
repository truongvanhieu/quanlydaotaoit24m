USE it24m;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    CREATE TABLE #PatternTemplates (
        pattern_id INT NOT NULL PRIMARY KEY,
        source_section_id UNIQUEIDENTIFIER NOT NULL
    );

    ;WITH slots AS (
        SELECT DISTINCT course_section_id, day_of_week, start_period, end_period
        FROM dbo.schedules
        WHERE is_active = 1 AND deleted_at IS NULL
    ), signatures AS (
        SELECT course_section_id,
               STRING_AGG(CONCAT(day_of_week, ':', start_period, '-', end_period), ',')
                   WITHIN GROUP (ORDER BY day_of_week, start_period, end_period) AS signature
        FROM slots
        GROUP BY course_section_id
    ), ranked AS (
        SELECT course_section_id, signature,
               DENSE_RANK() OVER (ORDER BY signature) AS pattern_id
        FROM signatures
    ), first_template AS (
        SELECT pattern_id, course_section_id,
               ROW_NUMBER() OVER (PARTITION BY pattern_id ORDER BY course_section_id) AS row_num
        FROM ranked
    )
    INSERT INTO #PatternTemplates (pattern_id, source_section_id)
    SELECT pattern_id, course_section_id
    FROM first_template
    WHERE row_num = 1;

    IF (SELECT COUNT(*) FROM #PatternTemplates) <> 5
        THROW 50001, 'Expected five non-overlapping sample timetable patterns.', 1;

    CREATE TABLE #PatternSlots (
        pattern_id INT NOT NULL,
        day_of_week INT NOT NULL,
        start_period INT NOT NULL,
        end_period INT NOT NULL
    );

    INSERT INTO #PatternSlots (pattern_id, day_of_week, start_period, end_period)
    SELECT DISTINCT template.pattern_id, schedule.day_of_week, schedule.start_period, schedule.end_period
    FROM #PatternTemplates AS template
    JOIN dbo.schedules AS schedule ON schedule.course_section_id = template.source_section_id
    WHERE schedule.is_active = 1 AND schedule.deleted_at IS NULL;

    IF EXISTS (
        SELECT 1
        FROM #PatternSlots AS first_pattern
        JOIN #PatternSlots AS second_pattern
          ON first_pattern.pattern_id < second_pattern.pattern_id
         AND first_pattern.day_of_week = second_pattern.day_of_week
         AND first_pattern.start_period <= second_pattern.end_period
         AND first_pattern.end_period >= second_pattern.start_period
    )
        THROW 50002, 'Sample timetable patterns overlap; refusing to assign conflicting classes.', 1;

    -- Marketing and Nursing have catalog courses but no scheduled sections.
    -- Create five sample sections per course using the existing five time patterns.
    INSERT INTO dbo.course_sections (
        code, name, course_id, semester_id, room_id, building_id,
        max_students, min_students, class_type, status, note, is_active
    )
    SELECT
        CONCAT(course.code, '-260', template.pattern_id),
        CONCAT(course.name, N' - Lớp mẫu ', template.pattern_id),
        course.id,
        source_section.semester_id,
        room.id,
        room.building_id,
        50,
        10,
        N'Lý thuyết',
        N'Đang mở',
        CONCAT(N'SAMPLE_MAJOR_PATTERN_', template.pattern_id),
        1
    FROM dbo.courses AS course
    JOIN dbo.departments AS department ON department.id = course.department_id
    CROSS JOIN #PatternTemplates AS template
    JOIN dbo.course_sections AS source_section ON source_section.id = template.source_section_id
    CROSS APPLY (
        SELECT TOP (1) id, building_id
        FROM dbo.rooms
        WHERE is_active = 1 AND deleted_at IS NULL
        ORDER BY NEWID()
    ) AS room
    WHERE department.code IN ('DD', 'MKT')
      AND course.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM dbo.course_sections AS existing
          WHERE existing.code = CONCAT(course.code, '-260', template.pattern_id)
            AND existing.deleted_at IS NULL
      );

    CREATE TABLE #TargetStaff (
        course_section_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        employee_id UNIQUEIDENTIFIER NOT NULL,
        room_id UNIQUEIDENTIFIER NOT NULL
    );

    INSERT INTO #TargetStaff (course_section_id, employee_id, room_id)
    SELECT section.id, employee.id, section.room_id
    FROM dbo.course_sections AS section
    JOIN dbo.courses AS course ON course.id = section.course_id
    JOIN dbo.departments AS department ON department.id = course.department_id
    CROSS APPLY (
        SELECT TOP (1) id
        FROM dbo.employees
        WHERE department_id = department.id
          AND is_active = 1
          AND deleted_at IS NULL
        ORDER BY NEWID()
    ) AS employee
    WHERE department.code IN ('DD', 'MKT')
      AND section.note LIKE N'SAMPLE_MAJOR_PATTERN[_]%'
      AND section.deleted_at IS NULL
      AND section.is_active = 1;

    -- Copy each section's twenty dated sessions from its matching pattern.
    INSERT INTO dbo.schedules (
        course_section_id, employee_id, room_id, day_of_week, [date], shift_id,
        start_period, end_period, number_of_periods, start_date, end_date,
        mode, status, description, schedule_status, note, is_active
    )
    SELECT
        target.id, staff.employee_id, staff.room_id, source.day_of_week, source.[date], source.shift_id,
        source.start_period, source.end_period, source.number_of_periods, source.start_date, source.end_date,
        source.mode, source.status, source.description, source.schedule_status,
        N'Lịch mẫu ngẫu nhiên theo ngành', 1
    FROM dbo.course_sections AS target
    JOIN #TargetStaff AS staff ON staff.course_section_id = target.id
    JOIN dbo.courses AS course ON course.id = target.course_id
    JOIN #PatternTemplates AS template
      ON template.pattern_id = TRY_CONVERT(INT, RIGHT(target.note, 1))
    JOIN dbo.schedules AS source ON source.course_section_id = template.source_section_id
    WHERE target.note LIKE N'SAMPLE_MAJOR_PATTERN[_]%'
      AND target.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM dbo.schedules AS existing
          WHERE existing.course_section_id = target.id AND existing.deleted_at IS NULL
      );

    CREATE TABLE #EligibleSections (
        course_section_id UNIQUEIDENTIFIER NOT NULL,
        course_id UNIQUEIDENTIFIER NOT NULL,
        department_id UNIQUEIDENTIFIER NOT NULL,
        pattern_id INT NOT NULL,
        PRIMARY KEY (course_section_id)
    );

    ;WITH slots AS (
        SELECT DISTINCT course_section_id, day_of_week, start_period, end_period
        FROM dbo.schedules
        WHERE is_active = 1 AND deleted_at IS NULL
    ), signatures AS (
        SELECT course_section_id,
               STRING_AGG(CONCAT(day_of_week, ':', start_period, '-', end_period), ',')
                   WITHIN GROUP (ORDER BY day_of_week, start_period, end_period) AS signature
        FROM slots
        GROUP BY course_section_id
    ), ranked AS (
        SELECT course_section_id,
               DENSE_RANK() OVER (ORDER BY signature) AS pattern_id
        FROM signatures
    )
    INSERT INTO #EligibleSections (course_section_id, course_id, department_id, pattern_id)
    SELECT section.id, course.id, course.department_id, ranked.pattern_id
    FROM ranked
    JOIN dbo.course_sections AS section ON section.id = ranked.course_section_id
    JOIN dbo.courses AS course ON course.id = section.course_id
    WHERE section.is_active = 1 AND section.deleted_at IS NULL
      AND course.is_active = 1 AND course.deleted_at IS NULL;

    IF EXISTS (
        SELECT 1
        FROM dbo.students_ AS student
        JOIN dbo.majors AS major ON major.id = student.major_id
        WHERE student.is_active = 1 AND student.deleted_at IS NULL
          AND (SELECT COUNT(DISTINCT eligible.course_id)
               FROM #EligibleSections AS eligible
               WHERE eligible.department_id = major.department_id) < 4
    )
        THROW 50003, 'A student major has fewer than four scheduled courses.', 1;

    -- Retire the previous all-courses sample assignments before re-seeding.
    UPDATE dbo.student_course_sections
    SET deleted_at = SYSDATETIME(), updated_at = SYSDATETIME(), is_active = 0
    WHERE deleted_at IS NULL
      AND is_active = 1
      AND registration_type IN (N'SAMPLE', N'SAMPLE_RANDOM_MAJOR');

    CREATE TABLE #CoursePicks (
        pick_order INT NOT NULL PRIMARY KEY,
        course_id UNIQUEIDENTIFIER NOT NULL
    );
    CREATE TABLE #UsedPatterns (pattern_id INT NOT NULL PRIMARY KEY);
    CREATE TABLE #NewStudentSections (
        student_id UNIQUEIDENTIFIER NOT NULL,
        course_section_id UNIQUEIDENTIFIER NOT NULL,
        PRIMARY KEY (student_id, course_section_id)
    );

    DECLARE @StudentId UNIQUEIDENTIFIER;
    DECLARE @DepartmentId UNIQUEIDENTIFIER;
    DECLARE @PickOrder INT;
    DECLARE @CourseId UNIQUEIDENTIFIER;
    DECLARE @SectionId UNIQUEIDENTIFIER;
    DECLARE @PatternId INT;

    DECLARE student_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT student.id, major.department_id
        FROM dbo.students_ AS student
        JOIN dbo.majors AS major ON major.id = student.major_id
        WHERE student.is_active = 1 AND student.deleted_at IS NULL
        ORDER BY NEWID();

    OPEN student_cursor;
    FETCH NEXT FROM student_cursor INTO @StudentId, @DepartmentId;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DELETE FROM #CoursePicks;
        DELETE FROM #UsedPatterns;

        INSERT INTO #CoursePicks (pick_order, course_id)
        SELECT ROW_NUMBER() OVER (ORDER BY NEWID()), selected.course_id
        FROM (
            SELECT TOP (4) eligible.course_id
            FROM #EligibleSections AS eligible
            WHERE eligible.department_id = @DepartmentId
            GROUP BY eligible.course_id
            ORDER BY NEWID()
        ) AS selected;

        IF (SELECT COUNT(*) FROM #CoursePicks) <> 4
            THROW 50004, 'Unable to pick four major-specific courses for a student.', 1;

        SET @PickOrder = 1;
        WHILE @PickOrder <= 4
        BEGIN
            SELECT @CourseId = course_id FROM #CoursePicks WHERE pick_order = @PickOrder;
            SET @SectionId = NULL;
            SET @PatternId = NULL;

            SELECT TOP (1) @SectionId = eligible.course_section_id,
                           @PatternId = eligible.pattern_id
            FROM #EligibleSections AS eligible
            WHERE eligible.course_id = @CourseId
              AND eligible.department_id = @DepartmentId
              AND NOT EXISTS (
                  SELECT 1 FROM #UsedPatterns AS used WHERE used.pattern_id = eligible.pattern_id
              )
            ORDER BY NEWID();

            IF @SectionId IS NULL
                THROW 50005, 'Unable to select a non-conflicting timetable pattern.', 1;

            INSERT INTO #UsedPatterns (pattern_id) VALUES (@PatternId);
            INSERT INTO #NewStudentSections (student_id, course_section_id)
            VALUES (@StudentId, @SectionId);

            SET @PickOrder += 1;
        END;

        FETCH NEXT FROM student_cursor INTO @StudentId, @DepartmentId;
    END;

    CLOSE student_cursor;
    DEALLOCATE student_cursor;

    INSERT INTO dbo.student_course_sections (
        student_id, course_section_id, registration_date, registration_type,
        status, attempt_number, note, created_at, is_active
    )
    SELECT
        selected.student_id,
        selected.course_section_id,
        GETDATE(),
        N'SAMPLE_RANDOM_MAJOR',
        N'Đang học',
        N'1',
        N'Lịch mẫu ngẫu nhiên theo ngành',
        SYSDATETIME(),
        1
    FROM #NewStudentSections AS selected;

    COMMIT TRANSACTION;

    SELECT COUNT(*) AS new_student_course_sections FROM #NewStudentSections;
    SELECT department.code AS department_code,
           COUNT(DISTINCT selected.student_id) AS students,
           COUNT(*) AS assigned_sections
    FROM #NewStudentSections AS selected
    JOIN dbo.students_ AS student ON student.id = selected.student_id
    JOIN dbo.majors AS major ON major.id = student.major_id
    JOIN dbo.departments AS department ON department.id = major.department_id
    GROUP BY department.code
    ORDER BY department.code;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    IF CURSOR_STATUS('local', 'student_cursor') >= 0 CLOSE student_cursor;
    IF CURSOR_STATUS('local', 'student_cursor') > -3 DEALLOCATE student_cursor;
    THROW;
END CATCH;
GO
