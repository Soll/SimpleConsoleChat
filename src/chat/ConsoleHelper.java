package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void write(String text) {
        System.out.println(text);
    }

    public static String readString() {

        String str = null;

        while (str == null || str.isEmpty()) {
            try {
                str = reader.readLine();
            } catch (IOException e) {
                write("Ошибка ввода строки!");
            }
        }
        return str;
    }

    public static int readInt() {

        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                write("Введено не число. Попробуйте еще раз!");
            }
        }
    }

}
