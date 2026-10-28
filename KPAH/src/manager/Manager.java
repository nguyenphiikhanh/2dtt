package manager;

import clan.Clan;
import daos.PlayerDAO;
import database.HikariCP;
import database.ResultSetImpl;
import effects.Animation;
import effects.EffectData;
import effects.ImageInfo;
import effects.PartChar;
import effects.PartFrame;
import interfaces.IMap;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import map.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Cleanup;
import map.Actor;
import map.ChildMap;
import map.LoctionWayPoint;
import map.MapData;
import map.Monster;
import map.WayPoint;
import map.XaPhu;
import org.json.JSONArray;
import org.json.JSONObject;
import player.Friend;
import player.Player;
import shop.NpcShop;
import template.GemTemplate;
import template.AttributeEquipTemplate;
import template.ItemQuestTemplate;
import template.ItemEquipTemplate;
import template.MonsterTemplate;
import template.NpcTemplate;
import template.PotionTemplate;
import template.ShopTemplate;
import template.SkillNewTemplate;
import template.TreeInfo;
import template.XaPhuTemplate;
import consts.Const;
import consts.NpcConst;
import deposite.Deposite;
import java.util.Arrays;
import java.util.Enumeration;
import map.NpcServer;
import template.AnimalTemplate;
import template.HoaTieuTemplate;
import template.NpcServerTemplate;
import template.ValueAttributeAnimal;
import utils.Logger;
import utils.Printer;
import utils.NumericStringComparator;
import utils.Util;

public class Manager {

    public static ConcurrentHashMap<Byte, ConcurrentHashMap<Short, IMap>> MAPS = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, MonsterTemplate> MOB_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, ItemEquipTemplate> ITEM_EQUIPMENTS = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Byte, ItemQuestTemplate> ITEM_QUEST_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, PotionTemplate> POTION_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, NpcShop> SHOP_NPC_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, ShopTemplate> SHOP_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, GemTemplate> GEM_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Byte, TreeInfo> TREE_INFOS = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, NpcServerTemplate> NPC_SERVER_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, AttributeEquipTemplate> ITEM_ATTRIBUTE_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Byte, HoaTieuTemplate> HOA_TIEU_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, NpcTemplate> NPC_TEMPLATES = new ConcurrentHashMap<>();
    public static List<int[]> TYPE_OF_TILE = new ArrayList<>();
    public static ConcurrentHashMap<Byte, XaPhuTemplate> XA_PHU_TEMPLATE = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, SkillNewTemplate> SKILL_NEW_TEMPLATES = new ConcurrentHashMap<>();
    public static List<EffectData> EFFECT_DATA = new ArrayList<>();
    public static List<PartChar> PART_CHAR = new ArrayList<>();
    public static List<XaPhu> XA_PHU = new ArrayList<>();
    public static ConcurrentHashMap<String, List<Short>> ITEM_EQUIPMENT = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, byte[]> IMAGES_DEFAULT = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, byte[][]> IMAGES_WEAPON = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Byte, byte[]> IMAGES_TREE = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Byte, ConcurrentHashMap<Byte, byte[]>> IMAGES_CLOTH = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Byte, byte[]> IMAGES_ANIMAL = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, byte[]> IMAGES_MONSTER = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, AnimalTemplate> ANIMAL_TEMPLATES = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Short, ValueAttributeAnimal> VALUE_ATTRIBUTE_ANIMAL = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Deposite> DEPOSITE = new ConcurrentHashMap<>();

    // <editor-fold defaultstate="collapsed" desc="Variables">
    public static long[] exps;
    public static byte[] DATA_LOCATION_HAC_HO;
    public static byte[] DATA_LOCATION_THANH_LONG;
    public static byte[] DATA_XA_PHU_TEMPLATE;
    public static byte[] DATA_EFFECT;
    public static short[][][] SKILL_DAM_PERCENT;
    public static short[][] TIME_LIFE_BUFF_SKILL;
    public static int[][][] SKILL_COOLDOWN;
    public static short[][][] SKILL_MP;
    public static short[][] SKILL_RANGE;
    public static byte[][] SKILL_AEO;
    public static byte[][] LEVEL_ADD_SKILL;
    public static byte[] ICON_ANIMAL;
    public static byte[][] ICON_SKILL;
    public static byte[] ICON_POTION;
    public static byte[] ICON_ITEM;
    public static byte[][] DATA_HORSE;
    public static byte[][][] HEAD_HORSE;
    public static byte[] PERCENT_ATTRIBUTE = new byte[]{20, 50, 10, 70, 20};
    public static short ID_MAP_PK;
    public static byte[][] MAIN_ATTACK;
    public static byte[][] MAIN_DEFEND;
    public static byte[][] PERCENT_ATTACK;
    public static byte[][] PERCENT_DEFEND;
    public static byte[] ARENA;
    public static String[] KHAM_PROPERTY;
    public static byte[] ID_NGOC_KHAM;
    public static byte[][] TYPE_MP_HP;
    public static int[][] VALUE_MP_HP;
    public static byte[][] DX_HORSE;
    public static byte[][] DY_HORSE;
    public static byte[][] DX_DY_WP;
    public static byte[][] DX_DY_AVT;
    public static byte[][] DX_DY_PP;
    public static short[] POTION_SHOP;

    public static byte[] ANIMAL_TRAIN_PRICE;
    public static byte ANIMAL_CHANGE_SPECIAL_ATTRIBUTE_PRICE;
    public static byte[] PRICE_UPGRADE_ANIMAL;

    public static final byte[][] EFF_BUFF_SKILL = new byte[][]{
        new byte[]{20, 24},
        new byte[]{20, 21},
        new byte[]{25, 30, 27, 23},
        new byte[]{19, 20},
        new byte[]{22, 19}
    };

    public static final byte[][] SKILL_CAN_BUFF_TO_USER = new byte[][]{
        new byte[]{-1, 0},
        new byte[]{0, -1},
        new byte[]{0, -1, 1, 0},
        new byte[]{0, -1},
        new byte[]{0, -1}
    };

    public static final byte[] BUFF_TYPE = new byte[]{19, 20, 22, 23, 24, 25, 27};
    public static final byte[] NUM_SKILLS = new byte[]{9, 9, 11, 9, 9};
    public static final byte[] X_CHECK = new byte[]{0, 0, -16, 16};
    public static final byte[] Y_CHECK = new byte[]{16, -16, 0, 0};
    public static final byte[] X_FOWARD = new byte[]{0, 0, -17, 17};
    public static final byte[] Y_FOWARD = new byte[]{17, -17, 0, 0};

    public static final boolean[][] ATTRIBUTE_FOR_TYPE = new boolean[][]{
        new boolean[]{false, true, true, true, false, false, true, false, false, false},
        new boolean[]{false, true, true, true, false, false, true, false, false, false},
        new boolean[]{false, true, true, true, false, false, true, false, false, false},
        new boolean[]{true, false, false, true, true, false, false, false, false, false},
        new boolean[]{true, false, false, true, true, false, false, false, false, false},
        new boolean[]{true, false, false, true, true, false, false, false, false, false},
        new boolean[]{true, false, false, true, true, false, false, false, false, false},
        new boolean[]{true, false, false, true, true, false, false, false, false, false},
        new boolean[]{true, false, false, true, false, false, false, false, false, false},
        new boolean[]{true, false, false, false, true, false, false, false, false, false},
        new boolean[]{false, true, true, false, false, false, true, false, false, false},
        new boolean[]{false, true, false, true, true, false, true, false, false, false},
        new boolean[]{false, false, false, true, true, true, false, false, false, false},
        new boolean[]{true, false, false, false, false, false, false, false, false, false},
        new boolean[]{false, true, false, false, false, false, true, false, false, false},
        new boolean[]{true, false, false, true, false, false, false, false, false, false},
        new boolean[]{false, true, false, false, false, false, true, false, false, false},
        new boolean[]{true, false, false, false, true, false, false, false, false, false},
        new boolean[]{false, true, false, false, false, false, true, false, false, false},
        new boolean[]{false, true, false, false, false, false, true, false, false, false}
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Functions">
    public static byte[] getImageCloth(byte id, byte type) {
        ConcurrentHashMap<Byte, byte[]> cloth = IMAGES_CLOTH.getOrDefault(type, null);
        if (cloth != null) {
            return cloth.getOrDefault(id, null);
        }
        return null;
    }

    public static IMap getMap(byte country, short idMap) {
        ConcurrentHashMap<Short, IMap> maps = MAPS.getOrDefault(country, null);
        if (maps == null) {
            return null;
        }
        IMap map = maps.getOrDefault(idMap, null);
        if (map != null) {
            if (map.getMapId() == idMap) {
                return map;
            }
            for (ChildMap child : map.getChildMaps()) {
                if (child != null && child.getId() == idMap) {
                    return child;
                }
            }
        }
        for (IMap currentMap : maps.values()) {
            if (currentMap != map) {
                for (ChildMap child : currentMap.getChildMaps()) {
                    if (child != null && child.getId() == idMap) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    public static Deposite getDeposite(String name) {
        return DEPOSITE.getOrDefault(name, null);
    }

    public static HoaTieuTemplate getHoaTieuTemplate(byte id) {
        return HOA_TIEU_TEMPLATES.getOrDefault(id, null);
    }

    public static NpcServerTemplate getNpcServerTemplate(short id) {
        return NPC_SERVER_TEMPLATES.getOrDefault(id, null);
    }

    public static byte getPriceUpgradeAnimal(byte level) {
        return PRICE_UPGRADE_ANIMAL[level - 1];
    }

    public static byte getAnimalTrainPrice(byte level) {
        return ANIMAL_TRAIN_PRICE[level - 1];
    }

    public static byte[] getImageAnimal(byte id) {
        return IMAGES_ANIMAL.getOrDefault(id, null);
    }

    public static byte[] getImageMonster(short id) {
        return IMAGES_MONSTER.getOrDefault(id, null);
    }

    public static byte[] getImageTree(byte id) {
        return IMAGES_TREE.getOrDefault(id, null);
    }

    public static byte[][] getImageWeapon(short id) {
        return IMAGES_WEAPON.getOrDefault(id, null);
    }

    public static byte[] getImageDefault(short id) {
        return IMAGES_DEFAULT.getOrDefault(id, null);
    }

    public static short randomItemEquipment(byte level, byte gender) {
        String key = level + "_" + gender;
        if (!ITEM_EQUIPMENT.containsKey(key)) {
            return -1;
        }
        List<Short> itemList = ITEM_EQUIPMENT.get(key);
        if (itemList == null || itemList.isEmpty()) {
            return -1;
        }
        int randomIndex = Util.nextInt(0, itemList.size() - 1);
        return itemList.get(randomIndex);
    }

    public static boolean hasNameClan(String name) {
        return ClanManager.CLANS.values().stream().anyMatch(c -> c != null && c.getName().equals(name));
    }

    public static boolean hasNameLeader(String name) {
        return ClanManager.CLANS.values().stream().anyMatch(c -> c != null && c.getNameLeader().equals(name));
    }

    public static Clan getClan(short id) {
        return ClanManager.CLANS.getOrDefault(id, null);
    }

    public static NpcTemplate getNpcTemplate(short id) {
        return NPC_TEMPLATES.getOrDefault(id, null);
    }

    public static NpcShop getItemsNpcShop(String name) {
        return SHOP_NPC_TEMPLATES.getOrDefault(name, null);
    }

    public static byte getCanBuffToUser(byte classChar, byte value) {
        return SKILL_CAN_BUFF_TO_USER[classChar][value];
    }

    public static byte getTypeBuff(byte classChar, byte value) {
        return SKILL_CAN_BUFF_TO_USER[classChar][value];
    }

    public static byte getIndexBuff(byte classChar, byte value) {
        int indexEffect = Util.findIndex(EFF_BUFF_SKILL[classChar], value);
        return (byte) (indexEffect + 4);
    }

    public static int getMpHpPlus(int type, int idPotion) {
        for (int i = 0; i < TYPE_MP_HP[type].length; i++) {
            if (TYPE_MP_HP[type][i] == idPotion) {
                return VALUE_MP_HP[type][i];
            }
        }
        return 0;
    }

    public static short getLevelAddSkill(int idSkill, int lvSkill) {
        return (short) ((lvSkill > 0) ? LEVEL_ADD_SKILL[idSkill][lvSkill] : 0);
    }

    public static boolean isSkillAeo(int cClass, int skill) {
        for (int i = 0; i < SKILL_AEO[cClass].length; i++) {
            if (skill == SKILL_AEO[cClass][i]) {
                return true;
            }
        }
        return false;
    }

    public static short getTimeLifeBuffSkill(int skillType, int skillLevel) {
        return (skillLevel > 0) ? TIME_LIFE_BUFF_SKILL[skillType][skillLevel] : 0;
    }

    public static short getSkillDamPercent(byte clazz, byte skillType, byte skillLevel) {
        return (skillLevel > 0) ? SKILL_DAM_PERCENT[clazz][skillType][skillLevel] : 0;
    }

    public static short getSkillMP(byte clazz, byte skillType, byte skillLevel) {
        return (skillLevel > 0) ? SKILL_MP[clazz][skillType][skillLevel] : 0;
    }

    public static long getSkillCooldown(byte clazz, byte skillType, byte level) {
        return (level > 0) ? SKILL_COOLDOWN[clazz][skillType][level] : 0;
    }

    public static short getSkillRange(byte clazz, byte skillType) {
        return SKILL_RANGE[clazz][skillType];
    }

    public static TreeInfo getTreeInfo(byte id) {
        return TREE_INFOS.getOrDefault(id, null);
    }

    public static MonsterTemplate getMobTemplate(short mobId) {
        return MOB_TEMPLATES.getOrDefault(mobId, null);
    }

    public static ValueAttributeAnimal getValueAttributeAnimal(byte id) {
        return VALUE_ATTRIBUTE_ANIMAL.getOrDefault(id, null);
    }

    public static short getMaxValueAttributeAnimal(short id, byte level) {
        ValueAttributeAnimal value = VALUE_ATTRIBUTE_ANIMAL.getOrDefault(id, null);
        if (value == null) {
            return 0;
        }
        return value.getMaxValue()[level - 1];
    }

    private static void loadTypeOfTile() {
        try {
            String[] names = new String[]{"t_thanh", "t", "t_hang"};
            for (String name : names) {
                DataInputStream tile = new DataInputStream(new FileInputStream("data/map/typeOfTile/" + name));
                int[] typeOfTile = new int[tile.available()];
                for (int i = 0; i < typeOfTile.length; i++) {
                    typeOfTile[i] = tile.readByte();
                }
                TYPE_OF_TILE.add(typeOfTile);
            }
        } catch (IOException e) {
        }
    }

    public static PotionTemplate getPotionTemplate(short itemId) {
        return POTION_TEMPLATES.getOrDefault(itemId, null);
    }

    public static ItemQuestTemplate getItemQuestTemplate(byte itemId) {
        return ITEM_QUEST_TEMPLATES.getOrDefault(itemId, null);
    }

    public static ItemEquipTemplate getItemEquipment(byte type, byte style, byte color) {
        return ITEM_EQUIPMENTS.values().stream().filter(it -> it != null && it.getType() == type && it.getStyle() == style && it.getColorItem() == color).findFirst().orElse(null);
    }

    public static ItemEquipTemplate getItemEquipment(short itemId) {
        return ITEM_EQUIPMENTS.getOrDefault(itemId, null);
    }

    public static AttributeEquipTemplate getAttributeTemplate(short attId) {
        return ITEM_ATTRIBUTE_TEMPLATES.getOrDefault(attId, null);
    }

    public static GemTemplate getGemTemplate(short id) {
        return GEM_TEMPLATES.getOrDefault(id, null);
    }

    public static AnimalTemplate getAnimalTemplate(short id) {
        return ANIMAL_TEMPLATES.getOrDefault(id, null);
    }

    public static ShopTemplate getShopTemplate(short id) {
        return SHOP_TEMPLATES.getOrDefault(id, null);
    }

    public static List<SkillNewTemplate> getListSkillNew(byte classChar) {
        return SKILL_NEW_TEMPLATES.values().stream().filter(s -> s != null && s.getClassChar() == classChar).collect(Collectors.toList());
    }

    public static List<EffectData> getEffectData(byte typeEffect) {
        return EFFECT_DATA.stream().filter(s -> s != null && s.getTypeEffect() == typeEffect).sorted(Comparator.comparingInt(EffectData::getId)).collect(Collectors.toList());
    }

    public static PartChar getPartCharData(byte type, byte id) {
        return PART_CHAR.stream().filter(s -> s != null && s.getType() == type && s.getId() == id).findFirst().orElse(null);
    }

    public static EffectData getEffectData(byte typeEffect, short idEffect) {
        return EFFECT_DATA.stream().filter(s -> s != null && s.getTypeEffect() == typeEffect && s.getId() == idEffect).findFirst().orElse(null);
    }
    //</editor-fold>

    public static void init() {
        try {
            loadTypeOfTile();

            // <editor-fold defaultstate="collapsed" desc="Load Others Data">
            ResultSetImpl rs = HikariCP.executeQuery("SELECT * FROM `others`");
            while (rs.next()) {
                String type = rs.getString("type");
                String data = rs.getString("data");
                JSONArray arr = new JSONArray(data);
                switch (type) {
                    case "ANIMAL_TRAIN_PRICE" -> {
                        ANIMAL_TRAIN_PRICE = new byte[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            ANIMAL_TRAIN_PRICE[i] = (byte) arr.getInt(i);
                        }
                    }
                    case "ANIMAL_CHANGE_SPECIAL_ATTRIBUTE_PRICE" -> {
                        ANIMAL_CHANGE_SPECIAL_ATTRIBUTE_PRICE = (byte) arr.getInt(0);
                    }
                    case "PRICE_UPGRADE_ANIMAL" -> {
                        PRICE_UPGRADE_ANIMAL = new byte[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            PRICE_UPGRADE_ANIMAL[i] = (byte) arr.getInt(i);
                        }
                    }
                    case "MAX_VALUE_ATTRIBUTE_ANIMAL" -> {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray value = arr.getJSONArray(i);
                            JSONArray arrId = value.getJSONArray(0);
                            short[] valueAtt = new short[4];
                            for (int k = 1; k < value.length(); k++) {
                                valueAtt[k - 1] = (short) value.getInt(k);
                            }
                            for (int j = 0; j < arrId.length(); j++) {
                                ValueAttributeAnimal att = ValueAttributeAnimal.builder().id((short) arrId.getInt(j)).maxValue(valueAtt).build();
                                VALUE_ATTRIBUTE_ANIMAL.put(att.getId(), att);
                            }
                        }
                    }
                    case "MAX_VALUE_ATTRIBUTE_ANIMAL_SPECIAL" -> {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray value = arr.getJSONArray(i);
                            short[] valueAtt = new short[4];
                            for (int k = 1; k < value.length(); k++) {
                                valueAtt[k - 1] = (short) value.getInt(k);
                            }
                            ValueAttributeAnimal att = ValueAttributeAnimal.builder().id((byte) value.getInt(0)).maxValue(valueAtt).build();
                            VALUE_ATTRIBUTE_ANIMAL.put(att.getId(), att);
                        }
                    }
                    case "POTION_SHOP" -> {
                        POTION_SHOP = new short[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            POTION_SHOP[i] = (short) arr.getInt(i);
                        }
                    }
                    case "ID_MAP_PK" -> {
                        ID_MAP_PK = (short) arr.getInt(0);
                    }
                    case "MAIN_ATTACK" -> {
                        MAIN_ATTACK = new byte[arr.length()][];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray mainAtt = arr.getJSONArray(i);
                            MAIN_ATTACK[i] = new byte[mainAtt.length()];
                            for (int j = 0; j < mainAtt.length(); j++) {
                                MAIN_ATTACK[i][j] = (byte) mainAtt.getInt(j);
                            }
                        }
                    }
                    case "MAIN_DEFEND" -> {
                        MAIN_DEFEND = new byte[arr.length()][];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray mainDef = arr.getJSONArray(i);
                            MAIN_DEFEND[i] = new byte[mainDef.length()];
                            for (int j = 0; j < mainDef.length(); j++) {
                                MAIN_DEFEND[i][j] = (byte) mainDef.getInt(j);
                            }
                        }
                    }
                    case "PERCENT_ATTACK" -> {
                        PERCENT_ATTACK = new byte[arr.length()][];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray pcAtt = arr.getJSONArray(i);
                            PERCENT_ATTACK[i] = new byte[pcAtt.length()];
                            for (int j = 0; j < pcAtt.length(); j++) {
                                PERCENT_ATTACK[i][j] = (byte) pcAtt.getInt(j);
                            }
                        }
                    }
                    case "PERCENT_DEFEND" -> {
                        PERCENT_DEFEND = new byte[arr.length()][];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray pcDef = arr.getJSONArray(i);
                            PERCENT_DEFEND[i] = new byte[pcDef.length()];
                            for (int j = 0; j < pcDef.length(); j++) {
                                PERCENT_DEFEND[i][j] = (byte) pcDef.getInt(j);
                            }
                        }
                    }
                    case "ARENA" -> {
                        ARENA = new byte[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            ARENA[i] = (byte) arr.getInt(i);
                        }
                    }
                    case "KHAM_PROPERTY" -> {
                        KHAM_PROPERTY = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            KHAM_PROPERTY[i] = arr.getString(i);
                        }
                    }
                    case "ID_NGOC_KHAM" -> {
                        ID_NGOC_KHAM = new byte[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            ID_NGOC_KHAM[i] = (byte) arr.getInt(i);
                        }
                    }
                    case "TYPE_MP_HP" -> {
                        TYPE_MP_HP = new byte[arr.length()][];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray typeMpHp = arr.getJSONArray(i);
                            TYPE_MP_HP[i] = new byte[typeMpHp.length()];
                            for (int j = 0; j < typeMpHp.length(); j++) {
                                TYPE_MP_HP[i][j] = (byte) typeMpHp.getInt(j);
                            }
                        }
                    }
                    case "VALUE_MP_HP" -> {
                        VALUE_MP_HP = new int[arr.length()][];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONArray valueMpHp = arr.getJSONArray(i);
                            VALUE_MP_HP[i] = new int[valueMpHp.length()];
                            for (int j = 0; j < valueMpHp.length(); j++) {
                                VALUE_MP_HP[i][j] = valueMpHp.getInt(j);
                            }
                        }
                    }
                    case "DX_HORSE" -> {
                        DX_HORSE = new byte[arr.length()][];
                        for (byte i = 0; i < arr.length(); i++) {
                            JSONArray dxH = arr.getJSONArray(i);
                            DX_HORSE[i] = new byte[dxH.length()];
                            for (byte j = 0; j < dxH.length(); j++) {
                                DX_HORSE[i][j] = (byte) dxH.getInt(j);
                            }
                        }
                    }
                    case "DY_HORSE" -> {
                        DY_HORSE = new byte[arr.length()][];
                        for (byte i = 0; i < arr.length(); i++) {
                            JSONArray dyH = arr.getJSONArray(i);
                            DY_HORSE[i] = new byte[dyH.length()];
                            for (byte j = 0; j < dyH.length(); j++) {
                                DY_HORSE[i][j] = (byte) dyH.getInt(j);
                            }
                        }
                    }
                    case "DX_DY_WP" -> {
                        DX_DY_WP = new byte[arr.length()][];
                        for (byte i = 0; i < arr.length(); i++) {
                            JSONArray dataWp = arr.getJSONArray(i);
                            DX_DY_WP[i] = new byte[dataWp.length()];
                            for (byte j = 0; j < dataWp.length(); j++) {
                                DX_DY_WP[i][j] = (byte) dataWp.getInt(j);
                            }
                        }
                    }
                    case "DX_DY_AVT" -> {
                        DX_DY_AVT = new byte[arr.length()][];
                        for (byte i = 0; i < arr.length(); i++) {
                            JSONArray dataAvt = arr.getJSONArray(i);
                            DX_DY_AVT[i] = new byte[dataAvt.length()];
                            for (byte j = 0; j < dataAvt.length(); j++) {
                                DX_DY_AVT[i][j] = (byte) dataAvt.getInt(j);
                            }
                        }
                    }
                    case "DX_DY_PP" -> {
                        DX_DY_PP = new byte[arr.length()][];
                        for (byte i = 0; i < arr.length(); i++) {
                            JSONArray dataPp = arr.getJSONArray(i);
                            DX_DY_PP[i] = new byte[dataPp.length()];
                            for (byte j = 0; j < dataPp.length(); j++) {
                                DX_DY_PP[i][j] = (byte) dataPp.getInt(j);
                            }
                        }
                    }
                    case "LEVEL_ADD_SKILL" -> {
                        LEVEL_ADD_SKILL = new byte[15][];
                        for (int i = 0; i < 15; i++) {
                            LEVEL_ADD_SKILL[i] = new byte[11];
                            for (int j = 0; j < 11; j++) {
                                LEVEL_ADD_SKILL[i][j] = (byte) arr.getJSONArray(i).getInt(j);
                            }
                        }
                    }
                    case "SKILL_AEO" -> {
                        SKILL_AEO = new byte[5][];
                        for (int i = 0; i < 5; i++) {
                            JSONArray skilAeo = arr.getJSONArray(i);
                            SKILL_AEO[i] = new byte[skilAeo.length()];
                            for (int j = 0; j < SKILL_AEO[i].length; j++) {
                                SKILL_AEO[i][j] = (byte) skilAeo.getInt(j);
                            }
                        }
                    }
                    case "SKILL_COOLDOWN" -> {
                        SKILL_COOLDOWN = new int[arr.length()][][];
                        for (int k = 0; k < arr.length(); k++) {
                            SKILL_COOLDOWN[k] = new int[15][];
                            for (int i = 0; i < 15; i++) {
                                SKILL_COOLDOWN[k][i] = new int[11];
                                for (int j = 0; j < 11; j++) {
                                    SKILL_COOLDOWN[k][i][j] = arr.getJSONArray(k).getJSONArray(i).getInt(j);
                                }
                            }
                        }
                    }
                    case "SKILL_DAM_PERCENT" -> {
                        SKILL_DAM_PERCENT = new short[arr.length()][][];
                        for (int k = 0; k < arr.length(); k++) {
                            SKILL_DAM_PERCENT[k] = new short[15][];
                            for (int i = 0; i < 15; i++) {
                                SKILL_DAM_PERCENT[k][i] = new short[11];
                                for (int j = 0; j < 11; j++) {
                                    SKILL_DAM_PERCENT[k][i][j] = (short) arr.getJSONArray(k).getJSONArray(i).getInt(j);
                                }
                            }
                        }
                    }
                    case "SKILL_MP" -> {
                        SKILL_MP = new short[arr.length()][][];
                        for (int k = 0; k < arr.length(); k++) {
                            SKILL_MP[k] = new short[15][];
                            for (int i = 0; i < 15; i++) {
                                SKILL_MP[k][i] = new short[11];
                                for (int j = 0; j < 11; j++) {
                                    SKILL_MP[k][i][j] = (short) arr.getJSONArray(k).getJSONArray(i).getInt(j);
                                }
                            }
                        }
                    }
                    case "SKILL_RANGE" -> {
                        SKILL_RANGE = new short[arr.length()][];
                        for (int k = 0; k < arr.length(); k++) {
                            SKILL_RANGE[k] = new short[15];
                            for (int i = 0; i < 15; i++) {
                                SKILL_RANGE[k][i] = (short) arr.getJSONArray(k).getInt(i);
                            }
                        }
                    }
                    case "TIME_LIFE_BUFF_SKILL" -> {
                        TIME_LIFE_BUFF_SKILL = new short[15][];
                        for (int i = 0; i < 15; i++) {
                            TIME_LIFE_BUFF_SKILL[i] = new short[11];
                            for (int j = 0; j < 11; j++) {
                                TIME_LIFE_BUFF_SKILL[i][j] = (short) arr.getJSONArray(i).getInt(j);
                            }
                        }
                    }
                    case "EXP" -> {
                        exps = new long[arr.length()];
                        for (int i = 0; i < exps.length; i++) {
                            exps[i] = arr.getLong(i);
                        }
                    }
                }
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen("Finish Load Others Data");

            // <editor-fold defaultstate="collapsed" desc="Load Monsters Template">
            rs = HikariCP.executeQuery("SELECT * FROM `monsters`");
            while (rs.next()) {
                MonsterTemplate mobTemplate = MonsterTemplate.builder().id(rs.getShort("id")).name(rs.getString("name")).type(rs.getByte("type")).level(rs.getShort("level")).maxHp(rs.getInt("maxHp")).palate(rs.getByte("palate")).spalate(rs.getByte("spalate")).moveType(rs.getByte("moveType")).speed(rs.getByte("speed")).height(rs.getByte("height")).w(rs.getByte("w")).h(rs.getByte("h")).xCenter(rs.getByte("xCenter")).yCenter(rs.getByte("yCenter")).build();
                MOB_TEMPLATES.put(mobTemplate.getId(), mobTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Monsters Template [%s]", MOB_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Npc Template">
            rs = HikariCP.executeQuery("SELECT * FROM `npc_actor`");
            while (rs.next()) {
                JSONArray itemsArray = new JSONArray(rs.getString("itemBody"));
                short[] itemEquipment = new short[itemsArray.length()];
                for (int i = 0; i < itemEquipment.length; i++) {
                    itemEquipment[i] = (short) itemsArray.getInt(i);
                }
                JSONArray idModelArray = new JSONArray(rs.getString("idModels"));
                short[] idModels = new short[idModelArray.length()];
                for (int i = 0; i < idModels.length; i++) {
                    idModels[i] = (short) idModelArray.getInt(i);
                }
                NpcTemplate npcTemplate = NpcTemplate.builder().id(rs.getShort("id")).name(rs.getString("name")).head(rs.getShort("head")).itemEquipment(itemEquipment).idModels(idModels).build();
                NPC_TEMPLATES.put(npcTemplate.getId(), npcTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Npc Template [%s]", NPC_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Tree Info">
            rs = HikariCP.executeQuery("SELECT * FROM `tree_info`");
            while (rs.next()) {
                TreeInfo treeInfo = TreeInfo.builder().id(rs.getByte("id")).dx(rs.getByte("dx")).dy(rs.getByte("dy")).startX(rs.getByte("startX")).startY(rs.getByte("startY")).endX(rs.getByte("endX")).endY(rs.getByte("endY")).build();
                treeInfo.setData();
                TREE_INFOS.put(treeInfo.getId(), treeInfo);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Tree Info [%s]", TREE_INFOS.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Item Equipment">
            rs = HikariCP.executeQuery("SELECT * FROM item_equipment");
            while (rs.next()) {
                JSONArray attb = new JSONArray(rs.getString("attribute"));
                short[] attribute = new short[attb.length()];
                for (int i = 0; i < attb.length(); i++) {
                    attribute[i] = (short) attb.getInt(i);
                }
                JSONArray dxdy = new JSONArray(rs.getString("dataWeapon"));
                byte dx = dxdy.length() > 0 ? (byte) dxdy.getInt(0) : Manager.DX_DY_WP[0][0];
                byte dy = dxdy.length() > 0 ? (byte) dxdy.getInt(1) : Manager.DX_DY_WP[1][0];
                ItemEquipTemplate itemTemplate = ItemEquipTemplate.builder().id(rs.getShort("id")).name(rs.getString("name")).classChar(rs.getByte("classChar")).idIcon(rs.getShort("idIcon")).type(rs.getByte("type")).style(rs.getByte("stype")).he(rs.getByte("he")).gender(rs.getByte("gender")).level(rs.getByte("level")).durable(rs.getShort("durable")).price(rs.getInt("price")).colorItem(rs.getByte("colorItem")).ndayLoan(rs.getShort("ndayLoan")).attribute(attribute).dxWear(dx).dyWear(dy).build();
                ITEM_EQUIPMENTS.put(itemTemplate.getId(), itemTemplate);
                if (itemTemplate.getColorItem() == 0 && itemTemplate.getNdayLoan() == 0) {
                    Util.addItemToMap(ITEM_EQUIPMENT, itemTemplate.getLevel() + "_" + itemTemplate.getGender(), itemTemplate.getId());
                }
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Item Template [%s]", ITEM_EQUIPMENTS.size()));

            // <editor-fold defaultstate="collapsed" desc="load Item Attribute Template">
            rs = HikariCP.executeQuery("SELECT * FROM `item_attribute`");
            while (rs.next()) {
                AttributeEquipTemplate itemAttributeTemplate = AttributeEquipTemplate.builder().id(rs.getShort("id")).name(rs.getString("name")).isPercent(rs.getByte("isPercent")).colorPaint(rs.getByte("colorPaint")).build();
                ITEM_ATTRIBUTE_TEMPLATES.put(itemAttributeTemplate.getId(), itemAttributeTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Attribute Template [%s]", ITEM_ATTRIBUTE_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Item Quest Template">
            rs = HikariCP.executeQuery("SELECT * FROM `item_quest`");
            while (rs.next()) {
                ItemQuestTemplate itemQuestTemplate = ItemQuestTemplate.builder().id(rs.getByte("id")).name(rs.getString("name")).idIcon(rs.getShort("iconId")).build();
                ITEM_QUEST_TEMPLATES.put(itemQuestTemplate.getId(), itemQuestTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Item Quest Template [%s]", ITEM_QUEST_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Xa Phu Template">
            rs = HikariCP.executeQuery("SELECT * FROM `xa_phu_template`;");
            while (rs.next()) {
                byte id = rs.getByte("id");
                JSONArray mapIdArray = new JSONArray(rs.getString("mapId"));
                short[] mapId = new short[mapIdArray.length()];
                for (int i = 0; i < mapIdArray.length(); i++) {
                    mapId[i] = (short) mapIdArray.getInt(i);
                }
                JSONArray xArray = new JSONArray(rs.getString("x"));
                short[] x = new short[xArray.length()];
                for (int i = 0; i < xArray.length(); i++) {
                    x[i] = (short) xArray.getInt(i);
                }
                JSONArray yArray = new JSONArray(rs.getString("y"));
                short[] y = new short[yArray.length()];
                for (int i = 0; i < yArray.length(); i++) {
                    y[i] = (short) yArray.getInt(i);
                }
                JSONArray priceArray = new JSONArray(rs.getString("price"));
                short[] price = new short[priceArray.length()];
                for (int i = 0; i < priceArray.length(); i++) {
                    price[i] = (short) priceArray.getInt(i);
                }
                XaPhuTemplate xaPhuTemplate = XaPhuTemplate.builder().id(id).mapID(mapId).x(x).y(y).price(price).build();
                XA_PHU_TEMPLATE.put(id, xaPhuTemplate);
            }
            rs.close();
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Load Xa Phu">
            rs = HikariCP.executeQuery("SELECT * FROM `xa_phu`");
            while (rs.next()) {
                XaPhu xaPhu = XaPhu.builder().id(rs.getByte("id")).idMap(rs.getShort("map")).x(rs.getShort("x")).y(rs.getShort("y")).build();
                XA_PHU.add(xaPhu);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Xa Phu [%s]", XA_PHU_TEMPLATE.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Gem Template">
            rs = HikariCP.executeQuery("SELECT * FROM `gem_template`");
            while (rs.next()) {
                GemTemplate gem = GemTemplate.builder().id(rs.getShort("id")).idImage(rs.getByte("idImage")).price(rs.getInt("price")).name(rs.getString("name")).decript(rs.getString("decript")).type(rs.getByte("type")).isSell(rs.getBoolean("isSell")).typeEp(rs.getByte("typeEp")).typeMoney(rs.getByte("typeMoney")).build();
                GEM_TEMPLATES.put(gem.getId(), gem);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Gem Template [%s]", GEM_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Shop Template">
            rs = HikariCP.executeQuery("SELECT * FROM shop_template");
            while (rs.next()) {
                ShopTemplate shopTemplate = ShopTemplate.builder().id(rs.getShort("id")).idImage(rs.getShort("idImage")).name(rs.getString("name")).decript(rs.getString("decript")).shopType(rs.getByte("shopType")).price(rs.getInt("price")).typeMoney(rs.getByte("typeMoney")).isSell(rs.getBoolean("isSell")).value(rs.getShort("value")).idItemEquip(rs.getShort("idItemEquip")).idItemPotion(rs.getShort("idItemPotion")).idItemGem(rs.getShort("idItemGem")).isGemLock(rs.getByte("isGemLock") == 1).build();
                SHOP_TEMPLATES.put(shopTemplate.getId(), shopTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Shop Template [%s]", SHOP_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Potion Template">
            rs = HikariCP.executeQuery("SELECT * FROM `potion_template`");
            while (rs.next()) {
                PotionTemplate potionTemplate = PotionTemplate.builder().id(rs.getShort("id")).name(rs.getString("name")).name2(rs.getString("name2")).idImage(rs.getByte("idImage")).delay(rs.getShort("delay")).isTrade(rs.getBoolean("isTrade")).price(rs.getShort("price")).recovered(rs.getShort("recovered")).build();
                POTION_TEMPLATES.put(potionTemplate.getId(), potionTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Potion Template [%s]", POTION_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Skill News Template">
            rs = HikariCP.executeQuery("SELECT * FROM `skill_news`");
            while (rs.next()) {
                SkillNewTemplate skillNewTemplate = SkillNewTemplate.builder().id(rs.getShort("id")).name(rs.getString("name")).decript(rs.getString("des")).idSkill(rs.getByte("idSkill")).classChar(rs.getByte("charClass")).price(rs.getInt("price")).build();
                SKILL_NEW_TEMPLATES.put(skillNewTemplate.getId(), skillNewTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Skill New Template [%s]", SKILL_NEW_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Part Char Data">
            rs = HikariCP.executeQuery("SELECT * FROM `char_part`");
            while (rs.next()) {
                JSONArray xArray = new JSONArray(rs.getString("x"));
                byte[][] x = new byte[xArray.length()][];
                for (int i = 0; i < xArray.length(); i++) {
                    JSONArray xData = xArray.getJSONArray(i);
                    x[i] = new byte[xData.length()];
                    for (int j = 0; j < xData.length(); j++) {
                        x[i][j] = (byte) xData.getInt(j);
                    }
                }
                JSONArray yArray = new JSONArray(rs.getString("y"));
                byte[][] y = new byte[yArray.length()][];
                for (int i = 0; i < yArray.length(); i++) {
                    JSONArray yData = yArray.getJSONArray(i);
                    y[i] = new byte[yData.length()];
                    for (int j = 0; j < yData.length(); j++) {
                        y[i][j] = (byte) yData.getInt(j);
                    }
                }
                JSONArray wArray = new JSONArray(rs.getString("w"));
                byte[][] w = new byte[wArray.length()][];
                for (int i = 0; i < wArray.length(); i++) {
                    JSONArray wData = wArray.getJSONArray(i);
                    w[i] = new byte[wData.length()];
                    for (int j = 0; j < wData.length(); j++) {
                        w[i][j] = (byte) wData.getInt(j);
                    }
                }
                JSONArray hArray = new JSONArray(rs.getString("h"));
                byte[][] h = new byte[hArray.length()][];
                for (int i = 0; i < hArray.length(); i++) {
                    JSONArray hData = hArray.getJSONArray(i);
                    h[i] = new byte[hData.length()];
                    for (int j = 0; j < hData.length(); j++) {
                        h[i][j] = (byte) hData.getInt(j);
                    }
                }
                JSONArray dxArray = new JSONArray(rs.getString("dx"));
                byte[][] dx = new byte[dxArray.length()][];
                for (int i = 0; i < dxArray.length(); i++) {
                    JSONArray dxData = dxArray.getJSONArray(i);
                    dx[i] = new byte[dxData.length()];
                    for (int j = 0; j < dxData.length(); j++) {
                        dx[i][j] = (byte) dxData.getInt(j);
                    }
                }
                JSONArray dyArray = new JSONArray(rs.getString("dy"));
                byte[][] dy = new byte[dyArray.length()][];
                for (int i = 0; i < dyArray.length(); i++) {
                    JSONArray dyData = dyArray.getJSONArray(i);
                    dy[i] = new byte[dyData.length()];
                    for (int j = 0; j < dyData.length(); j++) {
                        dy[i][j] = (byte) dyData.getInt(j);
                    }
                }
                PartChar part = PartChar.builder().x(x).y(y).dx(dx).dy(dy).w(w).h(h).id(rs.getByte("idPart")).type(rs.getByte("type")).avx0(rs.getShort("avx0")).avy0(rs.getShort("avy0")).avw0(rs.getShort("avw0")).avh0(rs.getShort("avh0")).avxf(rs.getShort("avxf")).avyf(rs.getShort("avyf")).build();
                part.setData();
                PART_CHAR.add(part);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Part Char Data [%s]", PART_CHAR.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Effect Data">
            rs = HikariCP.executeQuery("SELECT * FROM `effect_data` ORDER BY `typeEffect`");
            while (rs.next()) {
                short idEffect = rs.getShort("idEffect");
                byte idSpecial = rs.getByte("idShadow");
                byte typeEffect = rs.getByte("typeEffect");
                JSONArray smallImage = new JSONArray(rs.getString("smallImage"));
                ImageInfo[] imageInfo = new ImageInfo[smallImage.length()];
                for (int i = 0; i < smallImage.length(); i++) {
                    JSONObject dataImage = smallImage.getJSONObject(i);
                    ImageInfo info = ImageInfo.builder().ID((short) dataImage.getInt("id")).x0((short) dataImage.getInt("x0")).y0((short) dataImage.getInt("y0")).w((byte) dataImage.getInt("w")).h((byte) dataImage.getInt("h")).build();
                    imageInfo[i] = info;
                }
                JSONArray frameImage = new JSONArray(rs.getString("frames"));
                PartFrame[][] partFrame = new PartFrame[frameImage.length()][];
                for (int i = 0; i < frameImage.length(); i++) {
                    JSONArray smallPart = frameImage.getJSONArray(i);
                    partFrame[i] = new PartFrame[smallPart.length()];
                    for (int k = 0; k < smallPart.length(); k++) {
                        JSONObject partData = smallPart.getJSONObject(k);
                        PartFrame part = PartFrame.builder().idSmallImg((byte) partData.getInt("id")).dx((short) partData.getInt("dx")).dy((short) partData.getInt("dy")).onTop((byte) partData.getInt("onTop")).flip((byte) partData.getInt("flip")).xShadow((byte) partData.getInt("xShadow")).yShadow((byte) partData.getInt("yShadow")).build();
                        partFrame[i][k] = part;
                    }
                }
                JSONArray sequenceData = new JSONArray(rs.getString("sequence"));
                byte[] sequence = new byte[sequenceData.length()];
                for (int i = 0; i < sequenceData.length(); i++) {
                    sequence[i] = (byte) sequenceData.getInt(i);
                }
                JSONArray animationData = new JSONArray(rs.getString("animations"));
                Animation[] animatinos = new Animation[animationData.length()];
                for (int i = 0; i < animationData.length(); i++) {
                    JSONArray animationSmall = animationData.getJSONArray(i);
                    byte[] data = new byte[animationSmall.length()];
                    for (int k = 0; k < animationSmall.length(); k++) {
                        data[k] = (byte) animationSmall.getInt(k);
                    }
                    animatinos[i] = new Animation(data);
                }
                EffectData effectData = EffectData.builder().id(idEffect).idSpecial(idSpecial).typeEffect(typeEffect).imageInfo(imageInfo).frame(partFrame).arrFrame(sequence).animation(animatinos).build();
                effectData.setData();
                EFFECT_DATA.add(effectData);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Effect Data [%s]", EFFECT_DATA.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Animal Template">
            rs = HikariCP.executeQuery("SELECT * FROM `animal_template`");
            while (rs.next()) {
                JSONArray attributeJSON = new JSONArray(rs.getString("attributeSpecial"));
                ConcurrentHashMap<Short, ValueAttributeAnimal> attribute = new ConcurrentHashMap<>();
                for (int i = 0; i < attributeJSON.length(); i++) {
                    JSONArray arr = attributeJSON.getJSONArray(i);
                    short[] mValue = new short[1];
                    ValueAttributeAnimal value = ValueAttributeAnimal.builder().id((short) arr.getInt(0)).maxValue(mValue).build();
                    mValue[0] = (short) arr.getInt(1);
                    attribute.put(value.getId(), value);
                }
                AnimalTemplate animal = AnimalTemplate.builder().idTrung(rs.getShort("idTrung")).name(rs.getString("name")).idImage(rs.getShort("idImg")).type(rs.getByte("type")).nFrame(rs.getByte("nFrame")).attributeSpecial(attribute).build();
                ANIMAL_TEMPLATES.put(animal.getIdTrung(), animal);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Animal Templates [%s]", ANIMAL_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Npc Server">
            rs = HikariCP.executeQuery("SELECT * FROM `npc_server`");
            while (rs.next()) {
                NpcServerTemplate npc = NpcServerTemplate.builder().id(rs.getShort("id")).name(rs.getString("name")).idImage(rs.getShort("idImg")).w0(rs.getShort("w0")).h0(rs.getShort("h0")).frame(rs.getByte("frame")).typeLimit(rs.getByte("typeLimit")).build();
                NPC_SERVER_TEMPLATES.put(npc.getId(), npc);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Npc Server Templates [%s]", NPC_SERVER_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Npc Shop">
            rs = HikariCP.executeQuery("SELECT * FROM `npc_shop`");
            while (rs.next()) {
                NpcShop npc = NpcShop.builder().id(rs.getByte("id")).nameShop(rs.getString("nameShop")).typeShop(rs.getByte("typeShop")).build();
                JSONArray listItem = new JSONArray(rs.getString("items"));
                short[] id = new short[listItem.length()];
                for (int i = 0; i < listItem.length(); i++) {
                    id[i] = (short) listItem.getInt(i);
                }
                npc.setIdItems(id);
                SHOP_NPC_TEMPLATES.put(npc.getNameShop(), npc);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Npc Shop [%s]", SHOP_NPC_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Data Map">
            for (byte country = 0; country < 2; country++) {
                ConcurrentHashMap<Short, IMap> listMaps = new ConcurrentHashMap<>();
                rs = HikariCP.executeQuery(String.format("SELECT * FROM `maps` WHERE `id` != '%s';", country == Const.THANH_LONG ? 0 : 70));
                while (rs.next()) {
                    short id = rs.getShort("id");
                    String name = Util.removeControlCharacters(rs.getString("name"));
                    JSONArray data = new JSONArray(rs.getString("data"));
                    JSONArray sizeMap = data.getJSONArray(0);
                    int w = sizeMap.getInt(0);
                    int h = sizeMap.getInt(1);
                    JSONArray maps = data.getJSONArray(1);
                    short[] map = new short[maps.length()];
                    int[] type = new int[maps.length()];
                    int[] typeOfTile;
                    if (id > 200) {
                        typeOfTile = TYPE_OF_TILE.get(0);
                    } else if (id < 110) {
                        typeOfTile = TYPE_OF_TILE.get(1);
                    } else {
                        typeOfTile = TYPE_OF_TILE.get(2);
                    }
                    for (int i = 0; i < maps.length(); i++) {
                        map[i] = (short) maps.getInt(i);
                        if (map[i] == 254) {
                            map[i] = 255;
                        }
                        if (map[i] != 255 && map[i] != -1) {
                            type[i] = typeOfTile[map[i]];
                        }
                    }
                    List<Actor> tileTops = new ArrayList<>();
                    List<Actor> tileTops2 = new ArrayList<>();
                    JSONArray tileArray = new JSONArray(rs.getString("tileTop"));
                    for (int i = 0; i < tileArray.length(); i++) {
                        JSONArray arr = tileArray.getJSONArray(i);
                        for (int j = 0; j < arr.length(); j++) {
                            JSONArray tileJSONArray = arr.getJSONArray(j);
                            if (tileJSONArray.length() == 5) {
                                JSONArray idTitles = tileJSONArray.getJSONArray(0);
                                short[] indexs = new short[idTitles.length()];
                                for (int l = 0; l < indexs.length; l++) {
                                    indexs[l] = (short) idTitles.getInt(l);
                                }
                                Actor tile = Actor.builder().indexs(indexs).x((short) tileJSONArray.getInt(1)).sizeX(tileJSONArray.getInt(2)).sizeY(tileJSONArray.getInt(3)).plusY(tileJSONArray.getInt(4)).build();
                                tileTops.add(tile);
                            } else {
                                Actor tile = Actor.builder().index(tileJSONArray.getInt(0)).x((short) tileJSONArray.getInt(1)).y((short) tileJSONArray.getInt(2)).sizeY(-1).build();
                                tileTops2.add(tile);
                            }
                        }
                    }
                    List<Actor> trees = new ArrayList<>();
                    JSONArray treeArray = new JSONArray(rs.getString("tree"));
                    for (int i = 0; i < treeArray.length(); i++) {
                        JSONArray arr = treeArray.getJSONArray(i);
                        Actor tree = Actor.builder().index(arr.getInt(0)).x((short) arr.getInt(1)).y((short) arr.getInt(2)).build();
                        trees.add(tree);
                    }
                    List<Player> npcsActor = new ArrayList<>();
                    JSONArray npcActorArray = new JSONArray(rs.getString("npcsActor"));
                    for (int i = 0; i < npcActorArray.length(); i++) {
                        JSONArray arr = npcActorArray.getJSONArray(i);
                        Player npcActor = PlayerDAO.buildNpcActor(getNpcTemplate((short) arr.getInt(0)), (short) arr.getInt(1), (short) arr.getInt(2));
                        npcActor.setIdPlayer((short) (i - 32000));
                        npcsActor.add(npcActor);
                    }
                    List<Actor> npcs = new ArrayList<>();
                    JSONArray npcArray = new JSONArray(rs.getString("npcs"));
                    for (int i = 0; i < npcArray.length(); i++) {
                        JSONArray arr = npcArray.getJSONArray(i);
                        Actor npc = Actor.builder().index(arr.getInt(0)).x((short) arr.getInt(1)).y((short) arr.getInt(2)).build();
                        npcs.add(npc);
                    }
                    List<LoctionWayPoint> locationWayPoints = new ArrayList<>();
                    JSONArray locationWpArray = new JSONArray(rs.getString("locationWayPoints"));
                    for (int i = 0; i < locationWpArray.length(); i++) {
                        JSONArray arr = locationWpArray.getJSONArray(i);
                        LoctionWayPoint wp = LoctionWayPoint.builder().toMap((short) arr.getInt(0)).toX((short) arr.getInt(1)).toY((short) arr.getInt(2)).x((short) arr.getInt(3)).y((short) arr.getInt(4)).build();
                        type[wp.getY() * w + wp.getX()] = 2000000000 + i;
                        locationWayPoints.add(wp);
                    }

                    JSONArray WpArray = new JSONArray(rs.getString("wayPoints"));
                    WayPoint[][][] WayPoints = new WayPoint[WpArray.length()][][];
                    for (int i = 0; i < WpArray.length(); i++) {
                        JSONArray arr = WpArray.getJSONArray(i);
                        WayPoints[i] = new WayPoint[arr.length()][];
                        for (int j = 0; j < arr.length(); j++) {
                            JSONArray arr2 = arr.getJSONArray(j);
                            WayPoints[i][j] = new WayPoint[arr2.length()];
                            for (int k = 0; k < arr2.length(); k++) {
                                JSONArray wpJSONArray = arr2.getJSONArray(k);
                                WayPoint wp = WayPoint.builder().nameWayPoint(Util.removeControlCharacters(wpJSONArray.getString(0))).toMap((short) wpJSONArray.getInt(1)).toX((short) wpJSONArray.getInt(2)).toY((short) wpJSONArray.getInt(3)).build();
                                WayPoints[i][j][k] = wp;
                            }
                        }
                    }
                    List<ChildMap> childMaps = new ArrayList<>();
                    JSONArray chillArray = new JSONArray(rs.getString("childMap"));
                    for (int i = 0; i < chillArray.length(); i++) {
                        JSONArray arr = chillArray.getJSONArray(i);
                        ChildMap chillMap = ChildMap.builder().id((short) arr.getInt(0)).name(Util.removeControlCharacters(arr.getString(1))).build();
                        childMaps.add(chillMap);
                    }
                    List<NpcServer> npcServers = new ArrayList<>();
                    JSONArray npcNArray = new JSONArray(rs.getString("npcServer"));
                    for (int i = 0; i < npcNArray.length(); i++) {
                        JSONArray arr = npcNArray.getJSONArray(i);
                        NpcServer npc = NpcServer.builder().template(getNpcServerTemplate((short) arr.getInt(0))).x((short) arr.getInt(1)).y((short) arr.getInt(2)).build();
                        npcServers.add(npc);
                    }
                    List<Monster> mobs = new ArrayList<>();
                    JSONArray mobArray = new JSONArray(rs.getString("mobs"));
                    for (short i = 0; i < mobArray.length(); i++) {
                        JSONArray arr = mobArray.getJSONArray(i);
                        short idTemp = (short) arr.getInt(0);
                        MonsterTemplate template = getMobTemplate(idTemp);
                        Monster mob = Monster.builder().id(i).x((short) arr.getInt(1)).hp(template.getMaxHp()).y((short) arr.getInt(2)).template(template).build();
                        mobs.add(mob);
                    }
                    MapData mapData = MapData.builder().npcServer(npcServers).npcsActor(npcsActor).idHoaTieu(rs.getByte("idHoaTieu")).idXaPhu(rs.getByte("idXaPhu")).maxZone(rs.getByte("maxZone")).w((short) w).h((short) h).map(map).type(type).isOfflineMap(rs.getBoolean("isOfflineMap")).mobsOrigin(mobs).tileTops(tileTops).tileTops2(tileTops2).trees(trees).wayPoints(WayPoints).locationWayPoints(locationWayPoints).npcs(npcs).build();
                    Map m = Map.builder().id(id).name(name).childMaps(childMaps).country(Const.THANH_LONG).mapData(mapData).build();
                    m.initZone();
                    m.initChildMap();
                    m.startUpdateMap();
                    listMaps.put(m.getId(), m);
                }
                MAPS.put(country, listMaps);
                rs.close();
            }
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Map Thanh Long [%s]", MAPS.get(Const.THANH_LONG).size()));

            Printer.printGreen(String.format("Finish Load Map Hac Ho [%s]", MAPS.get(Const.HAC_HO).size()));

            // <editor-fold defaultstate="collapsed" desc="Load Hoa Tieu Template">
            rs = HikariCP.executeQuery("SELECT * FROM `hoa_tieu_template`");
            while (rs.next()) {
                byte id = rs.getByte("id");
                JSONArray mapIdArray = new JSONArray(rs.getString("mapId"));
                short[] mapId = new short[mapIdArray.length()];
                for (int i = 0; i < mapIdArray.length(); i++) {
                    mapId[i] = (short) mapIdArray.getInt(i);
                }
                JSONArray xArray = new JSONArray(rs.getString("x"));
                short[] x = new short[xArray.length()];
                for (int i = 0; i < xArray.length(); i++) {
                    x[i] = (short) xArray.getInt(i);
                }
                JSONArray yArray = new JSONArray(rs.getString("y"));
                short[] y = new short[yArray.length()];
                for (int i = 0; i < yArray.length(); i++) {
                    y[i] = (short) yArray.getInt(i);
                }
                String[][] mapNameChild = new String[mapId.length][];
                String[] mapName = new String[mapId.length];
                short[][] mapIdChild = new short[mapId.length][];
                for (int i = 0; i < mapId.length; i++) {
                    IMap map = getMap(Const.THANH_LONG, mapId[i]);
                    mapNameChild[i] = new String[map.getChildMaps().size() + 1];
                    mapIdChild[i] = new short[map.getChildMaps().size() + 1];
                    mapNameChild[i][0] = map.getName();
                    mapIdChild[i][0] = map.getMapId();
                    mapName[i] = map.getName();
                    for (int k = 0; k < map.getChildMaps().size(); k++) {
                        IMap childMap = map.getChildMaps().get(k);
                        mapNameChild[i][k + 1] = childMap.getName();
                        mapIdChild[i][k + 1] = childMap.getMapId();
                    }
                }
                HoaTieuTemplate hoaTieuTemplate = HoaTieuTemplate.builder().id(id).mapId(mapIdChild).x(x).y(y).nameMapChild(mapNameChild).nameMap(mapName).build();
                HOA_TIEU_TEMPLATES.put(id, hoaTieuTemplate);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Hoa Tieu Templates [%s]", HOA_TIEU_TEMPLATES.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Clan">
            rs = HikariCP.executeQuery("SELECT * FROM `clan`");
            while (rs.next()) {
                String nameLeader = rs.getString("nameLeader");
                String nameClan = rs.getString("name");
                String slogan = rs.getString("slogan");
                short indexIcon = rs.getShort("indexIcon");
                byte nationId = rs.getByte("nationId");
                byte level = rs.getByte("level");
                long exp = rs.getLong("exp");
                long dedicationPoint = rs.getLong("dedicationPoint");
                long xu = rs.getLong("xu");
                long lastTimeCreate = rs.getLong("lastTimeCreate");
                long lastTimeEndDelete = rs.getLong("lastTimeEndDelete");
                boolean solve = rs.getByte("dissolve") == 1;
                List<Friend> members = PlayerDAO.buildListFriend(rs.getString("members"));
                Clan clan = Clan.builder().nameLeader(nameLeader).name(nameClan).slogan(slogan).indexIcon(indexIcon).dissolve(solve).nationID(nationId).level(level).exp(exp).dedicationPoint(dedicationPoint).xu(xu).members(members).membersOnGame(new ArrayList<>()).messages(new ArrayList<>()).date(Util.convertTimeToString(lastTimeCreate)).lastTimeCreate(lastTimeCreate).lastTimeEndDelete(lastTimeEndDelete).build();
                ClanManager.CLANS.put(indexIcon, clan);
            }
            rs.close();
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Clan [%s]", ClanManager.CLANS.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Default Deposite">
            for (byte idNpc = NpcConst.NHAT_GIAP; idNpc <= NpcConst.NHAT_NGUU; idNpc++) {
                for (byte i = 0; i < 3; i++) {
                    String name = String.format("%s_%s", idNpc, i);
                    Deposite deposite = Deposite.builder().nameDeposite(name).playerSell(new ArrayList<>()).build();
                    DEPOSITE.put(name, deposite);
                }
            }
            // </editor-fold>

            Printer.printGreen(String.format("Finish Load Default Deposite [%s]", DEPOSITE.size()));

            // <editor-fold defaultstate="collapsed" desc="Load Icon Data">
            ICON_ANIMAL = Util.readFile("data\\image\\animal.png");
            ICON_POTION = Util.readFile("data\\image\\potion\\potion.png");
            ICON_ITEM = Util.readFile("data\\image\\potion\\item.png");
            ICON_SKILL = new byte[5][];
            for (int i = 0; i < 5; i++) {
                ICON_SKILL[i] = Util.readFile("data\\image\\skill\\" + i + ".png");
            }
            File folder = new File("data\\image\\icon");
            for (File file : folder.listFiles()) {
                IMAGES_DEFAULT.put(Short.valueOf(Util.getFileNameWithoutExtension(file.getName())), Util.readFile(file));
            }
            Printer.printGreen(String.format("Finish Load Images Default [%s]", IMAGES_DEFAULT.size()));

            folder = new File("data\\image\\horse");
            @Cleanup("clear")
            List<File> fileList = new ArrayList<>();
            Arrays.stream(folder.listFiles())
                    .sorted(Comparator.comparing(File::getName, new NumericStringComparator()))
                    .forEach(a -> fileList.add(a));
            DATA_HORSE = new byte[fileList.size()][];
            HEAD_HORSE = new byte[fileList.size()][][];
            for (int i = 0; i < fileList.size(); i++) {
                File[] files = fileList.get(i).listFiles();
                HEAD_HORSE[i] = new byte[files.length][];
                int splitLocation = Util.findSmallestFileSize(files) / 2;
                for (int j = 0; j < files.length; j++) {
                    File file = files[j];
                    byte[][] data = Util.readFileAndSplit(file, splitLocation);
                    HEAD_HORSE[i][j] = data[0];
                    DATA_HORSE[i] = data[1];
                }
            }
            Printer.printGreen(String.format("Finish Load Images Horse [%s]", DATA_HORSE.length));

            folder = new File("data\\map\\mobImage");
            for (File file : folder.listFiles()) {
                short id = Short.parseShort(Util.getFileNameWithoutExtension(file.getName()));
                IMAGES_MONSTER.put(id, Util.readFile(file));
            }
            Printer.printGreen(String.format("Finish Load Images Monster [%s]", IMAGES_MONSTER.size()));

            folder = new File("data\\image\\animal");
            for (File file : folder.listFiles()) {
                byte id = Byte.parseByte(Util.getFileNameWithoutExtension(file.getName()));
                IMAGES_ANIMAL.put(id, Util.readFile(file));
            }
            Printer.printGreen(String.format("Finish Load Images Animal [%s]", IMAGES_ANIMAL.size()));

            folder = new File("data\\image\\tree");
            for (File file : folder.listFiles()) {
                byte id = Byte.parseByte(Util.getFileNameWithoutExtension(file.getName()));
                byte[] data = Util.concatenate(Util.readFile(file), getTreeInfo(id).getData());
                IMAGES_TREE.put(id, data);
            }
            Printer.printGreen(String.format("Finish Load Images Tree [%s]", IMAGES_TREE.size()));

            folder = new File("data\\image\\weapon");
            for (File file : folder.listFiles()) {
                IMAGES_WEAPON.put(Short.valueOf(Util.getFileNameWithoutExtension(file.getName())), Util.readFileAndSplit(file));
            }
            Printer.printGreen(String.format("Finish Load Images Weapon [%s]", IMAGES_WEAPON.size()));

            folder = new File("data\\image\\cloth");
            for (File folderChild : folder.listFiles()) {
                byte idParent = Byte.parseByte(Util.getFileNameWithoutExtension(folderChild.getName()));
                ConcurrentHashMap<Byte, byte[]> clothes = new ConcurrentHashMap<>();
                for (File file : folderChild.listFiles()) {
                    byte id = Byte.parseByte(Util.getFileNameWithoutExtension(file.getName()));
                    byte[] data = Util.concatenate(Util.readFile(file), getPartCharData(idParent, id).getData());
                    clothes.put(id, data);
                }
                IMAGES_CLOTH.put(idParent, clothes);
                Printer.printGreen(String.format("Finish Load Images Cloth Type %s [%s]", idParent, IMAGES_CLOTH.get(idParent).size()));
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Build Data Effect">
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
                dataOutputStream.writeByte(MOB_TEMPLATES.size());
                dataOutputStream.writeShort(0);
                @Cleanup("clear")
                List<EffectData> effDynamic = Manager.getEffectData(Const.DYNAMIC_EFFECT);
                dataOutputStream.writeByte(effDynamic.size());
                for (EffectData effect : effDynamic) {
                    dataOutputStream.writeShort(effect.getData().length);
                    dataOutputStream.write(effect.getData());
                }
                @Cleanup("clear")
                List<EffectData> effPet = Manager.getEffectData(Const.PET_EFFECT);
                dataOutputStream.writeByte(effPet.size());
                for (EffectData effect : effPet) {
                    dataOutputStream.writeShort(effect.getId());
                    dataOutputStream.writeShort(effect.getData().length);
                    dataOutputStream.write(effect.getData());
                }
                @Cleanup("clear")
                List<EffectData> effSkill = Manager.getEffectData(Const.SKILL_EFFECT);
                dataOutputStream.writeByte(effSkill.size());
                for (EffectData effect : effSkill) {
                    dataOutputStream.writeShort(effect.getId());
                    dataOutputStream.writeShort(effect.getData().length);
                    dataOutputStream.write(effect.getData());
                }

                dataOutputStream.writeByte(DX_HORSE.length);
                for (int i = 0; i < DX_HORSE.length; i++) {
                    for (int j = 0; j < 4; j++) {
                        dataOutputStream.writeByte(DX_HORSE[i][j]);
                    }
                    for (int k = 0; k < 4; k++) {
                        dataOutputStream.writeByte(DY_HORSE[i][k]);
                    }
                }
                for (int i = 0; i < 2; i++) {
                    dataOutputStream.writeByte(DX_DY_WP[i].length);
                    for (int j = 0; j < DX_DY_WP[i].length; j++) {
                        dataOutputStream.writeByte(DX_DY_WP[i][j]);
                    }
                }
                for (int i = 0; i < 2; i++) {
                    dataOutputStream.writeByte(DX_DY_PP[i].length);
                    for (int j = 0; j < DX_DY_PP[i].length; j++) {
                        dataOutputStream.writeByte(DX_DY_PP[i][j]);
                    }
                }
                for (int i = 0; i < 2; i++) {
                    dataOutputStream.writeByte(DX_DY_AVT[i].length);
                    for (int j = 0; j < DX_DY_AVT[i].length; j++) {
                        dataOutputStream.writeByte(DX_DY_AVT[i][j]);
                    }
                }
                DATA_EFFECT = byteArrayOutputStream.toByteArray();
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Build Data Map">
            for (byte l = 0; l < 2; l++) {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
                    dataOutputStream.writeByte(5);
                    ConcurrentHashMap<Short, IMap> maps = Manager.MAPS.get(l);
                    @Cleanup("clear")
                    List<IMap> list = maps.values().stream().sorted(Comparator.comparingInt(map -> map.getMapId())).collect(Collectors.toList());;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getMapId() == 70) {
                            IMap im = list.remove(i);
                            list.add(0, im);
                            break;
                        }
                    }
                    dataOutputStream.writeByte(list.size());
                    for (int i = 0; i < list.size(); i++) {
                        IMap map = list.get(i);
                        if (map.getMapData().getWayPoints().length > 0) {
                            WayPoint[][] wps = map.getMapData().getWayPoints()[l];
                            if (l == Const.THANH_LONG && map.getMapData().getWayPoints()[l].length <= 0) {
                                wps = map.getMapData().getWayPoints()[Const.HAC_HO];
                            }
                            dataOutputStream.writeByte(wps.length);
                            for (WayPoint[] wayPoint : wps) {
                                @Cleanup("clear")
                                List<Short> location = new ArrayList<>();
                                @Cleanup("clear")
                                List<String> name = new ArrayList<>();
                                for (WayPoint wp : wayPoint) {
                                    location.add(wp.getToMap());
                                    location.add(wp.getToX());
                                    location.add(wp.getToY());
                                    name.add(wp.getNameWayPoint());
                                }
                                for (int k = 0; k < location.size(); k++) {
                                    dataOutputStream.writeShort(location.get(k));
                                    if (k % 3 == 0) {
                                        dataOutputStream.writeUTF(name.get(k / 3));
                                    }
                                }
                            }
                        } else {
                            dataOutputStream.writeByte(0);
                        }
                    }
                    dataOutputStream.writeByte(list.size());
                    for (short i = 0; i < list.size(); i++) {
                        IMap map = list.get(i);
                        dataOutputStream.writeShort(map.getMapId());
                        for (int j = 0; j < 4; j++) {
                            if (j > map.getChildMaps().size() - 1) {
                                dataOutputStream.writeShort(0);
                            } else {
                                ChildMap childMap = map.getChildMaps().get(j);
                                dataOutputStream.writeShort(childMap.getId());
                            }
                        }
                        dataOutputStream.writeShort(i);
                    }
                    dataOutputStream.writeByte(XA_PHU.size());
                    for (XaPhu xaPhu : XA_PHU) {
                        @Cleanup("clear")
                        List<String> name = new ArrayList<>();
                        @Cleanup("clear")
                        List<Short> location = new ArrayList<>();
                        IMap map = Manager.getMap(Const.HAC_HO, (short) xaPhu.getIdMap());
                        name.add(map.getName());
                        location.add(map.getMapId());
                        location.add(xaPhu.getX());
                        location.add(xaPhu.getY());
                        for (IMap childMap : map.getChildMaps()) {
                            name.add(childMap.getName());
                            location.add(childMap.getMapId());
                            location.add(xaPhu.getX());
                            location.add(xaPhu.getY());
                        }
                        for (int k = 0; k < location.size(); k++) {
                            dataOutputStream.writeShort(location.get(k));
                            if (k % 3 == 0) {
                                dataOutputStream.writeUTF(name.get(k / 3));
                            }
                        }
                    }
                    if (l == Const.HAC_HO) {
                        DATA_LOCATION_HAC_HO = byteArrayOutputStream.toByteArray();
                    } else {
                        DATA_LOCATION_THANH_LONG = byteArrayOutputStream.toByteArray();
                    }
                }
            }

            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
                dataOutputStream.writeByte(XA_PHU_TEMPLATE.size());
                Enumeration<Byte> keysByte = Manager.XA_PHU_TEMPLATE.keys();
                while (keysByte.hasMoreElements()) {
                    byte key = keysByte.nextElement();
                    XaPhuTemplate template = XA_PHU_TEMPLATE.get(key);
                    int length = template.getMapID().length;
                    dataOutputStream.writeByte(length);
                    for (int i = 0; i < length; i++) {
                        dataOutputStream.writeByte(template.getMapID()[i]);
                    }
                    for (int i = 0; i < length; i++) {
                        dataOutputStream.writeShort(template.getX()[i]);
                    }
                    for (int i = 0; i < length; i++) {
                        dataOutputStream.writeShort(template.getY()[i]);
                    }
                    for (int i = 0; i < length; i++) {
                        dataOutputStream.writeShort(template.getPrice()[i]);
                    }
                }
                DATA_XA_PHU_TEMPLATE = byteArrayOutputStream.toByteArray();
            }
            // </editor-fold>

        } catch (Exception e) {
            Logger.logError("Lỗi Tải Dữ Liệu", e);
            System.exit(0);
        }
    }
}
