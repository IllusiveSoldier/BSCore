CREATE FUNCTION dbo.bs_Get_BSBankAccount_count()
RETURNS INT
AS
	BEGIN
		DECLARE @bankAccountCount INT
		SELECT DISTINCT @bankAccountCount = COUNT(account.OUID)
		FROM dbo.BS_BANK_ACCOUNT AS account
		WHERE account.STATUS = 10 OR account.STATUS IS NULL

		RETURN @bankAccountCount
	END