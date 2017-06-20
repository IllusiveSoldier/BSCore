package system.core;

import system.core.database.BSDb;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Класс, позволяющий взаимодействовать с пользователями в системе
 */
public class BSUser extends BSObject {
    public static final int ADMIN_OUID = 1;

    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private String secondName;
    private long birthDate;
    private String eMail;
    private byte[] avatar;

    public BSUser (String login, String password, String firstName, String lastName,
                   String secondName, long birthDate, String eMail) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.secondName = secondName;
        this.birthDate = birthDate;
        this.eMail = eMail;
    }
    public BSUser() {}

    /**
     * Метод, который проверяет, существует ли пользователь в системе.
     * Проверка осуществляется по логину/e-mail или логину И e-mail.
     *
     * Справвка: функция на SQL Server, отвечающая за проверку называется "bs_isExistUser".
     *
     * @param login - Логин пользователя
     * @param email - Адрес электронной почты пользователя
     * @param connection - Подключение к базе данных
     * @return - Возвращает true, если пользовтаель существует в системе, false - если не существует.
     */
    public static boolean isExistUser(String login, String email, Connection connection)
            throws SQLException {
        CallableStatement statement = connection
                .prepareCall("{? = call dbo.bs_isExistUser(?, ?)}");
        statement.registerOutParameter(1, Types.BIT);
        statement.setString(2, login);
        statement.setString(3, email);
        BSDb.openSK(connection);
        statement.execute();
        BSDb.closeSK(connection);
        boolean isExist = statement.getInt(1) > 0;
        statement.close();

        return isExist;
    }

    /**
     * Метод, который регистрирует пользователя в системе.
     *
     * Справка: функция на SQL Server, отвечающая за проведения процесса регистрации пользователя
     * в системе называется "bs_bsUserInsert".
     *
     * @param creator - Идентификатор пользователя, который яляется инициатором создания записи
     * @param login - Логин пользователя
     * @param password - Пароль пользователя
     * @param firstName - Имя пользователя
     * @param lastName - Фамилия пользователя
     * @param secondName - Отчество пользователя
     * @param birthDate - Дата рождения пользователя
     * @param email - Адрес электронной почты пользователя
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void signUp(int creator, String login, String password, String firstName,
                              String lastName, String secondName, long birthDate, String email,
                              Connection connection) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsUserInsert(?, ?, ?, ?, ?, ?, ?, ?)}");
        statement.setInt(1, creator);
        statement.setString(2, login);
        statement.setString(3, password);
        statement.setString(4, firstName);
        statement.setString(5, lastName);
        statement.setString(6, secondName);
        statement.setTimestamp(7, new Timestamp(birthDate));
        statement.setString(8, email);
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который отвечает за процесс аутентификации пользователя в системе.
     *
     * Справка: функция на SQL Server, отвечающая за проведения процесса аутентификации называется
     * "bs_signInUser".
     *
     * @param login - Логин пользователя
     * @param email - Адрес электронной почты пользователя
     * @param password - Пароль пользователя
     * @param connection - Подключение к базе данных
     * @return - Возвращает GUID пользователя при успешном процессе аутентификации
     * @throws SQLException
     */
    public static String signIn(String login, String email, String password, Connection connection)
            throws SQLException {
        CallableStatement statement = connection
                .prepareCall("{? = call dbo.bs_signInUser(?, ?, ?)}");
        statement.registerOutParameter(1, Types.VARCHAR);
        statement.setString(2, login);
        statement.setString(3, email);
        statement.setString(4, password);
        BSDb.openSK(connection);
        statement.execute();
        BSDb.closeSK(connection);
        String userGuid = statement.getString(1);
        statement.close();

        return userGuid;
    }

    /**
     * Метод, который удаляет профиль пользователя из системыю
     *
     * Справка: функция на SQL Server, отвечающая за проведения процесса удаления профиля
     * пользователя из системы называется "bs_updateUserStatusDelete".
     *
     * @param login - Логин пользователя
     * @param email - Адрес электронной почты пользователя
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void deleteAccount(String login, String email, Connection connection)
            throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_updateUserStatusDelete(?, ?)}");
        statement.setString(1, login);
        statement.setString(2, email);
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который проверяет, что логин пользователя состоит из латинских символов и некоторых
     * спец знаков.
     * @param userLogin - Логин пользователя
     * @return - true - Логин прошёл валидацию, false - нет
     */
    public static boolean isLatinCharactersInLogin(String userLogin) {
        return userLogin != null && !userLogin.isEmpty() && userLogin.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Метод, который проверяет адрес электронной почты пользователя на валидность
     * @param userEmail - Адрес электронной почты пользователя
     * @return - true - Адрес прошёл валидацию, false - нет
     */
    public static boolean isCorrectEmailAddress(String userEmail) {
        return userEmail != null && !userEmail.isEmpty()
                && userEmail.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    }

    /**
     * Метод, который возврвщает экземпляр класса "Пользователь".
     * @param userGuid - Глобальный идентфиикатор пользователя.
     * @param connection - Подключение к базе данных
     * @return - Экземпляр класса "Пользователь"
     * @throws SQLException
     */
    public static BSUser getBSUser(String userGuid, Connection connection) throws SQLException {
        final BSUser bsUser = new BSUser();
        if (userGuid != null && !userGuid.isEmpty()) {
            final StringBuilder sql = new StringBuilder("SELECT TOP 1\n")
                    .append("OUID,\n")
                    .append("CREATE_DATE,\n")
                    .append("CREATOR,\n")
                    .append("STATUS,\n")
                    .append("GUID,\n")
                    .append("CONVERT(VARCHAR(255), DECRYPTBYKEY(LOGIN)),\n")
                    .append("PASSWORD,\n")
                    .append("FIRST_NAME,\n")
                    .append("LAST_NAME,\n")
                    .append("SECOND_NAME,\n")
                    .append("BIRTHDATE,\n")
                    .append("E_MAIL,\n")
                    .append("AVATAR\n")
                    .append("FROM dbo.BS_USER\n")
                    .append("WHERE (\n")
                    .append("ISNULL(STATUS, 10) = 10\n")
                    .append("AND GUID = ?)");
            final PreparedStatement statement = connection.prepareStatement(sql.toString());
            statement.setString(1, userGuid);
            BSDb.openSK(connection);
            final ResultSet resultSet = statement.executeQuery();
            BSDb.closeSK(connection);
            while (resultSet.next()) {
                bsUser.setOuid(resultSet.getInt(1));
                bsUser.setCreateDate(resultSet.getTimestamp(2).getTime());
                bsUser.setCreator(resultSet.getInt(3));
                bsUser.setStatus(resultSet.getInt(4));
                bsUser.setGuid(resultSet.getString(5));
                bsUser.setLogin(resultSet.getString(6));
                bsUser.setFirstName(resultSet.getString(8));
                bsUser.setLastName(resultSet.getString(9));
                bsUser.setSecondName(resultSet.getString(10));
                bsUser.setBirthDate(resultSet.getTimestamp(11).getTime());
                bsUser.seteMail(resultSet.getString(12));
                bsUser.setAvatar(resultSet.getBytes(13));
            }
            resultSet.close();
            statement.close();
        }

        return bsUser;
    }

    /**
     * Метод, который меняет пароль к аккаунту.
     * @param userGuid - Глобальынй идентификатор пользователя
     * @param newUserPassword - Новый пароль пользователя
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void changePassword(String userGuid, String newUserPassword,
                                      Connection connection) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsUser_updatePassword (?, ?)}");
        statement.setString(1, userGuid);
        statement.setString(2, newUserPassword);
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который изменяет пользовательский аватар на странице с информацией о пользователе.
     * Справка: вызываемая процедура на SQL Server "bs_bsUser_changeAvatar"
     * @param userGuid - Глобальный идентификатор пользователя
     * @param avatar - Аватар
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void changeAvatar(final String userGuid, final byte[] avatar,
                                    final Connection connection) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsUser_changeAvatar(?,?)}");
        statement.setString(1, userGuid);
        statement.setBytes(2, avatar);
        statement.execute();
        statement.close();
    }

    /**
     * Метод, который осуществляет отправку денежных средств посредством транзакции.
     * @param from - GUID банковского депозита/карты с которого будет осуществляться передача
     * @param fromType - Тип (депозит/карта) с которого будет осуществляться передача
     * @param to - GUID банковского депозита/карты на который будет осуществляться передача
     * @param toType - Тип (депозит/карта) на который будет осуществляться передача
     * @param value - Размер денежной передачи
     * @param connection - Подключение к базе данных
     * @return - true - передача успешна, false - ошибка
     * @throws SQLException
     */
    public static boolean sendMoney(final String from, final String fromType, final String to,
                                 final String toType, final byte[] value,
                                    final Connection connection) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_sendMoney (?, ?, ?, ?, ?)}");
        statement.setString(1, from);
        statement.setString(2, fromType);
        statement.setString(3, to);
        statement.setString(4, toType);
        final String decryptValueAsString = BSDb.decryptBytesBySK(value, connection);
        final BigDecimal decryptValue = new BigDecimal(decryptValueAsString)
                .setScale(2, BigDecimal.ROUND_DOWN);
        statement.setBigDecimal(5, decryptValue);
        final boolean isExecute = statement.execute();
        statement.close();

        return isExecute;
    }

    /**
     * Метод, который осуществляет отправку денежных средств посредством транзакции.
     * @param from - GUID банковского депозита/карты с которого будет осуществляться передача
     * @param fromType - Тип (депозит/карта) с которого будет осуществляться передача
     * @param to - GUID банковского депозита/карты на который будет осуществляться передача
     * @param toType - Тип (депозит/карта) на который будет осуществляться передача
     * @param value - Размер денежной передачи
     * @param connection - Подключение к базе данных
     * @return - true - передача успешна, false - ошибка
     * @throws SQLException
     */
    public static boolean sendMoney(final String from, final String fromType, final String to,
                                    final String toType, final BigDecimal value,
                                    final Connection connection) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_sendMoney (?, ?, ?, ?, ?)}");
        statement.setString(1, from);
        statement.setString(2, fromType);
        statement.setString(3, to);
        statement.setString(4, toType);
        statement.setBigDecimal(5, value);
        final boolean isExecute = statement.execute();
        statement.close();

        return isExecute;
    }

    /**
     * Метод, который производит удаление пользователя из системы вместе с сессиями, депозитами и
     * картами
     * @param userGuid - GUID пользователя
     * @param connection - Подключение к базе данных
     * @throws SQLException
     */
    public static void deleteUser(final String userGuid, final Connection connection)
            throws SQLException {
        final PreparedStatement statement = connection
                .prepareStatement("{call dbo.bs_bsUser_deleteUser(?)}");
        statement.setString(1, userGuid);
        statement.execute();
        statement.close();
    }

    /**
     * Получает количество пользователей в системе
     * @param connection
     * @return
     * @throws SQLException
     */
    public static int getUsersCount(final Connection connection) throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT\n")
                .append("COUNT([user].OUID) AS usersCount\n")
                .append("FROM dbo.BS_USER AS [user]\n")
                .append("WHERE [user].STATUS = 10 OR [user].STATUS IS NULL");
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(sql.toString());
        int usersCount = 0;
        if (resultSet.next())
            usersCount = resultSet.getInt(1);
        resultSet.close();
        statement.close();

        return usersCount;
    }

    public String getLogin () {
        return login;
    }

    public void setLogin (String login) {
        this.login = login;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public String getFirstName () {
        return firstName;
    }

    public void setFirstName (String firstName) {
        this.firstName = firstName;
    }

    public String getLastName () {
        return lastName;
    }

    public void setLastName (String lastName) {
        this.lastName = lastName;
    }

    public String getSecondName () {
        return secondName;
    }

    public void setSecondName (String secondName) {
        this.secondName = secondName;
    }

    public long getBirthDate () {
        return birthDate;
    }

    public void setBirthDate (long birthDate) {
        this.birthDate = birthDate;
    }

    public String geteMail () {
        return eMail;
    }

    public void seteMail (String eMail) {
        this.eMail = eMail;
    }

    public byte[] getAvatar () {
        return avatar;
    }

    public void setAvatar (byte[] avatar) {
        this.avatar = avatar;
    }
}
