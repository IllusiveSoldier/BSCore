CREATE PROCEDURE dbo.bs_truncate_table (
		@tableName VARCHAR(255)
)
AS
	SET NOCOUNT ON;
	BEGIN TRY
			DECLARE @sqlScript VARCHAR(255)
			SET @sqlScript = 'IF OBJECT_ID(''' + @tableName + ''') IS NOT NULL TRUNCATE TABLE ' + @tableName
		PRINT @sqlScript
			EXEC (@sqlScript)
	END TRY
	BEGIN CATCH
			IF @@TRANCOUNT > 0
					ROLLBACK

			DECLARE @ErrorMessage nvarchar(4000), @ErrorSeverity int;
			SELECT @ErrorMessage = ERROR_MESSAGE(), @ErrorSeverity = ERROR_SEVERITY();
			RAISERROR(@ErrorMessage, @ErrorSeverity, 1);
	END CATCH