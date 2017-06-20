CREATE PROCEDURE dbo.bs_bsBankAccountType_addAccountType(
		@name VARCHAR(255),
		@percent NUMERIC(5, 2),
		@duration SMALLINT
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			INSERT INTO dbo.BS_BANK_ACCOUNT_TYPE (
					[NAME],
					[PERCENT],
					DURATION
			)
			VALUES (
					@name,
					@percent,
					@duration
			)
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH