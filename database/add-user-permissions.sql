IF OBJECT_ID(N'dbo.user_permissions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.user_permissions (
        id uniqueidentifier NOT NULL,
        user_id uniqueidentifier NOT NULL,
        permission_id uniqueidentifier NOT NULL,
        created_at datetime2(7) NULL,
        updated_at datetime2(7) NULL,
        deleted_at datetime2(7) NULL,
        is_active bit NOT NULL CONSTRAINT DF_user_permissions_is_active DEFAULT (1),
        CONSTRAINT PK_user_permissions PRIMARY KEY CLUSTERED (id)
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_user_permissions_active_user_permission'
      AND object_id = OBJECT_ID(N'dbo.user_permissions')
)
BEGIN
    CREATE UNIQUE INDEX UX_user_permissions_active_user_permission
        ON dbo.user_permissions (user_id, permission_id)
        WHERE is_active = 1 AND deleted_at IS NULL;
END;
GO
