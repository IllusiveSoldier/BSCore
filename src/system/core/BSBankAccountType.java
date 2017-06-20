package system.core;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public class BSBankAccountType extends BSObject {
    /**
     * Наименование
     */
    private String name;
    /**
     * Процент
     */
    private BigDecimal percent;
    /**
     * Продолжительность
     */
    private int duration;

    /**
     * Метод, который добавляет тип банковского депозита
     * @param name - Наименовение типа
     * @param percent - Процент
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void addAccountType(final String name, final BigDecimal percent,
                                      final Connection connection) throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsBankAccountType_addAccountType(?, ?)}");
        statement.setString(1, name);
        statement.setBigDecimal(2, percent);
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который возвращает список всех типов депозитов в системе
     * @param connection - Подключение к базе данных
     * @return - Список всех типов депозитов в системе
     * @throws SQLException
     */
    public static ArrayList<BSBankAccountType> getBankAccountTypes(final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT DISTINCT\n")
                .append("OUID,\n")
                .append("GUID,\n")
                .append("NAME,\n")
                .append("[PERCENT],\n")
                .append("DURATION\n")
                .append("FROM dbo.BS_BANK_ACCOUNT_TYPE");
        final PreparedStatement statement = connection.prepareStatement(sql.toString());
        final ResultSet resultSet = statement.executeQuery();
        final ArrayList<BSBankAccountType> types = new ArrayList<BSBankAccountType>(5);
        while (resultSet.next()) {
            BSBankAccountType accountType = new BSBankAccountType();
            accountType.setOuid(resultSet.getInt(1));
            accountType.setGuid(resultSet.getString(2));
            accountType.setName(resultSet.getString(3));
            accountType.setPercent(resultSet.getBigDecimal(4));
            accountType.setDuration(resultSet.getInt(5));
            types.add(accountType);
        }
        resultSet.close();
        statement.close();

        return types;
    }

    /**
     * Метод, который возвращает тип в виде числовго представления по конкретному GUID типа
     * @param guid - GUID типа депозита
     * @param connection - Подключение к базе данных
     * @return - Числовое предсталение типа
     * @throws SQLException
     */
    public static int getTypeAccountByGuid(final String guid, final Connection connection)
            throws SQLException {
        final CallableStatement statement = connection
                .prepareCall("{? = call dbo.bs_bsBankAccountType_Get_typeAsIntByGuid(?)}");
        statement.registerOutParameter(1, Types.INTEGER);
        statement.setString(2, guid);
        statement.execute();
        final int accountType = statement.getInt(1);
        statement.close();

        return accountType;
    }

    public int getDuration () {
        return duration;
    }

    public void setDuration (int duration) {
        this.duration = duration;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public BigDecimal getPercent () {
        return percent;
    }

    public void setPercent (BigDecimal percent) {
        this.percent = percent;
    }
}
