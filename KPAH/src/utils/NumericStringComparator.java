package utils;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class NumericStringComparator implements Comparator<String> {

    private static final Pattern PATTERN = Pattern.compile("\\d+");

    @Override
    public int compare(String s1, String s2) {
        Matcher matcher1 = PATTERN.matcher(s1);
        Matcher matcher2 = PATTERN.matcher(s2);
        while (matcher1.find() && matcher2.find()) {
            int number1 = Integer.parseInt(matcher1.group());
            int number2 = Integer.parseInt(matcher2.group());

            if (number1 != number2) {
                return number1 - number2;
            }
        }
        return 0;
    }
}
