CREATE PROCEDURE dbo.bs_bsCard_closeCard (
		@cardGuid VARCHAR(255)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			UPDATE dbo.BS_CARD
			SET STATUS = 70
			WHERE GUID = @cardGuid
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK
		
			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH