package system.core.database;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * Класс, организующий взаимодействие с базой данных.
 */
public class BSDb {
    /**
     * Метод, который позволяет сформировать подключение к базе данных.
     * @param userName - Логин пользовтаеля SQL Server
     * @param userPassword - Пароль пользователя SQL Server
     * @param serverName - URI к SQL Server
     * @param portNumber - Номер порта для подключения к SQL Server
     * @param databaseName - Наименование базы данных
     * @return
     * @throws SQLServerException
     */
    public static Connection getConnection(final String userName, final String userPassword,
                                           final String serverName, final int portNumber,
                                           final String databaseName) throws SQLServerException {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser(userName);
        ds.setPassword(userPassword);
        ds.setServerName(serverName);
        ds.setPortNumber(portNumber);
        ds.setDatabaseName(databaseName);

        return ds.getConnection();
    }

    /**
     * Метод, который позволяет сформировать подключение к базе данных используя конфигурационный
     * файл. Файл содерждит в себе xml-разметку, в которой располагается вся информация для
     * подклчюения.
     * @param configFile - Файл с конфигом
     * @return - Подключение к базе данных
     */
    public static Connection getConnectionFromConfigurationFile(final File configFile)
            throws SAXException, ParserConfigurationException, IOException, SQLServerException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        BSDatabaseConnectionConfigFileParse configFileParse =
                new BSDatabaseConnectionConfigFileParse();

        if (configFile != null)
            parser.parse(configFile, configFileParse);
        else parser.parse(new File("config/connectionConfig.xml"), configFileParse);

        SQLServerDataSource dataSource = configFileParse.getDs();

        return dataSource.getConnection();
    }

    /**
     * Метод, который вызывает хранимаую процедуру на SQL Server, которая в свою очередь открывает
     * симметричный ключ.
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void openSK(Connection connection) throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_open_bsSK()}");
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который вызывает хранимаую процедуру на SQL Server, которая в свою очередь закрывает
     * симметричный ключ.
     * @param connection
     * @throws SQLException
     */
    public static void closeSK(Connection connection) throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_close_bsSK()}");
        statement.execute();
        statement.close();
    }

    public static String decryptBytesBySK(byte[] targetBytes, Connection connection)
            throws SQLException
    {
        final CallableStatement statement = connection
                .prepareCall("{? = call dbo.bs_Get_decrypted_string_bySK(?)}");
        statement.registerOutParameter(1, Types.VARCHAR);
        statement.setBytes(2, targetBytes);
        BSDb.openSK(connection);
        statement.execute();
        BSDb.closeSK(connection);
        final String decryptString = statement.getString(1);
        statement.close();

        return decryptString;
    }
}
