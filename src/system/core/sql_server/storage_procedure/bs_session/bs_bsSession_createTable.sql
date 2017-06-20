CREATE PROCEDURE dbo.bs_bsSession_createTable
AS
	SET NOCOUNT ON;
	BEGIN TRY
			CREATE TABLE dbo.BS_SESSION (
					OUID INT PRIMARY KEY IDENTITY(1, 1) NOT NULL,
					CREATE_DATE DATETIME DEFAULT GETDATE() NULL,
					/*
						Если не передан идентификатор создателя записи,
						то по умолчанию будет считаться что запись создал пользовтаель "admin"
					*/
					CREATOR INT DEFAULT 1 NULL,
					/**
						Есди не передан идентификатор статуса записи, то по умолчанию запись
						будет считаться активной
					*/
					STATUS INT DEFAULT 10 NULL,
					GUID VARCHAR(36) DEFAULT NEWID() NULL,
					USER_GUID VARBINARY(255) NULL,
					OUT_TIME DATETIME NULL,
					IP_ADDRESS VARBINARY(255) NULL
			)
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH