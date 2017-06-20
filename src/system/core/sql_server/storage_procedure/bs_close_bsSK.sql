CREATE PROCEDURE dbo.bs_close_bsSK
AS
	SET NOCOUNT ON;
	BEGIN TRY
			CLOSE SYMMETRIC KEY bsSK
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
				ROLLBACK

			DECLARE @ErrorMessage NVARCHAR(4000), @ErrorSeverity INT;
			SELECT
				@ErrorMessage = ERROR_MESSAGE( ),
				@ErrorSeverity = ERROR_SEVERITY( );
			RAISERROR (@ErrorMessage, @ErrorSeverity, 1);
	END CATCH