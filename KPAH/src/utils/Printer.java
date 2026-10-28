package utils;

import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.ansi;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class Printer {

    public static void printAscii(String[] asciiArt, Ansi.Color color) {
        for (String s : asciiArt) {
            System.out.println(ansi().fg(color).a(s).reset());
        }
    }

    public static void printAscii(String[] asciiArt, int r, int g, int b) {
        for (String s : asciiArt) {
            System.out.println(ansi().fgRgb(r, g, b).a(s).reset());
        }
    }
    
    public static void printRed(String text) {
        System.out.println(ansi().fgRed().a(text).reset());
    }

    public static void printGreen(String text) {
        System.out.println(ansi().fgGreen().a(text).reset());
    }

    public static void printYellow(String text) {
        System.out.println(ansi().fgYellow().a(text).reset());
    }

    public static void printBlue(String text) {
        System.out.println(ansi().fgBlue().a(text).reset());
    }

    public static void printPurple(String text) {
        System.out.println(ansi().fgMagenta().a(text).reset());
    }

    public static void printCyan(String text) {
        System.out.println(ansi().fgCyan().a(text).reset());
    }
}
