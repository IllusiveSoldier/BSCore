CREATE FUNCTION dbo.bs_Get_userAvatar(@userGuid VARCHAR(36))
	RETURNS VARBINARY(MAX)
AS
	BEGIN
			DECLARE @userAvatar VARBINARY(MAX)

			SET @userAvatar = (
					SELECT TOP 1
						AVATAR
					FROM dbo.BS_USER
					WHERE (
						GUID = @userGuid
						AND ISNULL(STATUS, 10) = 10
					)
			)

		RETURN @userAvatar
	END