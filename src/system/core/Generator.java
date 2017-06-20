package system.core;

import java.util.Random;

/**
 * Класс, который позволяет генерировать псевдослучайные последовательности
 */
public class Generator {
    private static final String symbols = "qwertyuiop[]asdfghjkl;'\\zxcvbnm,./`1234567890-=~!@"
            + "#$%^&*()_+QWERTYUIOP{}ASDFGHJKL:\"|ZXCVBNM<>?";
    private final static Random random = new Random();
    private static StringBuilder stringBuilder = new StringBuilder();

    /**
     * Метод, который возвращает псевдослучайный пароль по заданной длине.
     * Длина пароля должна быть больше 0, но не более 255 символов.
     * @param length - Длина пароля
     * @return - псевдослучайный пароль
     */
    public static String getPseudoGeneratedPassword(int length) {
        String pass = null;
        if (length > 0 && length <= 255) {
            for (int i = 0; i <= length; i++) {
                stringBuilder.append(symbols.charAt(random.nextInt(symbols.length())));
            }
            pass = stringBuilder.toString();
            stringBuilder = null;
        }

        return pass;
    }
}
