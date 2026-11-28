USE it24m;
GO

IF OBJECT_ID('dbo.revoked_tokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.revoked_tokens (
        id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_revoked_tokens PRIMARY KEY
            DEFAULT NEWID(),

        token_id NVARCHAR(255) NOT NULL,

        user_id UNIQUEIDENTIFIER NOT NULL,

        expires_at DATETIME2 NOT NULL,

        revoked_at DATETIME2 NOT NULL
            DEFAULT GETDATE(),

        CONSTRAINT UQ_revoked_tokens_token_id
            UNIQUE (token_id),

        CONSTRAINT FK_revoked_tokens_users
            FOREIGN KEY (user_id)
            REFERENCES dbo.users(id)
    );

    CREATE INDEX IX_revoked_tokens_user_id
        ON dbo.revoked_tokens(user_id);

    CREATE INDEX IX_revoked_tokens_expires_at
        ON dbo.revoked_tokens(expires_at);
END;
GO


USE it24m;
GO

IF OBJECT_ID('dbo.password_reset_tokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.password_reset_tokens (
        id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_password_reset_tokens
            PRIMARY KEY DEFAULT NEWID(),

        token_hash NVARCHAR(255) NOT NULL,

        user_id UNIQUEIDENTIFIER NOT NULL,

        expires_at DATETIME2 NOT NULL,

        used_at DATETIME2 NULL,

        created_at DATETIME2 NOT NULL
            DEFAULT GETDATE(),

        CONSTRAINT UQ_password_reset_tokens_token_hash
            UNIQUE (token_hash),

        CONSTRAINT FK_password_reset_tokens_users
            FOREIGN KEY (user_id)
            REFERENCES dbo.users(id)
    );

    CREATE INDEX IX_password_reset_tokens_user_id
        ON dbo.password_reset_tokens(user_id);

    CREATE INDEX IX_password_reset_tokens_expires_at
        ON dbo.password_reset_tokens(expires_at);
END;
GO
