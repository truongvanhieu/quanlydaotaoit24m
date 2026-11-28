USE [QuanLyDaoTao];
GO

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.school_years', N'U') IS NULL
    THROW 50001, 'dbo.school_years does not exist in QuanLyDaoTao.', 1;

IF COL_LENGTH(N'dbo.school_years', N'is_active') IS NULL
BEGIN
    ALTER TABLE dbo.school_years
        ADD is_active bit NOT NULL
            CONSTRAINT DF_school_years_is_active DEFAULT (1) WITH VALUES;
END;

COMMIT TRANSACTION;
GO
