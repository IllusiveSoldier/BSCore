CREATE FUNCTION dbo.bs_isExistUser(@login VARCHAR(255), @eMail VARCHAR(255))
	RETURNS BIT
AS
	BEGIN
			DECLARE @isExistUser BIT

			SELECT @isExistUser = COUNT(OUID)
			FROM dbo.BS_USER
			WHERE (
					ISNULL(STATUS, 10) = 10
					AND (
						DECRYPTBYKEY(LOGIN) = @login
						OR E_MAIL = ISNULL(@eMail, '')
					)
			)

		RETURN @isExistUser
	END