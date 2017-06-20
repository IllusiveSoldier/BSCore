package system.core;

import system.core.database.BSDb;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс, который позволяет выбирать информацию о финансах пользователя
 */
public class FinanceInfo {
    /**
     * Метод, который возвращает общий денежный остаток на депозитах и картах пользователя
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - Сумма общего остатка на картах и депозитах
     * @throws SQLException
     */
    public static BigDecimal getAllMoneyInAccount(final String userGuid, final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("SUM(\n")
                .append("COALESCE(CAST(CAST(DECRYPTBYKEY(")
                .append("account.[VALUE]) AS VARCHAR(255)) AS NUMERIC(20, 2)), 0.00) + \n")
                .append("COALESCE(CAST(CAST(DECRYPTBYKEY(card.[VALUE]) ")
                .append("AS VARCHAR(255)) AS NUMERIC(20, 2)), 0.00)\n")
                .append(") AS allCashSum\n")
                .append("FROM dbo.BS_USER AS [user]\n")
                .append("LEFT JOIN dbo.BS_BANK_ACCOUNT AS account\n")
                .append("ON DECRYPTBYKEY(account.USER_GUID) = [user].GUID\n")
                .append("AND (account.STATUS = 10 OR account.STATUS IS NULL)\n")
                .append("LEFT JOIN dbo.BS_CARD AS card\n")
                .append("ON card.USER_GUID = [user].GUID\n")
                .append("AND (card.STATUS = 10 OR card.STATUS IS NULL)\n")
                .append("WHERE [user].GUID = ?\n")
                .append("AND ([user].STATUS = 10 OR [user].STATUS IS NULL)");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        final BigDecimal allCashValue = resultSet.next()
                ? resultSet.getBigDecimal(1).setScale(2, BigDecimal.ROUND_DOWN)
                : new BigDecimal(0.00f).setScale(2, BigDecimal.ROUND_DOWN);
        resultSet.close();
        statement.close();

        return allCashValue;
    }

    /**
     * Метод, который возвращает общий денежный остаток на депозитах пользователя
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - Общий денежный остаток на депозитах пользователя
     * @throws SQLException
     */
    public static BigDecimal getAllMoneyInDeposits(final String userGuid, final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("SUM(\n")
                .append("COALESCE(CAST(CAST(DECRYPTBYKEY(account.[VALUE]) AS VARCHAR(255)) AS NUMERIC(20, 2)), 0.00)\n")
                .append(") AS allCashSum\n")
                .append("FROM dbo.BS_USER AS [user]\n")
                .append("LEFT JOIN dbo.BS_BANK_ACCOUNT AS account\n")
                .append("ON DECRYPTBYKEY(account.USER_GUID) = [user].GUID\n")
                .append(" AND (account.STATUS = 10 OR account.STATUS IS NULL)\n")
                .append("WHERE [user].GUID = ?\n")
                .append("AND ([user].STATUS = 10 OR [user].STATUS IS NULL)");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        final BigDecimal allCashValue = resultSet.next()
                ? resultSet.getBigDecimal(1).setScale(2, BigDecimal.ROUND_DOWN)
                : new BigDecimal(0.00f).setScale(2, BigDecimal.ROUND_DOWN);
        resultSet.close();
        statement.close();

        return allCashValue;
    }

    /**
     * Метод, который возвращает общий денежный остаток на картах пользователя
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - Денежный остаток на картах пользователя
     * @throws SQLException
     */
    public static BigDecimal getAllMoneyInCards(final String userGuid, final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("SUM(\n")
                .append("COALESCE(CAST(CAST(DECRYPTBYKEY(card.[VALUE])  AS VARCHAR(255)) AS NUMERIC(20, 2)), 0.00)) AS allCashSum\n\n")
                .append("FROM dbo.BS_USER AS [user]\n")
                .append("LEFT JOIN dbo.BS_CARD AS card\n")
                .append("ON card.USER_GUID = [user].GUID\n")
                .append(" AND (card.STATUS = 10 OR card.STATUS IS NULL)\n")
                .append("WHERE [user].GUID = ?\n")
                .append("AND ([user].STATUS = 10 OR [user].STATUS IS NULL)");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        final ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        final BigDecimal allCashValue = resultSet.next()
                ? resultSet.getBigDecimal(1).setScale(2, BigDecimal.ROUND_DOWN)
                : new BigDecimal(0.00f).setScale(2, BigDecimal.ROUND_DOWN);
        resultSet.close();
        statement.close();

        return allCashValue;
    }
}
