CREATE FUNCTION dbo.bs_isExistAccount(@guid VARCHAR(36))
			RETURNS BIT
AS
BEGIN
	DECLARE @isExist BIT
	SELECT @isExist = COUNT(account.OUID)
	FROM dbo.BS_BANK_ACCOUNT AS account
	WHERE account.GUID = @guid AND (account.STATUS = 10 OR account.STATUS IS NULL)

	RETURN @isExist
END