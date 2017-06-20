package system.core;

import system.core.database.BSDb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Класс, который позволяет оперировать пользовательскими сессиями
 */
public class BSSession extends BSObject {
    /**
     * Глобальный идентификатор сесии
     */
    private String guid;
    /**
     * Глобальный идентификатор пользователя
     */
    private String userGuid;
    /**
     * Дата закрытия сессии
     */
    private long outDate;
    /**
     * Продолжительность сессии
     */
    private long duration;
    /**
     * Дата создания сессии
     */
    private long createDate;
    /**
     * IP-адрес клиента, который осуществил вход в систему
     */
    private String ipAddress;

    public BSSession (String guid, String userGuid, long outDate) {
        this.guid = guid;
        this.userGuid = userGuid;
        this.outDate = outDate;
    }

    public BSSession() {}

    /**
     * Метод, который открывает сессию для пользователя. Заносит запись в таблицу.
     * @param creator - Идентификатор пользователя, который является создателем сессии
     * @param userGuid - Глобальный идентификатор пользователя, который являкися создателем сессии.
     * @param connection - Подключение к базе данных
     * @return - Возврашает глобальный идентификатор сессии
     * @throws SQLException
     */
    public static String openSession(int creator, final String userGuid, final String ipAddress,
                                     final Connection connection) throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsSessionInsert(?, ?, ?, ?)}");
        statement.setInt(1, creator);
        final String sessionGuid = UUID.randomUUID().toString();
        statement.setString(2, sessionGuid);
        statement.setString(3, userGuid);
        statement.setString(4, ipAddress);
        statement.execute();
        statement.close();

        return sessionGuid;
    }

    /**
     * Метод, который закрыввает сесиию для текущего пользователя.
     * @param userGuid - Глобальный идентификатор пользователя, который является создателем сессии.
     * @param outDate - Дата закрытия сессии.
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void closeSession(final String userGuid, long outDate, final String sessionGuid,
                                    final Connection connection) throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsSession_updateOutTime(?, ?, ?)}");
        statement.setTimestamp(1, new Timestamp(outDate));
        statement.setString(2, userGuid);
        statement.setString(3, sessionGuid);
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который возвращает список всех сессий для конкретного пользователя.
     * @param userGuid - Глобальный идентификатор пользователя
     * @return - Список всех сессий для конкретного пользователя.
     * @throws SQLException
     */
    public static ArrayList<BSSession> getBSSessionList(final String userGuid,
                                                        final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("CREATE_DATE,\n")
                .append("GUID,\n")
                .append("OUT_TIME,\n")
                .append("DATEDIFF(SECOND, CREATE_DATE, OUT_TIME),\n")
                .append("CONVERT(VARCHAR(255), DECRYPTBYKEY(IP_ADDRESS))\n")
                .append("FROM dbo.BS_SESSION\n")
                .append("WHERE (\n")
                .append("ISNULL(STATUS, 10) = 10\n")
                .append("AND CONVERT(VARCHAR(36), DECRYPTBYKEY(USER_GUID)) = ?)");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        ArrayList<BSSession> sessions = new ArrayList<BSSession>();
        while (resultSet.next()) {
            BSSession bsSession = new BSSession();
            bsSession.setCreateDate(resultSet.getTimestamp(1).getTime());
            bsSession.setGuid(resultSet.getString(2));
            if (resultSet.getTimestamp(3) != null) {
                bsSession.setOutDate(resultSet.getTimestamp(3).getTime());
            }
            bsSession.setDuration(resultSet.getLong(4));
            if (resultSet.getString(5) != null) {
                bsSession.setIpAddress(resultSet.getString(5));
            }
            sessions.add(bsSession);
        }
        resultSet.close();
        statement.close();

        return sessions;
    }

    /**
     * Метод, который возвращает IP-адрес компьютера, с которого был осуществлен вход в систему
     * @return - IP-адрес клиента
     */
    public static String getComputerIpAddress() {
        try {
            URL checkIp = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    checkIp.openStream()));

            return in.readLine();
        } catch (Exception e) {
            return "Не удалось определить IP-адрес клиента";
        }
    }

    /**
     * Возвращает кол-во активных сессий
     * @param connection
     * @return
     * @throws SQLException
     */
    public static int getActiveSessionsCount(final Connection connection) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT\n")
                .append("COUNT([session].OUID) AS activeSessionsCount\n")
                .append("FROM dbo.BS_SESSION AS [session]\n")
                .append("WHERE [session].OUT_TIME IS NULL\n")
                .append("AND ([session].STATUS = 10 OR [session].STATUS IS NULL)");
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(sql.toString());
        int activeSessionsCount = 0;
        if (resultSet.next())
            activeSessionsCount = resultSet.getInt(1);
        resultSet.close();
        statement.close();

        return activeSessionsCount;
    }

    public String getUserGuid () {
        return userGuid;
    }

    public void setUserGuid (String userGuid) {
        this.userGuid = userGuid;
    }

    public long getOutDate () {
        return outDate;
    }

    public void setOutDate (long outDate) {
        this.outDate = outDate;
    }

    public String getGuid () {
        return guid;
    }

    public void setGuid (String guid) {
        this.guid = guid;
    }

    public long getDuration () {
        return duration;
    }

    public void setDuration (long duration) {
        this.duration = duration;
    }

    public long getCreateDate () {
        return createDate;
    }

    public void setCreateDate (long createDate) {
        this.createDate = createDate;
    }

    public String getIpAddress () {
        return ipAddress;
    }

    public void setIpAddress (String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
