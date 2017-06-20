CREATE PROCEDURE dbo.bs_bsUser_deleteUser (
		@userGuid VARCHAR(36)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			BEGIN TRANSACTION deleteUser

			UPDATE dbo.BS_USER
			SET STATUS = 70
			WHERE GUID = @userGuid

			EXEC dbo.bs_open_bsSK
			UPDATE dbo.BS_BANK_ACCOUNT
			SET STATUS = 70
			WHERE DECRYPTBYKEY(USER_GUID) = @userGuid

			UPDATE dbo.BS_SESSION
			SET STATUS = 70
			WHERE DECRYPTBYKEY(USER_GUID) = @userGuid
			EXEC dbo.bs_close_bsSK
		
			UPDATE dbo.BS_CARD
			SET STATUS = 70
			WHERE USER_GUID = @userGuid
		
			COMMIT TRANSACTION deleteUser
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH