CREATE PROCEDURE dbo.bs_bsCard_addCard (
		@cardGuid VARCHAR(36),
		@userGuid VARCHAR(36)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			EXEC dbo.bs_open_bsSK

			DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
			SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()
		
			INSERT INTO dbo.BS_CARD (
					GUID,
					USER_GUID, 
					[VALUE]
			) 
			VALUES (
					@cardGuid,
					@userGuid,
					ENCRYPTBYKEY(@bsSKeyGuid, CAST(0.00 AS VARCHAR(255)))
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