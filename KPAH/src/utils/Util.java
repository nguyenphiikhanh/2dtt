package utils;

import consts.Const;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Synchronized;
import map.Monster;
import player.Player;
import manager.Manager;

public class Util {

    private static final Random RANDOM = new Random();
    private static final ZoneId VIET_NAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String[] FROM_CHARS = {"à", "á", "ả", "ã", "ạ", "â", "ầ", "ấ", "ẩ", "ẫ", "ậ", "ă", "ằ", "ắ", "ẳ", "ẵ", "ặ", "è", "é", "ẻ", "ẽ", "ẹ", "ê", "ề", "ế", "ể", "ễ", "ệ", "ì", "í", "ỉ", "ĩ", "ị", "ò", "ó", "ỏ", "õ", "ọ", "ô", "ồ", "ố", "ổ", "ỗ", "ộ", "ơ", "ờ", "ớ", "ở", "ỡ", "ợ", "ù", "ú", "ủ", "ũ", "ụ", "ư", "ừ", "ứ", "ử", "ữ", "ự", "ỳ", "ý", "ỷ", "ỹ", "ỵ", "đ", "À", "Á", "Ả", "Ã", "Ạ", "Â", "Ầ", "Ấ", "Ẩ", "Ẫ", "Ậ", "Ă", "Ằ", "Ắ", "Ẳ", "Ẵ", "Ặ", "È", "É", "Ẻ", "Ẽ", "Ẹ", "Ê", "Ề", "Ế", "Ể", "Ễ", "Ệ", "Ì", "Í", "Ỉ", "Ĩ", "Ị", "Ò", "Ó", "Ỏ", "Õ", "Ọ", "Ô", "Ồ", "Ố", "Ổ", "Ỗ", "Ộ", "Ơ", "Ờ", "Ớ", "Ở", "Ỡ", "Ợ", "Ù", "Ú", "Ủ", "Ũ", "Ụ", "Ư", "Ừ", "Ứ", "Ử", "Ữ", "Ự", "Ỳ", "Ý", "Ỷ", "Ỹ", "Ỵ", "Đ"};
    private static final String[] TO_CHARS = {"a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "i", "i", "i", "i", "i", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "u", "u", "u", "u", "u", "u", "u", "u", "u", "u", "u", "y", "y", "y", "y", "y", "d", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "i", "i", "i", "i", "i", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "u", "u", "u", "u", "u", "u", "u", "u", "u", "u", "u", "y", "y", "y", "y", "y", "d"};
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public static long roundNumber(long number) {
        if (number < 10) {
            return number;
        }
        String num = String.valueOf(number);
        long repeat = num.length() - (num.length() > 4 ? 2 : 1);
        long numMulti = (int) Math.pow(10, repeat);
        int result = Math.round(number / numMulti);
        return result * numMulti;
    }

    public static int findIndex(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static String capitalizeFirstLetter(String str) {
        return str.isEmpty() ? str : Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String convertTimeToString(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public static String getCurrentDateTimeInVietnam() {
        LocalDateTime currentTime = LocalDateTime.now(VIET_NAM_ZONE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
        return formattedDateTime;
    }

    public static String removeSpecialCharacters(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll("\\s+", "_").toUpperCase();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int index = indexOfAccent(ch);
            if (index != -1) {
                result.append(TO_CHARS[index]);
            } else if (Character.isLetterOrDigit(ch) || ch == '_') {
                result.append(ch);
            }
        }
        return result.toString().toUpperCase();
    }

    private static int indexOfAccent(char ch) {
        for (int i = 0; i < FROM_CHARS.length; i++) {
            if (FROM_CHARS[i].indexOf(ch) != -1) {
                return i;
            }
        }
        return -1;
    }

    public static byte[] concatenate(byte[] dataImage, byte[] data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {
            outputStream.writeShort(dataImage.length);
            outputStream.write(dataImage);
            outputStream.writeShort(data.length);
            outputStream.write(data);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
        }
        return null;
    }

    public static String getFileNameWithoutExtension(String fileName) {
        File file = new File(fileName);
        int lastIndex = file.getName().lastIndexOf('.');
        if (lastIndex == -1) {
            return fileName;
        }
        return file.getName().substring(0, lastIndex);
    }

    @Synchronized
    public static <K, V> void addItemToMap(ConcurrentHashMap<K, List<V>> map, K key, V valueToAdd) {
        if (!map.containsKey(key)) {
            List<V> newList = new ArrayList<>();
            newList.add(valueToAdd);
            map.put(key, newList);
        } else {
            List<V> existingList = map.get(key);
            existingList.add(valueToAdd);
        }
    }

    public static int nextInt(int from, int to) {
        return from + RANDOM.nextInt(to - from + 1);
    }

    public static int nextInt(int max) {
        return RANDOM.nextInt(max);
    }

    public static int getOne(int n1, int n2) {
        return RANDOM.nextInt() % 2 == 0 ? n1 : n2;
    }

    public static boolean canDoWithTime(long lastTime, long miniTimeTarget) {
        return System.currentTimeMillis() - lastTime > miniTimeTarget;
    }

    public static double nextDouble(double max) {
        return RANDOM.nextDouble() * max;
    }

    public static double nextDouble(double from, double to) {
        return from + RANDOM.nextDouble(to - from + 1);
    }

    public static boolean isTrue(double ratio, double typeRatio) {
        double num = Util.nextDouble(typeRatio);
        return num < ratio;
    }

    public static byte getHe(byte clazz) {
        switch (clazz) {
            case Const.KIEM_KHACH -> {
                return Const.KIM;
            }
            case Const.CHIEN_BINH -> {
                return Const.HOA;
            }
            case Const.PHAP_SU -> {
                return Const.THUY;
            }
            case Const.DAU_SI -> {
                return Const.THO;
            }
            case Const.CUNG_THU -> {
                return Const.MOC;
            }
        }
        return -1;
    }

    public static long getExp(int level) {
        long exp = 0;
        for (int i = 0; i < level; i++) {
            exp += Manager.exps[i];
        }
        return exp;
    }

    public static short getPercentExp(int level, long exp) {
        return (short) ((double) exp / (double) getExp(level) * 1000);
    }

    public static byte[] readFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] ab = new byte[fis.available()];
            fis.read(ab, 0, ab.length);
            return ab;
        } catch (Exception e) {
        }
        return null;
    }

    public static byte[] readFile(String url) throws IOException {
        try (FileInputStream fis = new FileInputStream(url)) {
            byte[] ab = new byte[fis.available()];
            fis.read(ab, 0, ab.length);
            return ab;
        }
    }

    public static byte[][] readFileAndSplit(File file) throws IOException {
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            long fileSize = channel.size();
            int halfLength = (int) Math.ceil(fileSize / 2.0);
            ByteBuffer buffer1 = channel.map(FileChannel.MapMode.READ_ONLY, 0, halfLength);
            ByteBuffer buffer2 = channel.map(FileChannel.MapMode.READ_ONLY, halfLength, fileSize - halfLength);
            byte[] ab1 = new byte[halfLength];
            byte[] ab2 = new byte[(int) (fileSize - halfLength)];
            buffer1.get(ab1);
            buffer2.get(ab2);
            return new byte[][]{
                ab1,
                ab2
            };
        }
    }

    public static byte[][] readFileAndSplit(File file, int halfLength) throws IOException {
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            long fileSize = channel.size();
            halfLength += halfLength / 3;
            ByteBuffer buffer1 = channel.map(FileChannel.MapMode.READ_ONLY, 0, halfLength);
            ByteBuffer buffer2 = channel.map(FileChannel.MapMode.READ_ONLY, halfLength, fileSize - halfLength);
            byte[] ab1 = new byte[halfLength];
            byte[] ab2 = new byte[(int) (fileSize - halfLength)];
            buffer1.get(ab1);
            buffer2.get(ab2);
            return new byte[][]{
                ab1,
                ab2
            };
        }
    }

    public static int findSmallestFileSize(File[] files) {
        if (files == null || files.length == 0) {
            return -1;
        }
        int smallestSize = Integer.MAX_VALUE;
        for (File file : files) {
            int fileSize = (int) file.length();
            if (fileSize < smallestSize) {
                smallestSize = fileSize;
            }
        }
        return smallestSize;
    }

    public static byte[][] readFileAndSplit(String url) throws IOException {
        try (FileChannel channel = FileChannel.open(Paths.get(url), StandardOpenOption.READ)) {
            long fileSize = channel.size();
            int halfLength = (int) Math.ceil(fileSize / 2.0);
            ByteBuffer buffer1 = channel.map(FileChannel.MapMode.READ_ONLY, 0, halfLength);
            ByteBuffer buffer2 = channel.map(FileChannel.MapMode.READ_ONLY, halfLength, fileSize - halfLength);
            byte[] ab1 = new byte[halfLength];
            byte[] ab2 = new byte[(int) (fileSize - halfLength)];
            buffer1.get(ab1);
            buffer2.get(ab2);
            return new byte[][]{
                ab1,
                ab2
            };
        }
    }

    public static String removeControlCharacters(String input) {
        String regex = "[\\p{Cntrl}\\r\\n]";
        return input.replaceAll(regex, "");
    }

    public static String formatNumber(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.equals("");
    }

    public static boolean checkSuperiorOrInferior(int value1, int value2, int dis) {
        return Math.abs(value1 - value2) <= dis;
    }

    public static int getDistance(Monster mob1, Monster mob) {
        return getDistance(mob1.getX(), mob1.getY(), mob.getX(), mob.getY());
    }

    public static int getDistance(Player pl1, Monster mob) {
        return getDistance(pl1.getLocation().getX(), pl1.getLocation().getY(), mob.getX(), mob.getY());
    }

    public static int getDistance(Player pl1, Player pl2) {
        return getDistance(pl1.getLocation().getX(), pl1.getLocation().getY(), pl2.getLocation().getX(), pl2.getLocation().getY());
    }

    public static int getDistance(int x, int y, int x2, int y2) {
        return sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));
    }

    public static File[] getListFile(String pathFolder) {
        return new File(pathFolder).listFiles();
    }

    public static int sqrt(int a) {
        if (a <= 0) {
            return 0;
        }
        int num = (a + 1) / 2;
        int num2;
        do {
            num2 = num;
            num = num / 2 + a / (2 * num);
        } while (Math.abs(num2 - num) > 1);
        return num;
    }

    public static long plusDayToTimeStamp(long timeStamp, byte day) {
        return timeStamp + (7 * 24 * 60 * 60 * 1000);
    }

    public static int getDayDifference(long startDate, long endDate) {
        long differenceInTime = startDate - endDate;
        long differenceInDays = (differenceInTime / (86400000));
        return (int) differenceInDays;
    }

    public static int getSecondDifference(long startDate, long endDate) {
        long differenceInTime = startDate - endDate;
        long differenceInDays = differenceInTime / 1000;
        return (int) differenceInDays;
    }

    public static int getMinutesDifference(long startDate, long endDate) {
        long differenceInTime = startDate - endDate;
        long differenceInDays = differenceInTime / 60000;
        return (int) differenceInDays;
    }
}
