package system.core;

import system.core.database.BSDb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class BSCard extends BSObject {
    public static final String TYPE = "card";

    /**
     * GUID карты
     */
    private String userGuid;
    /**
     * Денежный остаток на карте
     */
    private BigDecimal value;

    /**
     * Метод, который осуществляет создание банковской карты
     * @param cardGuid - GUID создаваемой карты
     * @param userGuid - GUID пользователя, создающего карты
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void createCard(final String cardGuid,  final String userGuid,
                                  final Connection connection) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsCard_addCard(?, ?)}");
        statement.setString(1, cardGuid);
        statement.setString(2, userGuid);
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который возвращает список банковских карт для конкретного пользователя
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - список банковских карт
     * @throws SQLException
     */
    public static ArrayList<BSCard> getBsCardsPerUsser(final String userGuid,
                                                       final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT DISTINCT\n")
                .append("bsCard.GUID,\n")
                .append("CAST(DECRYPTBYKEY(bsCard.VALUE) AS VARCHAR(255)) AS [value]\n")
                .append("FROM dbo.BS_USER AS bsUser\n")
                .append("INNER JOIN BS_CARD AS bsCard\n")
                .append("ON bsCard.USER_GUID = bsUser.GUID\n")
                .append("AND (bsCard.STATUS = 10 OR bsCard.STATUS IS NULL)")
                .append("WHERE bsUser.GUID = ?\n")
                .append("AND (bsUser.STATUS = 10 OR bsUser.STATUS IS NULL)");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        ArrayList<BSCard> bsCards = new ArrayList<BSCard>(5);
        while (resultSet.next()) {
            BSCard bsCard = new BSCard();
            if (resultSet.getString(1) != null) {
                final String guid = resultSet.getString(1);
                bsCard.setGuid(guid);
            }
            if (resultSet.getString(2) != null) {
                final String value = resultSet.getString(2);
                bsCard.setValue(new BigDecimal(value).setScale(2, BigDecimal.ROUND_DOWN));
            }
            bsCards.add(bsCard);
        }
        resultSet.close();
        statement.close();

        return bsCards;
    }

    /**
     * Метод, который возвращает связку типа GUID карты --> Остаток на карте для конкретного
     * пользователя
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - вязку типа GUID карты --> Остаток на карте
     * @throws SQLException
     */
    public static HashMap<String, String> getGuidWithValueCard(final String userGuid,
                                                               final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("card.GUID,\n")
                .append("CAST(DECRYPTBYKEY(card.VALUE) AS VARCHAR(255)) AS [value]\n")
                .append("FROM dbo.BS_USER AS bsUser\n")
                .append("INNER JOIN dbo.BS_CARD AS card\n")
                .append("ON card.USER_GUID = bsUser.GUID\n")
                .append("AND (card.STATUS = 10 OR card.STATUS IS NULL)\n")
                .append("WHERE bsUser.GUID = ?\n")
                .append("AND (bsUser.STATUS = 10 OR bsUser.STATUS IS NULL)");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        HashMap<String, String> guidWithValueCard = new HashMap<String, String>();
        while (resultSet.next()) {
            final String guid = resultSet.getString(1);
            final String value = resultSet.getString(2);
            guidWithValueCard.put(guid, value);
        }
        resultSet.close();
        statement.close();

        return guidWithValueCard;
    }

    /**
     * Метод, который возвращает банковскую карту для конкретного пользователя
     * @param cardGuid - GUID карты
     * @param connection - Подключение к базе данных
     * @return - Банковскую карту
     * @throws SQLException
     */
    public static BSCard getBSCardObject(final String cardGuid, final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("card.OUID,\n")
                .append("card.CREATE_DATE,\n")
                .append("card.CREATOR,\n")
                .append("card.STATUS,\n")
                .append("card.GUID,\n")
                .append("card.USER_GUID,\n")
                .append("CAST(DECRYPTBYKEY(card.[VALUE]) AS VARCHAR(255)) AS [value]\n")
                .append("FROM dbo.BS_CARD AS card\n")
                .append("WHERE card.GUID = ?\n")
                .append("AND (card.STATUS = 10 OR card.STATUS IS NULL)");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, cardGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        BSCard bsCard = new BSCard();
        while (resultSet.next()) {
            bsCard.setOuid(resultSet.getInt(1));
            bsCard.setCreateDate(resultSet.getTimestamp(2).getTime());
            bsCard.setCreator(resultSet.getInt(3));
            bsCard.setStatus(resultSet.getInt(4));
            bsCard.setGuid(resultSet.getString(5));
            bsCard.setUserGuid(resultSet.getString(6));
            final String valueAsString = resultSet.getString(7);
            bsCard.setValue(new BigDecimal(valueAsString).setScale(2, BigDecimal.ROUND_DOWN));
        }
        resultSet.close();
        statement.close();

        return bsCard;
    }

    /**
     * Метод, который закрывает банковскую карту.
     * @param cardGuid - GUID карты
     * @param connection - Подключение к базе данных
     * @return - true - если первый результат ResultSet
     * @throws SQLException
     */
    public static boolean closeCard(final String cardGuid, final Connection connection)
            throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsCard_closeCard(?)}");
        statement.setString(1, cardGuid);
        final boolean isExecute = statement.execute();
        statement.close();

        return isExecute;
    }

    /**
     * Возвращает количество карт
     * @param connection
     * @return
     * @throws SQLException
     */
    public static int getCardsCount(final Connection connection) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT\n")
                .append("COUNT(card.OUID) AS cardsOuid\n")
                .append("FROM dbo.BS_CARD AS card\n")
                .append("WHERE card.STATUS = 10 OR card.STATUS IS NULL");
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(sql.toString());
        int cardsCount = 0;
        if (resultSet.next())
            cardsCount = resultSet.getInt(1);
        resultSet.close();
        statement.close();

        return cardsCount;
    }

    public static boolean isExistCard(final String cardGuid, final Connection connection)
            throws SQLException {
        CallableStatement statement = connection.prepareCall("{? = call dbo.bs_isExistCard(?)}");
        statement.registerOutParameter(1, Types.BIT);
        statement.setString(2, cardGuid);
        statement.execute();
        boolean isExist = statement.getInt(1) > 0;
        statement.close();

        return isExist;
    }

    public String getUserGuid () {
        return userGuid;
    }

    public void setUserGuid (String userGuid) {
        this.userGuid = userGuid;
    }

    public BigDecimal getValue () {
        return value;
    }

    public void setValue (BigDecimal value) {
        this.value = value;
    }
}
