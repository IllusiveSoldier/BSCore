CREATE PROCEDURE dbo.bs_bankAccount_percent
AS
SET NOCOUNT ON;
		BEGIN TRY
				DECLARE @bsSKeyGuid UNIQUEIDENTIFIER
				SELECT @bsSKeyGuid = dbo.bs_Get_bsSK_guid()

				EXEC dbo.bs_open_bsSK
				;WITH decryptBankAccounts (accountOuid, accountValue) AS (
						SELECT
								account.OUID,
								CAST(
										CAST(DECRYPTBYKEY(account.[VALUE]) AS VARCHAR(255)) AS NUMERIC(20, 2)
								)
						FROM dbo.BS_BANK_ACCOUNT AS account
						WHERE account.[TO] = 'account'
				      AND (account.STATUS = 10 OR account.STATUS IS NULL)
				)

				UPDATE account
				SET account.[VALUE] =
						ENCRYPTBYKEY(
								@bsSKeyGuid,
								CAST(
										decryptBankAccounts.accountValue
										+ ((decryptBankAccounts.accountValue * 0.01) * accountType.[PERCENT]) AS VARCHAR(255)
								)
						)
				FROM decryptBankAccounts
						INNER JOIN dbo.BS_BANK_ACCOUNT AS account
								ON account.OUID = decryptBankAccounts.accountOuid
						INNER JOIN dbo.BS_BANK_ACCOUNT_TYPE AS accountType
								ON accountType.OUID = account.TYPE
						   AND (accountType.STATUS = 10 OR accountType.STATUS IS NULL)
				EXEC dbo.bs_close_bsSK

				EXEC dbo.bs_open_bsSK
				;WITH guidCardWithAccount (cardGuid, accountOuid, cardValue, accountValue) AS (
						SELECT
								account.CAPITALIZATION_GUID,
								account.OUID,
								CAST(
										CAST(DECRYPTBYKEY(card.[VALUE]) AS VARCHAR(255)) AS NUMERIC(20, 2)
								),
								CAST(
										CAST(DECRYPTBYKEY(account.[VALUE]) AS VARCHAR(255)) AS NUMERIC(20, 2)
								)
						FROM dbo.BS_BANK_ACCOUNT AS account
								INNER JOIN dbo.BS_CARD AS card
										ON card.GUID = account.CAPITALIZATION_GUID
						WHERE account.[TO] = 'card'
				)

				UPDATE dbo.BS_CARD
				SET dbo.BS_CARD.[VALUE] =
				ENCRYPTBYKEY(
						@bsSKeyGuid,
						CAST(
								guidCardWithAccount.cardValue
								+ ((guidCardWithAccount.accountValue * 0.01) * accountType.[PERCENT]) AS VARCHAR(255)
						)
				)
				FROM guidCardWithAccount
						INNER JOIN dbo.BS_BANK_ACCOUNT AS account
								ON account.OUID = guidCardWithAccount.accountOuid
						INNER JOIN dbo.BS_BANK_ACCOUNT_TYPE AS accountType
								ON accountType.OUID = account.TYPE
					      AND (accountType.STATUS = 10 OR accountType.STATUS IS NULL)
				WHERE dbo.BS_CARD.GUID = guidCardWithAccount.cardGuid
				EXEC dbo.bs_close_bsSK
		END TRY
		BEGIN CATCH
				IF @@TRANCOUNT > 0
					ROLLBACK

				DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
				SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
				RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
		END CATCH