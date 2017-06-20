CREATE PROCEDURE dbo.bs_bsUser_changeAvatar (
		@userGuid VARCHAR(36),
		@userAvatar VARBINARY(MAX)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			UPDATE dbo.BS_USER
			SET AVATAR = @userAvatar
			WHERE (
					GUID = @userGuid
					AND ISNULL(STATUS, 10) = 10
			)
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH