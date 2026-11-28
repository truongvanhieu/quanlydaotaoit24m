USE it24m;
GO

IF OBJECT_ID('dbo.schedule_exceptions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.schedule_exceptions (
        id UNIQUEIDENTIFIER NOT NULL CONSTRAINT PK_schedule_exceptions PRIMARY KEY,
        schedule_id UNIQUEIDENTIFIER NOT NULL,
        occurrence_date DATE NOT NULL,
        exception_type VARCHAR(20) NOT NULL,
        replacement_date DATE NULL,
        room_id UNIQUEIDENTIFIER NULL,
        employee_id UNIQUEIDENTIFIER NULL,
        start_period INT NULL,
        end_period INT NULL,
        note NVARCHAR(255) NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_schedule_exceptions_created_at DEFAULT SYSdatetime(),
        updated_at DATETIME2 NOT NULL CONSTRAINT DF_schedule_exceptions_updated_at DEFAULT SYSdatetime(),
        deleted_at DATETIME2 NULL,
        CONSTRAINT FK_schedule_exceptions_schedules
            FOREIGN KEY (schedule_id) REFERENCES dbo.schedules(id),
        CONSTRAINT UQ_schedule_exceptions_occurrence
            UNIQUE (schedule_id, occurrence_date),
        CONSTRAINT CK_schedule_exceptions_type
            CHECK (exception_type IN ('CANCELLED', 'MAKEUP', 'SUBSTITUTE'))
    );
END;
GO
