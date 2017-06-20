CREATE PROCEDURE dbo.bs_bsUser_updatePassword (
		@userGuid VARCHAR(36),
		@newUserPassword VARCHAR(255)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			EXEC dbo.bs_open_bsSK

			DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
			SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()

			UPDATE dbo.BS_USER
			SET PASSWORD = ENCRYPTBYKEY(@bsSKeyGuid, @newUserPassword)
			WHERE (
					GUID = @userGuid
					AND ISNULL(STATUS, 10) = 10
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