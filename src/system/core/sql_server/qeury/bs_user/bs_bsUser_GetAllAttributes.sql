CREATE FUNCTION dbo.bs_bsUser_GetAllAttributes(@userGuid VARCHAR(36))
	RETURNS TABLE
AS
	RETURN (
			SELECT TOP 1
					OUID,
					CREATE_DATE,
					CREATOR,
					STATUS,
					GUID,
					LOGIN,
					PASSWORD,
					FIRST_NAME,
					LAST_NAME,
					SECOND_NAME,
					BIRTHDATE,
					E_MAIL
			FROM dbo.BS_USER
			WHERE (
					ISNULL(STATUS, 10) = 10
					AND GUID = @userGuid
			)
	);