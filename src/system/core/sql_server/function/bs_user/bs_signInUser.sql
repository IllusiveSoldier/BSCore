CREATE FUNCTION dbo.bs_signInUser(@login VARCHAR(255), @eMail VARCHAR(255), @password VARCHAR(255))
	RETURNS VARCHAR(36)
AS
	BEGIN
			DECLARE @userGuid VARCHAR(36)

			SELECT TOP 1 @userGuid = GUID
			FROM dbo.BS_USER
			WHERE (
					ISNULL(STATUS, 10) = 10
					AND (
							DECRYPTBYKEY(LOGIN) = @login
							OR E_MAIL = @eMail
					)
					AND DECRYPTBYKEY(PASSWORD) = @password
			)

			RETURN @userGuid
	END