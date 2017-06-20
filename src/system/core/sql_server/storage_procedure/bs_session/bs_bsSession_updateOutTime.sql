CREATE PROCEDURE dbo.bs_bsSession_updateOutTime (
		@outTime DATETIME,
		@userGuid VARCHAR(36),
		@sessionGuid VARCHAR(36)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			EXEC dbo.bs_open_bsSK

			UPDATE dbo.BS_SESSION
			SET OUT_TIME = @outTime
			WHERE (
					DECRYPTBYKEY(USER_GUID) = @userGuid
					AND GUID = @sessionGuid
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