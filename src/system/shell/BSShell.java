package system.shell;

import system.core.*;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Интерпритатор для ядра
 */
public class BSShell {
    /**
     * Версия ядра
     */
    private static final String CORE_VERSION = "1.0.0a";

    private static final String HELP_CMD = "bscore -h";
    private static final String INFO_CMD = "bscore -i";

    /**
     * Получает команду и возвращает информацию в соответствии с командой
     * @param command
     * @param connection
     * @return
     * @throws SQLException
     */
    public static String getDataByCommand(final String command, final Connection connection)
            throws SQLException {
        final StringBuilder info = new StringBuilder();
        if (HELP_CMD.equals(command))
            info.append("Commands: \"").append(HELP_CMD).append("\" (help), \"").append(INFO_CMD)
                    .append("\" (Base info about system)").append("\n");
        else if (INFO_CMD.equals(command)) {
            info.append("Core version: ").append(CORE_VERSION).append("\n");
            final int usersCount = BSUser.getUsersCount(connection);
            info.append("Number of users: ").append(usersCount).append("\n");
            final int depositsCount = BSBankAccount.getBankAccountCount(connection);
            info.append("Number of deposits: ").append(depositsCount).append("\n");
            final int cardsCount = BSCard.getCardsCount(connection);
            info.append("Number of cards: ").append(cardsCount).append("\n");
            final int transactionsCount = BSTransactionInfo.getTranscationsCount(connection);
            info.append("Number of transactions: ").append(transactionsCount).append("\n");
            final int activeSessionsCount = BSSession.getActiveSessionsCount(connection);
            info.append("Number of active session: ").append(activeSessionsCount).append("\n");
        } else info.append("Incorrect command, please enter \"").append(HELP_CMD)
                .append("\" for help").append("\n");

        return info.toString();
    }
}
