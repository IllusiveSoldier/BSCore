CREATE PROCEDURE dbo.bs_updateUserStatusDelete(
		@login VARCHAR(255),
		@eMail VARCHAR(255)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			EXEC dbo.bs_open_bsSK

			UPDATE dbo.BS_USER
			SET STATUS = 70
			WHERE (
					ISNULL( STATUS, 10 ) = 10
					AND (
							DECRYPTBYKEY( LOGIN ) = @login
							OR E_MAIL = @eMail
					)
			)

			EXEC dbo.bs_close_bsSK
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