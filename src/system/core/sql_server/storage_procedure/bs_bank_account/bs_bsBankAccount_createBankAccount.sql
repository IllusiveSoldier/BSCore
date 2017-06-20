CREATE PROCEDURE dbo.bs_bsBankAccount_createBankAccount (
		@guid VARCHAR(255),
		@userGuid VARCHAR(36),
		@value NUMERIC(20, 2),
		@type TINYINT,
		@capitalizationGuid VARCHAR(36),
		@to VARCHAR(255),
		@endDate DATETIME
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			EXEC dbo.bs_open_bsSK

			DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
			SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()

			INSERT INTO dbo.BS_BANK_ACCOUNT (
					GUID,
					USER_GUID,
					BEGIN_DATE,
					VALUE,
					TYPE,
					CAPITALIZATION_GUID,
					[TO],
					END_DATE
			)
			VALUES (
					@guid,
					ENCRYPTBYKEY(@bsSKeyGuid, @userGuid),
					CURRENT_TIMESTAMP,
					ENCRYPTBYKEY(@bsSKeyGuid, CAST(@value AS VARCHAR(255))),
					@type,
					@capitalizationGuid,
					@to,
					@endDate
			)

			EXEC dbo.bs_close_bsSK
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH