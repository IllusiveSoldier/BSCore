CREATE PROCEDURE dbo.bs_bsBankAccount_closeDeposit (
		@depositGuid VARCHAR(36)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			UPDATE dbo.BS_BANK_ACCOUNT
			SET [STATUS] = 70
			WHERE GUID = @depositGuid
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK
		
			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH