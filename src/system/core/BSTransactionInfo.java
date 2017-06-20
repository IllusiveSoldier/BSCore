package system.core;

import system.core.database.BSDb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;

/**
 * Класс, который позволяет манипулировать информацией о транзакциях
 */
public class BSTransactionInfo extends BSObject {
    /**
     * Откуда была осуществлена транзакция
     */
    private String from;
    /**
     * В отношении кого была осуществлена транзакция
     */
    private String to;
    /**
     * Сколько денежных средств было передано
     */
    private BigDecimal value;
    /**
     * Денжный остаток у инициатора транзакции после её завершения
     */
    private BigDecimal cashBalance;

    /**
     * Метод, который возвращает связку типа GUID банковского депозита --> Количество транзакций,
     * в котором он учавствовал
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - связку типа GUID банковского депозита --> Количество транзакций,
     * в котором он учавствовал
     * @throws SQLException
     */
    public static HashMap<String, Integer> getGuidWithTransactionCountBA(final String userGuid,
                                                                        final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("account.GUID,\n")
                .append("COUNT(transactionInfo.OUID) AS transactionCount\n")
                .append("FROM dbo.BS_USER AS bsUser\n")
                .append("INNER JOIN dbo.BS_BANK_ACCOUNT AS account\n")
                .append("ON DECRYPTBYKEY(account.USER_GUID) = bsUser.GUID\n")
                .append("AND (account.STATUS = 10 OR account.STATUS IS NULL)\n")
                .append("INNER JOIN dbo.BS_TRANSACTION_INFO AS transactionInfo\n")
                .append("ON DECRYPTBYKEY(transactionInfo.[FROM]) = account.GUID\n")
                .append("OR DECRYPTBYKEY(transactionInfo.[TO]) = account.GUID\n")
                .append("AND (transactionInfo.STATUS = 10 OR transactionInfo.STATUS IS NULL)")
                .append("WHERE bsUser.GUID = ?\n")
                .append("GROUP BY account.GUID");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        HashMap<String, Integer> info = new HashMap<String, Integer>();
        while (resultSet.next()) {
            final String accountGuid = resultSet.getString(1);
            final Integer transactionCount = resultSet.getInt(2);
            info.put(accountGuid, transactionCount);
        }
        resultSet.close();
        statement.close();

        return info;
    }

    /**
     * Метод, который возвращает связку типа GUID банковского депозита --> Дата и денежный
     * остаток в истории транзацкии, относительно этого банковского депозита
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - связку типа GUID банковского депозита --> Дата и денежный
     * остаток в истории транзацкии, относительно этого банковского депозита
     * @throws SQLException
     */
    public static HashMap<String, HashMap<String, String>> getGuidWithDateAndValueBA(
            final String userGuid, final Connection connection) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT\n" + "\t\taccount.GUID\n" + "FROM dbo.BS_BANK_ACCOUNT AS account\n"
                        + "WHERE CAST(DECRYPTBYKEY(account.USER_GUID) AS VARCHAR(255)) = ?\n"
                        + "\tAND (account.STATUS = 10 OR account.STATUS IS NULL)"
        );
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        ResultSet resultSet = statement.executeQuery();
        HashMap<String, HashMap<String, String>> map = new HashMap<>();
        while (resultSet.next()) {
            final String accountGuid = resultSet.getString(1);
            PreparedStatement statementAccount = connection.prepareStatement(
                    "SELECT \n" + "\t\ttInfo.CREATE_DATE,\n"
                            + "\t\tCAST(DECRYPTBYKEY(tInfo.CASH_BALANCE) AS VARCHAR(255)) AS cash\n"
                            + "FROM dbo.BS_TRANSACTION_INFO AS tInfo\n"
                            + "WHERE CAST(DECRYPTBYKEY(tInfo.[FROM]) AS VARCHAR(255)) = ? OR CAST(DECRYPTBYKEY(tInfo.[TO]) AS VARCHAR(255)) = ?\n"
                            + "\tAND (tInfo.STATUS = 10 OR tInfo.STATUS IS NULL)"
            );
            statementAccount.setString(1, accountGuid);
            statementAccount.setString(2, accountGuid);
            ResultSet rsAccount = statementAccount.executeQuery();
            HashMap<String, String> dateWithGuid = new HashMap<>();
            while (rsAccount.next()) {
                dateWithGuid.put(rsAccount.getString(1), rsAccount.getString(2));
            }
            rsAccount.close();
            map.put(accountGuid, dateWithGuid);
        }
        statement = connection.prepareStatement(
                "SELECT DISTINCT\n" + "\tcard.GUID\n" + "FROM dbo.BS_CARD AS card\n"
                        + "WHERE CAST(DECRYPTBYKEY(card.USER_GUID) AS VARCHAR(255)) = ?\n"
                        + "\tAND (card.STATUS = 10 OR card.STATUS IS NULL)"
        );
        statement.setString(1, userGuid);
        resultSet = statement.executeQuery();
        while (resultSet.next()) {
            final String cardGuid = resultSet.getString(1);
            PreparedStatement statementCard = connection.prepareStatement(
                    "SELECT \n" + "\t\ttInfo.CREATE_DATE,\n"
                            + "\t\tCAST(DECRYPTBYKEY(tInfo.CASH_BALANCE) AS VARCHAR(255)) AS cash\n"
                            + "FROM dbo.BS_TRANSACTION_INFO AS tInfo\n"
                            + "WHERE CAST(DECRYPTBYKEY(tInfo.[FROM]) AS VARCHAR(255)) = ? OR CAST(DECRYPTBYKEY(tInfo.[TO]) AS VARCHAR(255)) = ?\n"
                            + "\tAND (tInfo.STATUS = 10 OR tInfo.STATUS IS NULL)"
            );
            statementCard.setString(1, cardGuid);
            statementCard.setString(2, cardGuid);
            ResultSet rsCard = statementCard.executeQuery();
            HashMap<String, String> dateWithGuid = new HashMap<>();
            while (rsCard.next()) {
                dateWithGuid.put(rsCard.getString(1), rsCard.getString(2));
            }
            rsCard.close();
            map.put(cardGuid, dateWithGuid);
        }
        resultSet.close();
        statement.close();
        BSDb.closeSK(connection);

        return map;
    }

    /**
     * Возвращает количество транзакций
     * @param connection
     * @return
     * @throws SQLException
     */
    public static int getTranscationsCount(final Connection connection) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT\n")
                .append("COUNT(transactionInfo.OUID) AS transactionsCount\n")
                .append("FROM dbo.BS_TRANSACTION_INFO AS transactionInfo\n")
                .append("WHERE transactionInfo.STATUS = 10 OR transactionInfo.STATUS IS NULL");
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(sql.toString());
        int transactionCount = 0;
        if (resultSet.next())
            transactionCount = resultSet.getInt(1);
        resultSet.close();
        statement.close();

        return transactionCount;
    }

    public String getFrom () {
        return from;
    }

    public void setFrom (String from) {
        this.from = from;
    }

    public String getTo () {
        return to;
    }

    public void setTo (String to) {
        this.to = to;
    }

    public BigDecimal getValue () {
        return value;
    }

    public void setValue (BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getCashBalance () {
        return cashBalance;
    }

    public void setCashBalance (BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }
}
