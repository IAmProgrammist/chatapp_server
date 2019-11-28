import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(getCurrentTime() + " " + message);
    }

    public static String readString() {
        String message = "";
        while (true) {
            try {
                message = reader.readLine();
                break;
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
        return message;
    }

    public static int readInt() {
        int i = 0;
        while (true) {
            try {
                i = Integer.parseInt(readString());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
        return i;
    }

    private static String getCurrentTime() {
        Date date = new Date();
        String result = String.format("<%d.%d.%d %d:%d:%d.%d>", date.getDate(), date.getMonth() + 1, date.getYear() + 1900, date.getHours(), date.getMinutes(), date.getSeconds(), getMilliseconds(date));
        return result;
    }

    private static Integer getMilliseconds(Date date) {
        int n = (int) (date.getTime() % 1000);
        return n < 0 ? n + 1000 : n;
    }
}
