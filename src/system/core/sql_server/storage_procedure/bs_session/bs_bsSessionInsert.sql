CREATE PROCEDURE dbo.bs_bsSessionInsert (
		@creator INT,
		@sessionGuid VARCHAR(36),
		@userGuid VARCHAR(36),
		@ipAddress VARCHAR(255)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			EXEC dbo.bs_open_bsSK

			DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
			SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()

			INSERT INTO dbo.BS_SESSION (
					CREATOR,
					GUID,
					USER_GUID,
					IP_ADDRESS
			)
			VALUES (
					@creator,
					@sessionGuid,
					ENCRYPTBYKEY(@bsSKeyGuid, @userGuid),
					ENCRYPTBYKEY(@bsSKeyGuid, @ipAddress)
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