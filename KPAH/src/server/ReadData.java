package server;

import database.HikariCP;
import database.ResultSetImpl;
import effects.Animation;
import effects.ImageInfo;
import effects.PartFrame;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import manager.Manager;
import map.Actor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import player.Skill;
import template.ShopTemplate;
import consts.Const;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ReadData {

    public static void main(String[] args) {
        try {
            loadMob();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadMob() throws IOException, JSONException, SQLException {
        File folder = new File("E:\\KPAH\\KPAHZ\\Data\\MobInfo");
        for (File folderChild : folder.listFiles()) {
            int idmap = Integer.parseInt(Util.getFileNameWithoutExtension(folderChild.getName()));
            if (idmap != 17) {
                continue;
            }
            List<Actor> mobs = new ArrayList<>();
            for (File file : folderChild.listFiles()) {
                int nameFile = Integer.parseInt(Util.getFileNameWithoutExtension(folderChild.getName()));
                if (nameFile < 0) {
                    continue;
                }
                String[] data = readFileWithLine(file).split("_");
                int idMob = Integer.parseInt(data[1].replace("type: ", ""));
                int x = Integer.parseInt(data[2].replace("x: ", ""));
                int y = Integer.parseInt(data[3].replace("y: ", ""));
                if (idMob >= 0) {
                    if (idMob >= 94 && idMob <= 112) {
                        continue;
                    }
                    Actor mob = Actor.builder().index(idMob).x((short) x).y((short) y).plusY(-1).build();
                    mobs.add(mob);
                }
            }
            if (HikariCP.executeExist(String.format("SELECT EXISTS(SELECT * FROM `maps` WHERE `id` = '%s')", idmap)) == 1) {
                HikariCP.execute(String.format("UPDATE `maps` SET `mobs`='%s' WHERE `id` = '%s'", mobs.toString(), idmap));
            }
        }
    }

    public static void readPart(byte[] arr, byte idParent, byte id) throws IOException, JSONException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(arr));
        short lengthImg = dis.readShort();
        byte[] image = dis.readNBytes(lengthImg);
        lengthImg = dis.readShort();
        byte[] array2 = dis.readNBytes(lengthImg);
        DataInputStream dataInputStream2 = new DataInputStream(new ByteArrayInputStream(array2));
        int avx0 = readSignByte(dataInputStream2);
        int avy0 = readSignByte(dataInputStream2);
        int avw0 = readSignByte(dataInputStream2);
        int avh0 = readSignByte(dataInputStream2);
        int avxf = readSignByte(dataInputStream2);
        int avyf = readSignByte(dataInputStream2);
        int[][] x;
        int[][] y;
        int[][] w;
        int[][] h;
        int[][] dx;
        int[][] dy;
        dx = new int[4][];
        dy = new int[4][];
        for (int i = 0; i < dx.length; i++) {
            dx[i] = new int[6];
            dy[i] = new int[6];
        }
        int num = 0;
        switch (idParent) {
            case 0:
            case 1:
                num = 5;
                break;
            case 2:
            case 3:
                num = 1;
                break;
            default:
                num = 2;
                break;
        }
        x = new int[4][];
        y = new int[4][];
        w = new int[4][];
        h = new int[4][];
        for (int j = 0; j < x.length; j++) {
            x[j] = new int[num];
            y[j] = new int[num];
            w[j] = new int[num];
            h[j] = new int[num];
        }
        try {
            if (idParent < 4) {
                for (int k = 0; k < 4; k++) {
                    for (int l = 0; l < x[k].length; l++) {
                        x[k][l] = dataInputStream2.read();
                        y[k][l] = dataInputStream2.read();
                        w[k][l] = dataInputStream2.read();
                        h[k][l] = dataInputStream2.read();
                    }
                    for (int m = 0; m < 6; m++) {
                        dx[k][m] = readSignByte(dataInputStream2);
                        dy[k][m] = readSignByte(dataInputStream2);
                    }
                }
                HikariCP.execute(String.format("INSERT INTO `char_part`(`idPart`, `type`, `avxf`, `avyf`, `avx0`, `avy0`, `avw0`, `avh0`, `x`, `y`, `w`, `h`, `dx`, `dy`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                        id, idParent, avxf, avyf, avx0, avy0, avw0, avh0, new JSONArray(x), new JSONArray(y), new JSONArray(w), new JSONArray(h), new JSONArray(dx), new JSONArray(dy)));
                return;
            }
            for (int n = 0; n < 3; n++) {
                for (int numa = 0; num < x[n].length; num++) {
                    x[n][numa] = dataInputStream2.read();
                    y[n][numa] = dataInputStream2.read();
                    w[n][numa] = dataInputStream2.read();
                    h[n][numa] = dataInputStream2.read();
                }
            }
            for (int num2 = 0; num2 < 4; num2++) {
                for (int num3 = 0; num3 < 6; num3++) {
                    dx[num2][num3] = readSignByte(dataInputStream2);
                    dy[num2][num3] = readSignByte(dataInputStream2);
                }
            }
            HikariCP.execute(String.format("INSERT INTO `char_part`(`idPart`, `type`, `avxf`, `avyf`, `avx0`, `avy0`, `avw0`, `avh0`, `x`, `y`, `w`, `h`, `dx`, `dy`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    id, idParent, avxf, avyf, avx0, avy0, avw0, avh0, new JSONArray(x), new JSONArray(y), new JSONArray(w), new JSONArray(h), new JSONArray(dx), new JSONArray(dy)));
        } catch (Exception e) {
        }
        dis.close();
        dataInputStream2.close();
    }

    public static int readSignByte(DataInputStream iss) {
        byte[] array = new byte[1];
        try {
            int num = array.length;
            if (num > 1) {
                num = 1;
            }
            for (int i = 0; i < 1; i++) {
                array[i] = iss.readByte();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return array[0];
    }

    public static void writeToFile(String fileName, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(text);
            System.out.println("Text has been written to the file successfully.");
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static void writeBytesToFile(byte[] data, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        }
    }

    private static void loadItemQuest() throws IOException, JSONException, SQLException {
        String s = readFile("E:\\KPAH\\KPAHZ\\Data\\Config\\NAME_ITEM_QUEST.txt");
        String s2 = readFile("E:\\KPAH\\KPAHZ\\Data\\Config\\ICON_IMG_QUEST.txt");
        JSONArray arr = new JSONArray(s);
        JSONArray arr2 = new JSONArray(s2);
        for (int i = 0; i < arr.length(); i++) {
            int idIcon = arr2.getInt(i);
            String name = arr.getString(i);
            HikariCP.execute(String.format("INSERT INTO `item_quest`(`name`, `iconId`) VALUES ('%s','%s')", name, idIcon));
        }
    }

    private static void loadThanThuEffect() throws IOException, JSONException, SQLException {
        File folder = new File("E:\\KPAH\\KPAHZ\\Data\\SkillEffect");
        List<File> fileList = new ArrayList<>();
        Arrays.stream(folder.listFiles())
                .filter(File::isFile) // Chỉ lấy các tệp tin, loại bỏ các thư mục
                .sorted(Comparator.comparing(File::getName, new NumericStringComparator())) // Sắp xếp theo tên
                .forEach(a -> fileList.add(a)); // In ra tên các tệp tin đã sắp xếp
        for (File file : fileList) {
            if (file.exists()) {
                byte[] data = Util.readFile(file);
                List<ImageInfo> imgInfo;
                List<List<PartFrame>> frame;
                byte indexStartSkill;
                byte[][] frameChar = new byte[][]{
                    new byte[1],
                    new byte[1],
                    new byte[1],
                    new byte[1]
                };
                byte[] arrFrame;
                try (DataInputStream dos = new DataInputStream(new ByteArrayInputStream(data))) {
                    int idEff = Integer.parseInt(file.getName());
                    byte b = dos.readByte();
                    imgInfo = new ArrayList<>();
                    for (int i = 0; i < b; i++) {
                        imgInfo.add(ImageInfo.builder().ID(dos.readByte()).x0(dos.readByte()).y0(dos.readByte()).w(dos.readByte()).h(dos.readByte()).build());
                    }
                    short b2 = dos.readShort();
                    frame = new ArrayList<>();
                    for (int j = 0; j < b2; j++) {
                        byte b3 = dos.readByte();
                        List<PartFrame> parts = new ArrayList<>();
                        for (int k = 0; k < b3; k++) {
                            short dx = dos.readShort();
                            short dy = dos.readShort();
                            byte idImg = dos.readByte();
                            byte top = dos.readByte();
                            byte flip = dos.readByte();
                            parts.add(PartFrame.builder().dx(dx).dy(dy).idSmallImg(idImg).onTop(top).flip(flip).build());
                        }
                        frame.add(parts);
                    }
                    int b4 = dos.readUnsignedByte();
                    arrFrame = new byte[b4];
                    for (int l = 0; l < b4; l++) {
                        arrFrame[l] = (byte) dos.readShort();
                    }
                    indexStartSkill = dos.readByte();
                    byte num3 = dos.readByte();
                    frameChar[0] = new byte[num3];
                    for (int m = 0; m < num3; m++) {
                        frameChar[0][m] = dos.readByte();
                    }
                    num3 = dos.readByte();
                    frameChar[1] = new byte[num3];
                    for (int n = 0; n < num3; n++) {
                        frameChar[1][n] = dos.readByte();
                    }
                    num3 = dos.readByte();
                    frameChar[3] = new byte[num3];
                    for (int num4 = 0; num4 < num3; num4++) {
                        frameChar[3][num4] = dos.readByte();
                    }
                    HikariCP.execute(String.format("INSERT INTO `effect_data`(`idEffect`,`idShadow`, `typeEffect`, `image`, `smallImage`, `frames`, `sequence`, `animations`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s')",
                            idEff, indexStartSkill, Const.SKILL_EFFECT, "[]", imgInfo.toString(), frame.toString(), new JSONArray(arrFrame), new JSONArray(frameChar)));
                }
            }
        }
    }

    private static void loadPetEffect() throws IOException, JSONException, SQLException {
        File folder = new File("E:\\KPAH\\KPAHZ\\Data\\DataEffect");
        List<File> fileList = new ArrayList<>();
        Arrays.stream(folder.listFiles())
                .filter(File::isFile) // Chỉ lấy các tệp tin, loại bỏ các thư mục
                .sorted(Comparator.comparing(File::getName, new NumericStringComparator())) // Sắp xếp theo tên
                .forEach(a -> fileList.add(a)); // In ra tên các tệp tin đã sắp xếp
        for (File file : fileList) {
            if (file.exists() && file.getName().startsWith("PetData")) {
                byte[] data = Util.readFile(file);
                List<ImageInfo> imgInfo;
                List<List<PartFrame>> frame;
                List<Animation> animation = new ArrayList<>();
                byte idShadow = -1;
                byte[] arrFrame;
                try (DataInputStream dos = new DataInputStream(new ByteArrayInputStream(data))) {
                    int idEff = Integer.parseInt(file.getName().replace("PetData", ""));
                    byte b = dos.readByte();
                    imgInfo = new ArrayList<>();
                    for (int i = 0; i < b; i++) {
                        imgInfo.add(ImageInfo.builder().ID(dos.readByte()).x0(dos.readByte()).y0(dos.readByte()).w(dos.readByte()).h(dos.readByte()).build());
                    }

                    short b2 = dos.readShort();
                    frame = new ArrayList<>();
                    for (int j = 0; j < b2; j++) {
                        byte b3 = dos.readByte();
                        List<PartFrame> parts = new ArrayList<>();
                        for (int k = 0; k < b3; k++) {
                            short dx = dos.readShort();
                            short dy = dos.readShort();
                            byte idImg = dos.readByte();
                            byte top = dos.readByte();
                            byte flip = dos.readByte();
                            parts.add(PartFrame.builder().dx(dx).dy(dy).idSmallImg(idImg).onTop(top).flip(flip).build());
                        }
                        frame.add(parts);
                    }
                    short b4 = dos.readShort();
                    arrFrame = new byte[b4];
                    for (int l = 0; l < b4; l++) {
                        arrFrame[l] = (byte) dos.readShort();
                    }
                    byte num = dos.readByte();
                    byte[] data2 = new byte[num];
                    dos.read(data2);
                    Animation o = new Animation(data2);
                    animation.add(o);
                    num = dos.readByte();
                    data2 = new byte[num];
                    dos.read(data2);
                    o = new Animation(data2);
                    animation.add(o);
                    num = dos.readByte();
                    data2 = new byte[num];
                    dos.read(data2);
                    o = new Animation(data2);
                    animation.add(o);
                    num = dos.readByte();
                    data2 = new byte[num];
                    dos.read(data2);
                    o = new Animation(data2);
                    animation.add(o);
                    num = dos.readByte();
                    data2 = new byte[num];
                    dos.read(data2);
                    o = new Animation(data2);
                    animation.add(o);
                    num = dos.readByte();
                    data2 = new byte[num];
                    dos.read(data2);
                    o = new Animation(data2);
                    animation.add(o);
                    num = dos.readByte();
                    data2 = new byte[num];
                    dos.read(data2);
                    o = new Animation(data2);
                    animation.add(o);
                    num = dos.readByte();
                    data2 = new byte[num];
                    dos.read(data2);
                    o = new Animation(data);
                    animation.add(o);
                    if (dos.available() > 0) {
                        idShadow = dos.readByte();
                        for (int m = 0; m < b2; m++) {
                            byte xShadow = dos.readByte();
                            byte yShadow = dos.readByte();
                            for (List<PartFrame> part : frame) {
                                for (PartFrame f : part) {
                                    f.setXShadow(xShadow);
                                    f.setYShadow(yShadow);
                                }
                            }
                        }
                    }
                    if (dos.available() > 0) {
                        num = dos.readByte();
                        data2 = new byte[num];
                        dos.read(data2);
                        o = new Animation(data);
                        animation.add(o);
                        num = dos.readByte();
                        data2 = new byte[num];
                        dos.read(data2);
                        o = new Animation(data);
                        animation.add(o);
                        num = dos.readByte();
                        data2 = new byte[num];
                        dos.read(data2);
                        o = new Animation(data);
                        animation.add(o);
                        num = dos.readByte();
                        data2 = new byte[num];
                        dos.read(data2);
                        o = new Animation(data);
                        animation.add(o);
                    }
                    HikariCP.execute(String.format("INSERT INTO `effect_data`(`idEffect`,`idShadow`, `typeEffect`, `image`, `smallImage`, `frames`, `sequence`, `animations`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s')",
                            idEff, idShadow, Const.PET_EFFECT, "[]", imgInfo.toString(), frame.toString(), new JSONArray(arrFrame), animation.toString()));
                }
            }
        }
    }

    private static void loadEffect() throws IOException, JSONException, SQLException {
        File folder = new File("E:\\KPAH\\KPAHZ\\Data\\EffectNormal");
        for (File file : folder.listFiles()) {
            if (file.exists()) {
                byte[] data = Util.readFile(file);
                List<ImageInfo> imgInfo;
                List<List<PartFrame>> frame;
                byte[] arrFrame;
                try (DataInputStream dos = new DataInputStream(new ByteArrayInputStream(data))) {
                    byte type = dos.readByte();
                    byte idEff = dos.readByte();
                    short lengthImg = dos.readShort();
                    byte[] image = new byte[lengthImg];
                    dos.read(image);
                    byte b = dos.readByte();
                    imgInfo = new ArrayList<>();
                    for (int i = 0; i < b; i++) {
                        imgInfo.add(ImageInfo.builder().ID(dos.readByte()).x0(dos.readByte()).y0(dos.readByte()).w(dos.readByte()).h(dos.readByte()).build());
                    }
                    byte b2 = dos.readByte();
                    frame = new ArrayList<>();
                    for (int j = 0; j < b2; j++) {
                        byte b3 = dos.readByte();
                        byte[] dx = new byte[b3];
                        byte[] dy = new byte[b3];
                        byte[] idImg = new byte[b3];
                        List<PartFrame> parts = new ArrayList<>();
                        for (int k = 0; k < b3; k++) {
                            dx[k] = dos.readByte();
                            dy[k] = dos.readByte();
                            idImg[k] = dos.readByte();
                            parts.add(PartFrame.builder().dx(dx[k]).dy(dy[k]).idSmallImg(idImg[k]).build());
                        }
                        frame.add(parts);
                    }
                    byte b4 = dos.readByte();
                    arrFrame = new byte[b4];
                    for (int l = 0; l < b4; l++) {
                        arrFrame[l] = dos.readByte();
                    }
                    HikariCP.execute(String.format("INSERT INTO `effect_data`(`idEffect`, `typeEffect`, `image`, `smallImage`, `frames`, `sequence`, `animations`) VALUES ('%s','%s','%s','%s','%s','%s','%s')",
                            idEff, Const.NORMAL_EFFECT, new JSONArray(image), imgInfo.toString(), frame.toString(), new JSONArray(arrFrame), "[]"));
                }
            }
        }
    }

    private static void loadEffectDynamic() throws IOException, JSONException, SQLException {
        File folder = new File("E:\\KPAH\\KPAHZ\\Data\\DynamicEffect");
        File[] files = folder.listFiles();
        List<File> fileList = new ArrayList<>();
        Arrays.stream(files)
                .filter(File::isFile) // Chỉ lấy các tệp tin, loại bỏ các thư mục
                .sorted(Comparator.comparing(File::getName, new NumericStringComparator())) // Sắp xếp theo tên
                .forEach(a -> fileList.add(a)); // In ra tên các tệp tin đã sắp xếp
        for (int p = 0; p < fileList.size(); p++) {
            File file = fileList.get(p);
            if (file.exists()) {
                byte[] data = Util.readFile(file);
                List<ImageInfo> imgInfo;
                List<List<PartFrame>> frame;
                List<Animation> animation = new ArrayList<>();
                byte[] arrFrame;
                try (DataInputStream dos = new DataInputStream(new ByteArrayInputStream(data))) {
                    byte b = dos.readByte();
                    imgInfo = new ArrayList<>();
                    for (int i = 0; i < b; i++) {
                        imgInfo.add(ImageInfo.builder().ID(dos.readByte()).x0(dos.readByte()).y0(dos.readByte()).w(dos.readByte()).h(dos.readByte()).build());
                    }
                    frame = new ArrayList<>();
                    int num2 = dos.readShort();
                    for (int j = 0; j < num2; j++) {
                        byte ba = dos.readByte();
                        List<PartFrame> arr = new ArrayList<>();
                        for (int k = 0; k < ba; k++) {
                            PartFrame o = PartFrame.builder().dx(dos.readShort()).dy(dos.readShort()).idSmallImg(dos.readByte()).build();
                            arr.add(o);
                        }
                        frame.add(arr);
                    }
                    short b4 = dos.readShort();
                    arrFrame = new byte[b4];
                    for (int l = 0; l < b4; l++) {
                        arrFrame[l] = (byte) dos.readShort();
                    }
                    byte b2 = dos.readByte();
                    byte b3 = dos.readByte();
                    byte num3 = dos.readByte();
                    byte[] data2 = new byte[num3];
                    dos.read(data2);
                    Animation o2 = new Animation(data);
                    animation.add(o2);
                    num3 = dos.readByte();
                    data2 = new byte[num3];
                    dos.read(data2);
                    o2 = new Animation(data);
                    animation.add(o2);
                    num3 = dos.readByte();
                    data2 = new byte[num3];
                    dos.read(data2);
                    o2 = new Animation(data);
                    animation.add(o2);
                    num3 = dos.readByte();
                    data2 = new byte[num3];
                    dos.read(data2);
                    o2 = new Animation(data);
                    animation.add(o2);
                    HikariCP.execute(String.format("INSERT INTO `effect_data`(`idEffect`, `typeEffect`, `image`, `smallImage`, `frames`, `sequence`, `animations`) VALUES ('%s','%s','%s','%s','%s','%s','%s')",
                            Integer.valueOf(file.getName().replace("DynamicEffect", "")), Const.DYNAMIC_EFFECT, "[]", imgInfo.toString(), frame.toString(), new JSONArray(arrFrame), animation.toString()));
                }
            }
        }
    }

    private static void loadSkillNew() throws IOException, JSONException, SQLException {
        for (int id = 0; id < 5; id++) {
            String s = readFile("E:\\KPAH\\KPAHZ\\Data\\Skill\\" + id + ".txt");
            JSONArray arr = new JSONArray(s);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String name = obj.getString("name");
                String des = obj.getString("decript");
                byte idSkill = (byte) obj.getInt("idSkill");
                byte charClass = (byte) obj.getInt("charClass");
                int price = obj.getInt("price");
                HikariCP.execute(String.format("INSERT INTO `skill_news`(`name`, `decript`, `idSkill`, `charClass`, `price`) VALUES ('%s','%s','%s','%s','%s')", name, des, idSkill, charClass, price));
            }
        }
    }

    private static String readFileWithLine(File file) {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("_");
            }
            return result.toString();
        } catch (IOException e) {
            System.err.println("Đã xảy ra lỗi khi đọc tệp: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return "";
    }

    private static String readFile(File file) {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            System.err.println("Đã xảy ra lỗi khi đọc tệp: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return "";
    }

    private static String readFile(String filePath) {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            System.err.println("Đã xảy ra lỗi khi đọc tệp: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return "";
    }

    private static void loadMap() {
        try {
            for (int p = 17; p < 18; p++) {
                int mapId = p;
                File file = new File("E:\\KPAH\\KPAHZ\\Data\\Map\\" + mapId);
                if (file.exists()) {
                    if (!file.getName().startsWith("Info_")) {
                        try (DataInputStream dos = new DataInputStream(new FileInputStream(file))) {
                            int w = dos.read();
                            int h = dos.read();
                            short[] map = new short[w * h];
                            for (int j = 0; j < w * h; j++) {
                                map[j] = (short) dos.read();
                            }
                            String data = "[[" + w + "," + h + "]," + new JSONArray(map) + "]";
                            List<Actor> tileTops = new ArrayList<>();
                            while (true) {
                                int num = dos.read();
                                if (num == 255) {
                                    Actor tileTop = Actor.builder().indexs(new short[]{}).x((short) 255).sizeX(0).sizeY(0).plusY(0).build();
                                    tileTops.add(tileTop);
                                    break;
                                }
                                int num2 = dos.read();
                                int num3 = dos.read();
                                int num4 = dos.read();
                                List<Short> indexs = new ArrayList<>();
                                for (int k = num2; k < num2 + num4; k++) {
                                    for (int l = num; l < num + num3; l++) {
                                        short num5 = (short) dos.read();
                                        indexs.add(num5);
                                    }
                                }
                                short[] array = new short[indexs.size()];
                                for (int i = 0; i < indexs.size(); i++) {
                                    array[i] = indexs.get(i);
                                }
                                Actor tileTop = Actor.builder().x((short) num).indexs(array).sizeX(num3).sizeY(num2).plusY(num4).build();
                                tileTops.add(tileTop);
                            }
                            List<Actor> tileTops2 = new ArrayList<>();
                            while (true) {
                                int num6 = dos.read();
                                if (num6 == 255) {
                                    Actor tileTop = Actor.builder().y((short) 0).x((short) 255).index(0).sizeX(0).sizeY(0).plusY(-1).build();
                                    tileTops2.add(tileTop);
                                    break;
                                }
                                int num7 = dos.read();
                                short num8 = (short) dos.read();
                                Actor tileTop = Actor.builder().y((short) num7).x((short) num6).index(num8).plusY(-1).build();
                                tileTops2.add(tileTop);
                            }

                            List<Actor> trees = new ArrayList<>();
                            while (true) {
                                int num9 = dos.read();
                                if (num9 == 254) {
                                    num9 = 255;
                                }
                                if (num9 == 255) {
                                    Actor tree = Actor.builder().x((short) num9).y((short) 0).index(0).plusY(-1).build();
                                    trees.add(tree);
                                    break;
                                }
                                int y = dos.read();
                                int num10 = dos.read();
                                Actor tree = Actor.builder().x((short) num9).y((short) y).index(num10).plusY(-1).build();
                                trees.add(tree);
                            }

                            int b = dos.read();
                            int num11 = dos.read();
                            List<Actor> wps = new ArrayList<>();
                            for (int n = 0; n < num11; n++) {
                                int num20 = dos.read();
                                int num21 = dos.read();
                                int[] array = new int[]{
                                    dos.read(),
                                    dos.read()
                                };
                                short num23 = 0;
                                for (int num24 = 1; num24 >= 0; num24--) {
                                    num23 <<= 8;
                                    num23 |= (short) (0xFF & array[num24]);
                                }
                                int toX = dos.read();
                                int toY = dos.read();
                                Actor tree = Actor.builder().index((short) num23).x((short) toX).sizeX((short) toY).sizeY((short) num20).plusY((short) num21).build();
                                wps.add(tree);
                            }
                            List<Actor> npcs = new ArrayList<>();
                            int num26 = dos.read();
                            for (int num27 = 0; num27 < num26; num27++) {
                                Actor tree = Actor.builder().x((short) dos.read()).y((short) dos.read()).index(dos.read()).plusY(-1).build();
                                npcs.add(tree);

                            }
                            String tile = "[" + tileTops.toString() + "," + tileTops2.toString() + "]";
                            String nameMap = readFile("E:\\KPAH\\KPAHZ\\Data\\Map\\Info_" + mapId + ".txt").split("_")[5];
                            if (HikariCP.executeExist(String.format("SELECT EXISTS(SELECT * FROM `maps` WHERE `id` = '%s')", mapId)) == 0) {
                                HikariCP.execute(String.format("INSERT INTO `maps`(`id`, `name`, `maxZone`, `isOfflineMap`, `childMap`, `wayPoints`, `npcsActor`, `mobs`, `npcs`, `data`, `tileTop`, `tree`, `locationWayPoints`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                                        mapId, nameMap, 1, 0, "[]", "[]", "[]", "[]", npcs.toString(), data, tile, trees.toString(), wps.toString()));
                            }
                        }
                    }

                }

            }
        } catch (Exception e) {

        }

    }

    public static byte[] readFile2(String url) {
        try {
            try (FileInputStream fis = new FileInputStream(url)) {
                byte[] ab = new byte[fis.available()];
                fis.read(ab, 0, ab.length);
                return ab;
            }
        } catch (IOException e) {
            return null;

        }

    }

    static class NumericStringComparator implements Comparator<String> {

        private static final Pattern pattern = Pattern.compile("\\d+");

        @Override
        public int compare(String s1, String s2) {
            Matcher matcher1 = pattern.matcher(s1);
            Matcher matcher2 = pattern.matcher(s2);

            while (matcher1.find() && matcher2.find()) {
                int number1 = Integer.parseInt(matcher1.group());
                int number2 = Integer.parseInt(matcher2.group());

                if (number1 != number2) {
                    return number1 - number2;
                }
            }

            return 0; // Trường hợp không có số hoặc các số giống nhau
        }
    }
}
