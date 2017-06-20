package system.core;

import system.core.database.BSDb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class BSBankAccount extends BSObject {
    public static final String TYPE = "account";

    /**
     * Глобальный идентификатор пользователя
     */
    private String userGuid;
    /**
     * Дата создания депозита
     */
    private long beginDate;
    /**
     * Дата окончания действия депозита
     */
    private long endDate;
    /**
     * Денежный остаток на депозите
     */
    private byte[] value;
    /**
     * Тип вклада
     */
    private int type;
    /**
     * Направление капитализации
     */
    private String capitalizationGuid;
    /**
     * Направление капиталиации. Тип
     */
    private String to;

    /**
     * Метод, который осуществляет создание банковского депозита.
     * Справка: хранимаю процедура на SQL-Server: dbo.bs_bsBankAccount_createBankAccount
     * @param guid - GUID создаваемого депозита
     * @param userGuid - GUID пользователя, который будет являться держателем депозита
     * @param endDate - Дата окончания действия депозита
     * @param value - Первоначальный размер
     * @param type - Тип депозита
     * @param capitalizationGuid - Направление капитализации
     * @param to - Направление капитализации. Тип
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void createBankAccount(final String guid, final String userGuid, long endDate,
                                         final BigDecimal value, final int type,
                                         final String capitalizationGuid, final String to,
                                         final Connection connection) throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement(
                        "{call dbo.bs_bsBankAccount_createBankAccount(?, ?, ?, ?, ?, ?, ?)}"
                );
        statement.setString(1, guid);
        statement.setString(2, userGuid);
        if (value != null)
            statement.setBigDecimal(3, value.setScale(2, BigDecimal.ROUND_DOWN));
        else statement.setBigDecimal(3, null);
        statement.setInt(4, type);
        statement.setString(5, capitalizationGuid);
        statement.setString(6, to);
        statement.setTimestamp(7, new Timestamp(endDate));
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который возвращает список банковских депозитов для конкретного пользователя
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - список депозитов
     * @throws SQLException
     */
    public static ArrayList<BSBankAccount> getBSBankAccountsAsList(String userGuid,
                                                                   Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("bankAccount.OUID,\n")
                .append("bankAccount.CREATE_DATE,\n")
                .append("bankAccount.CREATOR,\n")
                .append("bankAccount.STATUS,\n")
                .append("bankAccount.GUID,\n")
                .append("CAST(DECRYPTBYKEY(bankAccount.USER_GUID) AS VARCHAR(255)) AS userGuid,\n")
                .append("bankAccount.BEGIN_DATE,\n")
                .append("bankAccount.\"VALUE\",\n")
                .append("bankAccount.\"TYPE\",\n")
                .append("bankAccount.END_DATE\n")
                .append("FROM dbo.BS_BANK_ACCOUNT AS bankAccount\n")
                .append("WHERE CAST(DECRYPTBYKEY(bankAccount.USER_GUID) AS VARCHAR(255)) = ?\n")
                .append("AND (bankAccount.STATUS = 10 OR bankAccount.STATUS IS NULL)");
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        ArrayList<BSBankAccount> bsBankAccounts = new ArrayList<BSBankAccount>();
        while (resultSet.next()) {
            BSBankAccount account = new BSBankAccount();
            account.setOuid(resultSet.getInt(1));
            account.setCreateDate(resultSet.getTimestamp(2).getTime());
            account.setCreator(resultSet.getInt(3));
            account.setStatus(resultSet.getInt(4));
            account.setGuid(resultSet.getString(5));
            account.setUserGuid(resultSet.getString(6));
            account.setBeginDate(resultSet.getTimestamp(7).getTime());
            account.setValue(resultSet.getBytes(8));
            account.setType(resultSet.getInt(9));
            account.setEndDate(resultSet.getTimestamp(10).getTime());
            bsBankAccounts.add(account);
        }
        resultSet.close();
        statement.close();

        return bsBankAccounts;
    }

    /**
     * Метод, который возвращает связку вида GUID депозита --> Остаток на депозите для конкретного
     * пользователя
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @return - связку GUID депозита --> Остаток на депозите
     * @throws SQLException
     */
    public static HashMap<String, String> getGuidWithValueBA(String userGuid, Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("GUID,\n")
                .append("CAST(DECRYPTBYKEY(\"VALUE\") AS VARCHAR(255)) AS \"value\"\n")
                .append("FROM dbo.BS_BANK_ACCOUNT\n")
                .append("WHERE CAST(DECRYPTBYKEY(USER_GUID) AS VARCHAR(255)) = ?\n")
                .append("AND (STATUS = 10 OR STATUS IS NULL)");
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, userGuid);
        BSDb.openSK(connection);
        ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        HashMap<String, String> info = new HashMap<String, String>();
        while (resultSet.next()) {
            final String guid = resultSet.getString(1);
            final String value = resultSet.getString(2);
            info.put(guid, value);
        }
        resultSet.close();
        statement.close();

        return info;
    }

    /**
     * Метод, который возвращает количество депозитов в системе
     * @param connection - Подключение к базе данных
     * @return - количество депозитов в системе
     * @throws SQLException
     */
    public static int getBankAccountCount(Connection connection) throws SQLException {
        CallableStatement statement = connection
                .prepareCall("{? = call dbo.bs_Get_BSBankAccount_count()}");
        statement.registerOutParameter(1, Types.INTEGER);
        statement.execute();
        final int accountsCount = statement.getInt(1);
        statement.close();

        return accountsCount;
    }

    /**
     * Метод, который осуществляет проверку на существование депозита
     * @param accountGuid - GUID депозита
     * @param connection - Подключение к базе данных
     * @return - true - депозит существует, false - нет
     * @throws SQLException
     */
    public static boolean isExistAccount(final String accountGuid, Connection connection)
            throws SQLException {
        CallableStatement statement = connection
                .prepareCall("{? = call dbo.bs_isExistAccount(?)}");
        statement.registerOutParameter(1, Types.BIT);
        statement.setString(2, accountGuid);
        statement.execute();
        boolean isExist = statement.getInt(1) > 0;
        statement.close();

        return isExist;
    }

    /**
     * Метод, который закрывает депозит
     * @param depositGuid - GUID депозита
     * @param connection - Подключение к базе данных
     * @return - true - закрыт, false - нет
     * @throws SQLException
     */
    public static boolean closeDeposit(final String depositGuid, final Connection connection)
            throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsBankAccount_closeDeposit(?)}");
        statement.setString(1, depositGuid);
        final boolean isExecute = statement.execute();
        statement.close();

        return isExecute;
    }

    /**
     * Метод, который возвращает объект данного класса.
     * @param accountGuid - GUID депозита
     * @param connection - Подключение к базе данных
     * @return
     * @throws SQLException
     */
    public static BSBankAccount getBSBankAccountObject(final String accountGuid,
                                                       final Connection connection)
            throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("account.OUID,\n")
                .append("account.CREATE_DATE,\n")
                .append("account.CREATOR,\n")
                .append("account.STATUS,\n")
                .append("account.GUID,\n")
                .append("CAST(DECRYPTBYKEY(account.USER_GUID) AS VARCHAR(255)) AS userGuid,\n")
                .append("account.BEGIN_DATE,\n")
                .append("account.\"VALUE\",\n")
                .append("account.\"TYPE\",\n")
                .append("account.END_DATE,\n")
                .append("account.CAPITALIZATION_GUID,\n")
                .append("account.[TO]\n")
                .append("FROM dbo.BS_BANK_ACCOUNT AS account\n")
                .append("WHERE account.GUID = ?\n")
                .append("AND (account.STATUS = 10 OR account.STATUS IS NULL)");
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setString(1, accountGuid);
        BSDb.openSK(connection);
        ResultSet resultSet = statement.executeQuery();
        BSDb.closeSK(connection);
        BSBankAccount bsBankAccount = new BSBankAccount();
        while (resultSet.next()) {
            bsBankAccount.setOuid(resultSet.getInt(1));
            bsBankAccount.setCreateDate(resultSet.getTimestamp(2).getTime());
            bsBankAccount.setCreator(resultSet.getInt(3));
            bsBankAccount.setStatus(resultSet.getInt(4));
            bsBankAccount.setGuid(resultSet.getString(5));
            bsBankAccount.setUserGuid(resultSet.getString(6));
            bsBankAccount.setBeginDate(resultSet.getTimestamp(7).getTime());
            bsBankAccount.setValue(resultSet.getBytes(8));
            bsBankAccount.setType(resultSet.getInt(9));
            bsBankAccount.setEndDate(resultSet.getTimestamp(10).getTime());
            bsBankAccount.setCapitalizationGuid(resultSet.getString(11));
            bsBankAccount.setTo(resultSet.getString(12));
        }
        resultSet.close();
        statement.close();

        return bsBankAccount;
    }

    public String getUserGuid () {
        return userGuid;
    }

    public void setUserGuid (String userGuid) {
        this.userGuid = userGuid;
    }

    public long getBeginDate () {
        return beginDate;
    }

    public void setBeginDate (long beginDate) {
        this.beginDate = beginDate;
    }

    public byte[] getValue () {
        return value;
    }

    public void setValue (byte[] value) {
        this.value = value;
    }

    public int getType () {
        return type;
    }

    public void setType (int type) {
        this.type = type;
    }

    public long getEndDate () {
        return endDate;
    }

    public void setEndDate (long endDate) {
        this.endDate = endDate;
    }

    public String getCapitalizationGuid () {
        return capitalizationGuid;
    }

    public void setCapitalizationGuid (String capitalizationGuid) {
        this.capitalizationGuid = capitalizationGuid;
    }

    public String getTo () {
        return to;
    }

    public void setTo (String to) {
        this.to = to;
    }
}
