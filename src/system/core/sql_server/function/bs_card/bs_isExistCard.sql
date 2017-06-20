CREATE FUNCTION dbo.bs_isExistCard(@cardGuid VARCHAR(36))
RETURNS BIT
AS
	BEGIN
			DECLARE @isExist BIT

			SELECT @isExist = COUNT(card.OUID)
			FROM dbo.BS_CARD AS card
			WHERE card.GUID = @cardGuid
			AND (card.STATUS = 10 OR card.STATUS IS NULL)

			RETURN @isExist
	END