CREATE PROCEDURE dbo.bs_bsUserInsert (
		@creator INT,
		@login VARCHAR(255),
		@password VARCHAR(255),
		@firstName VARCHAR(255),
		@lastName VARCHAR(255),
		@secondName VARCHAR(255),
		@birthDate DATETIME,
		@eMail VARCHAR(255)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			EXEC dbo.bs_open_bsSK

			DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
			SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()

			INSERT INTO dbo.BS_USER(
					CREATOR,
					LOGIN,
					PASSWORD,
					FIRST_NAME,
					LAST_NAME,
					SECOND_NAME,
					BIRTHDATE,
					E_MAIL
			)
			VALUES (
					@creator,
					ENCRYPTBYKEY(@bsSKeyGuid, @login),
					ENCRYPTBYKEY(@bsSKeyGuid, @password),
					@firstName,
					@lastName,
					@secondName,
					@birthDate,
					@eMail
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