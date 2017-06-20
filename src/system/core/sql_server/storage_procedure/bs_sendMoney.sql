CREATE PROCEDURE dbo.bs_sendMoney (
		@from VARCHAR(36),
		@fromType VARCHAR(255),
		@to VARCHAR(36),
		@toType VARCHAR(255),
		@value NUMERIC(20, 2)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			DECLARE @typeAccount VARCHAR(255),
							@typeCard VARCHAR(255)
			SELECT  @typeAccount = 'account',
							@typeCard = 'card'

			DECLARE @isExist NUMERIC(20, 2),
							@canSendValue NUMERIC(20, 2),
							@isExistTo NUMERIC(20, 2)

			DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
			SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()

			/* ACCOUNT --> ACCOUNT */
			IF (@fromType = @typeAccount AND @toType = @typeAccount)
					BEGIN
							BEGIN TRANSACTION sendMoney;

							EXEC dbo.bs_open_bsSK

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @from

							SELECT @canSendValue =  CASE
							                        WHEN @value <= 0 THEN 0
							                        WHEN @isExist > 0 AND @value > 0 AND @isExist >= @value THEN @value
							                        ELSE @value - (@value - @isExist)
							                        END
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @from

							UPDATE dbo.BS_BANK_ACCOUNT
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExist - @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @from

							SELECT @isExistTo = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @to

							UPDATE dbo.BS_BANK_ACCOUNT
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExistTo + @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @to

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @to

							INSERT INTO dbo.BS_TRANSACTION_INFO (
								[FROM],
								[TO],
								[VALUE],
								CASH_BALANCE
							)
							VALUES (
								ENCRYPTBYKEY(@bsSKeyGuid, @from),
								ENCRYPTBYKEY(@bsSKeyGuid, @to),
								ENCRYPTBYKEY(@bsSKeyGuid, CAST(@canSendValue AS VARCHAR(255))),
								ENCRYPTBYKEY(@bsSKeyGuid, CAST(@isExist AS VARCHAR(255)))
							)

							EXEC dbo.bs_close_bsSK

							COMMIT TRANSACTION sendMoney;
					END
			/* ACCOUNT --> CARD */
			IF (@fromType = @typeAccount AND @toType = @typeCard)
					BEGIN
							BEGIN TRANSACTION sendMoney;

							EXEC dbo.bs_open_bsSK

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @from

							SELECT @canSendValue =  CASE
							                        WHEN @value <= 0 THEN 0
							                        WHEN @isExist > 0 AND @value > 0 AND @isExist >= @value THEN @value
							                        ELSE @value - (@value - @isExist)
							                        END
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @from

							UPDATE dbo.BS_BANK_ACCOUNT
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExist - @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @from

							SELECT @isExistTo = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_CARD
							WHERE GUID = @to

							UPDATE dbo.BS_CARD
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExistTo + @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @to

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_CARD
							WHERE GUID = @to

							INSERT INTO dbo.BS_TRANSACTION_INFO (
								[FROM],
								[TO],
								[VALUE],
								CASH_BALANCE
							)
							VALUES (
								ENCRYPTBYKEY(@bsSKeyGuid, @from),
								ENCRYPTBYKEY(@bsSKeyGuid, @to),
								ENCRYPTBYKEY(@bsSKeyGuid, CAST(@canSendValue AS VARCHAR(255))),
								ENCRYPTBYKEY(@bsSKeyGuid, CAST(@isExist AS VARCHAR(255)))
							)

							EXEC dbo.bs_close_bsSK

							COMMIT TRANSACTION sendMoney;
					END
			/* CARD --> CARD */
			IF (@fromType = @typeCard AND @toType = @typeCard)
					BEGIN
							BEGIN TRANSACTION sendMoney;

							EXEC dbo.bs_open_bsSK

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_CARD
							WHERE GUID = @from

							SELECT @canSendValue =  CASE
							                        WHEN @value <= 0 THEN 0
							                        WHEN @isExist > 0 AND @value > 0 AND @isExist >= @value THEN @value
							                        ELSE @value - (@value - @isExist)
							                        END
							FROM dbo.BS_CARD
							WHERE GUID = @from

							UPDATE dbo.BS_CARD
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExist - @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @from

							SELECT @isExistTo = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_CARD
							WHERE GUID = @to

							UPDATE dbo.BS_CARD
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExistTo + @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @to

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_CARD
							WHERE GUID = @to

							INSERT INTO dbo.BS_TRANSACTION_INFO (
								[FROM],
								[TO],
								[VALUE],
								CASH_BALANCE
							)
							VALUES (
								ENCRYPTBYKEY(@bsSKeyGuid, @from),
								ENCRYPTBYKEY(@bsSKeyGuid, @to),
								ENCRYPTBYKEY(@bsSKeyGuid, CAST(@canSendValue AS VARCHAR(255))),
								ENCRYPTBYKEY(@bsSKeyGuid, CAST(@isExist AS VARCHAR(255)))
							)

							EXEC dbo.bs_close_bsSK

							COMMIT TRANSACTION sendMoney;
					END
			/* CARD --> ACCOUNT */
			IF (@fromType = @typeCard AND @toType = @typeAccount)
					BEGIN
							BEGIN TRANSACTION sendMoney;

							EXEC dbo.bs_open_bsSK

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_CARD
							WHERE GUID = @from

							SELECT @canSendValue =  CASE
							                        WHEN @value <= 0 THEN 0
							                        WHEN @isExist > 0 AND @value > 0 AND @isExist >= @value THEN @value
							                        ELSE @value - (@value - @isExist)
							                        END
							FROM dbo.BS_CARD
							WHERE GUID = @from

							UPDATE dbo.BS_CARD
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExist - @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @from

							SELECT @isExistTo = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @to

							UPDATE dbo.BS_BANK_ACCOUNT
							SET "VALUE" = ENCRYPTBYKEY(@bsSKeyGuid, CAST((@isExistTo + @canSendValue) AS VARCHAR(255)))
							WHERE GUID = @to

							SELECT @isExist = CAST(CAST(DECRYPTBYKEY("VALUE") AS VARCHAR(255)) AS NUMERIC(20, 2))
							FROM dbo.BS_BANK_ACCOUNT
							WHERE GUID = @to

							INSERT INTO dbo.BS_TRANSACTION_INFO (
									[FROM],
									[TO],
									[VALUE],
									CASH_BALANCE
							)
							VALUES (
									ENCRYPTBYKEY(@bsSKeyGuid, @from),
									ENCRYPTBYKEY(@bsSKeyGuid, @to),
									ENCRYPTBYKEY(@bsSKeyGuid, CAST(@canSendValue AS VARCHAR(255))),
									ENCRYPTBYKEY(@bsSKeyGuid, CAST(@isExist AS VARCHAR(255)))
							)

							EXEC dbo.bs_close_bsSK

							COMMIT TRANSACTION sendMoney;
					END
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH