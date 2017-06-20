CREATE FUNCTION dbo.bs_bsBankAccountType_Get_typeAsIntByGuid(@guid VARCHAR(36))
RETURNS INT
AS
	BEGIN
		DECLARE @accountType INT
		SELECT TOP 1 @accountType = OUID
		FROM dbo.BS_BANK_ACCOUNT_TYPE AS accountType
		WHERE accountType.GUID = @guid AND (accountType.STATUS = 10 OR accountType.STATUS IS NULL)

		RETURN @accountType
	END