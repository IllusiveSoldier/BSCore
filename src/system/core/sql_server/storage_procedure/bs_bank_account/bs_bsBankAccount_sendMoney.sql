CREATE PROCEDURE dbo.bs_bsBankAccount_sendMoney (
		@bankAccountGuidFrom VARCHAR(36),
		@bankAccountGuidTo VARCHAR(36),
		@value NUMERIC(20, 2)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			BEGIN TRANSACTION sendMoney;

			EXEC dbo.bs_open_bsSK

			DECLARE @isExist NUMERIC(20, 2)
			SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
			FROM dbo.BS_BANK_ACCOUNT
			WHERE GUID = @bankAccountGuidFrom

			DECLARE @canSendValue NUMERIC(20, 2)
			SELECT @canSendValue =  CASE
																	WHEN @value <= 0 THEN 0
																	WHEN @isExist > 0 AND @value > 0 AND @isExist >= @value THEN @value
																	ELSE @value - (@value - @isExist)
															END
			FROM dbo.BS_BANK_ACCOUNT
			WHERE GUID = @bankAccountGuidFrom

			DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
			SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()

			UPDATE dbo.BS_BANK_ACCOUNT
			SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExist - @canSendValue) AS VARCHAR(255)))
			WHERE GUID = @bankAccountGuidFrom

			DECLARE @isExistTo NUMERIC(20, 2)
			SELECT @isExistTo = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
			FROM dbo.BS_BANK_ACCOUNT
			WHERE GUID = @bankAccountGuidTo

			UPDATE dbo.BS_BANK_ACCOUNT
			SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExistTo + @canSendValue) AS VARCHAR(255)))
			WHERE GUID = @bankAccountGuidTo

			SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
			FROM dbo.BS_BANK_ACCOUNT
			WHERE GUID = @bankAccountGuidFrom

			INSERT INTO dbo.BS_TRANSACTION_INFO (
					[FROM],
					[TO],
					[VALUE],
					CASH_BALANCE
			)
			VALUES (
					ENCRYPTBYKEY(@bsSKeyGuid, @bankAccountGuidFrom),
					ENCRYPTBYKEY(@bsSKeyGuid, @bankAccountGuidTo),
					ENCRYPTBYKEY(@bsSKeyGuid, CAST(@canSendValue AS VARCHAR(255))),
					ENCRYPTBYKEY(@bsSKeyGuid, CAST(@isExist AS VARCHAR(255)))
			)

			EXEC dbo.bs_close_bsSK

			COMMIT TRANSACTION sendMoney;
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH