package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class Logger {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final static File FILE_ERROR;
    private static BufferedWriter writeError;
    private static int countErrorWriteError = 0;

    private static final int MAX_COUNT_ERROR = 10;

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy _ HH-mm-ss");
        String time = formatter.format(LocalDateTime.now());
        String pathFile = "log/";
        String nameFile = time + ".log";
        FILE_ERROR = new File(pathFile + nameFile);
        if (!FILE_ERROR.getParentFile().exists()) {
            boolean success = FILE_ERROR.getParentFile().mkdirs();
            if (!success) {
                System.exit(0);
            }
        }
        try {
            writeError = new BufferedWriter(new FileWriter(FILE_ERROR));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static String getCurrentTime() {
        return LocalDateTime.now().format(FORMATTER);
    }

    public static void logError(final String message, Exception e) {
        final String callFrom = getLogCallerInfo();
        Thread.ofVirtual().start(() -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionStackTrace = sw.toString();

            StringBuilder strBuild = new StringBuilder();
            strBuild
                    .append("[ERROR] ")
                    .append(getCurrentTime())
                    .append(" - ")
                    .append(message)
                    .append("\n\tfrom: ")
                    .append(callFrom)
                    .append("\nChi tiết lỗi:\n\t")
                    .append(exceptionStackTrace);
            String msg = strBuild.toString();
            Printer.printBlue(msg);
            try {
                writeError.write(msg);
                writeError.newLine();
                writeError.flush();
            } catch (Exception ex) {
                if (countErrorWriteError > MAX_COUNT_ERROR) {
                    return;
                }
                countErrorWriteError++;
                ex.printStackTrace();
            }
        });
    }

    private static String getLogCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 4) {
            StackTraceElement caller = stackTrace[3];
            StringBuilder strBuild = new StringBuilder();
            strBuild.append(caller.getClassName())
                    .append(".")
                    .append(caller.getMethodName())
                    .append("(")
                    .append(caller.getFileName())
                    .append(":")
                    .append(caller.getLineNumber())
                    .append(")");
            return strBuild.toString();
        }
        return "";
    }
}
