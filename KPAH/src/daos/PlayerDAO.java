package daos;

import clan.Clan;
import player.Player;
import database.HikariCP;
import database.ResultSetImpl;
import interfaces.ISession;
import item.Attribute;
import item.ItemEquip;
import item.ItemFriend;
import item.ItemGem;
import item.ItemPotion;
import item.ItemQuest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Cleanup;
import lombok.NonNull;
import manager.ClientManager;
import manager.Manager;
import manager.Settings;
import map.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import player.Friend;
import skill.SkillBuff;
import player.Info;
import player.Inventory;
import player.Location;
import player.Point;
import player.Skill;
import player.Sundry;
import services.ItemService;
import services.LoginService;
import services.MapService;
import services.Service;
import skill.BuffInfluencePlayer;
import template.ItemEquipTemplate;
import template.NpcTemplate;
import consts.Const;
import consts.HorseConst;
import item.ItemAnimal;
import player.Horse;
import utils.Printer;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class PlayerDAO {

    public static Player setupPlayer(int idSelect) throws SQLException, JSONException {
        ResultSetImpl rs = HikariCP.executeQuery("SELECT * FROM `players` WHERE `id` = '" + idSelect + "' LIMIT 1;");
        if (!rs.next()) {
            return null;
        }
        long lastTimeEndDelete = rs.getLong("lastTimeEndDelete");
        int day = -1;
        if (lastTimeEndDelete != 0) {
            day = Util.getDayDifference(lastTimeEndDelete, System.currentTimeMillis());
            if (day <= 0) {
                HikariCP.executeUpdate("DELETE FROM `players` WHERE `id` = '" + idSelect + "';");
                return null;
            }
        }
        Info info = buildInfo(idSelect, rs.getString("info"));
        Location location = buildLocation(rs.getString("location"), info.getIdNation());
        Point point = buildPoint(rs.getString("point"));
        Inventory inventory = buildInventory(rs.getString("inventory"), rs.getString("itemBody"), rs.getString("itemBag"), rs.getString("itemBox"), rs.getString("itemPotion"), rs.getString("itemQuest"), rs.getString("itemGem"), rs.getString("itemSpecial"), rs.getString("itemGemLock"), rs.getString("itemSold"), rs.getString("itemAnimal"), rs.getString("itemAnimalExpiry"));
        Skill skill = buildSkill(rs.getString("skills"));
        Horse horse = buildHorse(rs.getString("horse"));
        List<Friend> friends = buildListFriend(rs.getString("friends"));
        Player pl = Player.builder()
                .idDatabase(idSelect)
                .idPlayer(rs.getShort("idPlayer"))
                .name(rs.getString("name"))
                .info(info)
                .location(location)
                .point(point)
                .inventory(inventory)
                .skill(skill)
                .friends(friends)
                .sundry(Sundry.builder().build())
                .skillBuff(SkillBuff.builder().build())
                .buffInfluence(BuffInfluencePlayer.builder().build())
                .horse(horse)
                .build();
        pl.getSundry().setLastTimeLogout(rs.getLong("lastTimeLogout"));
        pl.getSundry().setLastTimeEndDelete(lastTimeEndDelete);
        pl.getSundry().setDayCanRestore((byte) day);
        rs.close();
        return pl;
    }

    public static void updatePlayer(@NonNull Player player) throws SQLException {
        String location = player.getLocation().toString();
        String info = player.getInfo().toString();
        String point = player.getPoint().toString();
        String itemBody = player.getInventory().getItemBody().toString();
        String itemPotion = player.getInventory().getItemPotion().toString();
        String itemBag = player.getInventory().getItemBag().toString();
        String inventory = player.getInventory().toString();
        String skill = player.getSkill().toString();
        String itemBox = player.getInventory().getItemBox().toString();
        String itemGem = player.getInventory().getItemGem().toString();
        String itemGemLock = player.getInventory().getItemGemLock().toString();
        String itemSold = player.getInventory().getItemSold().toString();
        String friends = player.getFriends().toString();
        String horse = player.getHorse().toString();
        String animal = player.getInventory().getItemAnimal().toString();
        String animalExpiry = player.getInventory().getItemAnimalExpiry().toString();
        if (Util.isNullOrEmpty(location) || Util.isNullOrEmpty(friends) || Util.isNullOrEmpty(animal) || Util.isNullOrEmpty(animalExpiry) || Util.isNullOrEmpty(horse) || Util.isNullOrEmpty(itemGemLock) || Util.isNullOrEmpty(itemSold) || Util.isNullOrEmpty(itemGem) || Util.isNullOrEmpty(itemBox) || Util.isNullOrEmpty(info) || Util.isNullOrEmpty(skill) || Util.isNullOrEmpty(itemBag) || Util.isNullOrEmpty(point) || Util.isNullOrEmpty(itemBody) || Util.isNullOrEmpty(itemPotion) || Util.isNullOrEmpty(inventory)) {
            Printer.printRed("Lỗi lưu dữ liệu: " + player.getName());
            return;
        }
        HikariCP.executeUpdate(String.format("UPDATE `players` SET `location` = '%s',`info` ='%s',`point` ='%s' ,`itemBody` ='%s',`itemPotion` ='%s',`inventory`='%s',`itemBag` = '%s',`skills` = '%s',`itemBox`='%s',`itemGem` = '%s',`itemSold`='%s',`itemGemLock`='%s',`friends`='%s',`horse`='%s',`itemAnimal`='%s',`itemAnimalExpiry`='%s',`lastTimeEndDelete` ='%s',`lastTimeLogout` ='%s' WHERE `id` = '%s'", location, info, point, itemBody, itemPotion, inventory, itemBag, skill, itemBox, itemGem, itemSold, itemGemLock, friends, horse, animal, animalExpiry, player.getSundry().getLastTimeEndDelete(), System.currentTimeMillis(), player.getIdDatabase()));
    }

    public static void createPlayer(@NonNull ISession session, String name, byte clazz, byte head, byte gender, byte idNation) throws SQLException, JSONException, IOException {
        ResultSetImpl rs = HikariCP.executeQuery("SELECT * FROM players WHERE name='" + name + "'");
        if (!rs.first()) {
            gender++;
            int idPlayer = HikariCP.executeExist("SELECT IFNULL(MAX(`idPlayer`), -31000) AS `maxId` FROM `players`;");
            int id = HikariCP.execute("INSERT INTO `players` (`idPlayer`,`name`, `info`,`location`,`point`,`itemBody`,`itemPotion`,`inventory`,`skills`) VALUES (?,?, ?,?,?,?,?,?,?)", 1, ++idPlayer, name, getDefaultInfo(clazz, head, gender, idNation), getDefaultLocation(idNation), getDefaultPoint(clazz), getDefaultItemWeapon(clazz, gender), getDefaultItemPotion(), getDefaultInventory(), getDefaultSkillLevels(clazz));
            Player pl = PlayerDAO.setupPlayer(id);
            if (pl != null) {
                session.addChar(pl);
                session.updateChar();
                LoginService.instance.sendListChar(session);
                return;
            }
            Service.instance.sendLogOut(session, "Có lỗi xảy ra");
        } else {
            Service.instance.sendLogOut(session, "Tên nhân vật đã tồn tại");
        }
        rs.close();
    }

    public static void deletePlayer(@NonNull ISession session, int playerId) throws IOException, SQLException {
        ResultSetImpl rs = HikariCP.executeQuery("SELECT * FROM users WHERE JSON_CONTAINS(chars, ?) AND username = ?", playerId, session.getUsername());
        if (!rs.next()) {
            Service.instance.sendLogOut(session, "Không tìm thấy nhân vật, vui lòng thử lại!");
            return;
        }
        rs.close();
        Player player = session.findPlayer(playerId);
        player.getSundry().setLastTimeEndDelete(Util.plusDayToTimeStamp(System.currentTimeMillis(), Settings.DAY_WAIT_FOR_DELETE));
        session.reloadChar(playerId);
        LoginService.instance.sendListChar(session);
    }

    public static void restorePlayer(@NonNull ISession session, int playerId) throws IOException, SQLException {
        ResultSetImpl rs = HikariCP.executeQuery("SELECT * FROM users WHERE JSON_CONTAINS(chars, ?) AND username = ?", playerId, session.getUsername());
        if (!rs.next()) {
            Service.instance.sendLogOut(session, "Không tìm thấy nhân vật, vui lòng thử lại!");
            return;
        }
        rs.close();
        Player player = session.findPlayer(playerId);
        player.getSundry().setLastTimeEndDelete(0);
        session.reloadChar(playerId);
        LoginService.instance.sendListChar(session);
    }

    // <editor-fold defaultstate="collapsed" desc="Builder">
    public static Player buildNpcActor(@NonNull NpcTemplate npcTemplate, short x, short y) {
        List<ItemEquip> itemsBody = new ArrayList<>();
        for (int i = 0; i < npcTemplate.getItemEquipment().length; i++) {
            ItemEquip item = ItemService.instance.createNewItemEquipment(npcTemplate.getItemEquipment()[i], (byte) 0);
            itemsBody.add(item);
        }
        Player player = Player.builder().horse(Horse.builder().build()).buffInfluence(BuffInfluencePlayer.builder().build()).skillBuff(SkillBuff.builder().build()).sundry(Sundry.builder().build()).skill(Skill.builder().build()).inventory(Inventory.builder().itemBody(itemsBody).build()).point(Point.builder().build()).location(Location.builder().x(x).y(y).build()).info(Info.builder().classPlayer(Const.KIEM_KHACH).head((byte) npcTemplate.getHead()).gender(Const.MALE).build()).idDatabase(npcTemplate.getId()).isNpc(true).name(npcTemplate.getName()).build();
        return player;
    }

    public static List<Friend> buildListFriend(String friendString) throws JSONException {
        JSONArray friendArray = new JSONArray(friendString);
        List<Friend> friends = new ArrayList<>();
        for (int i = 0; i < friendArray.length(); i++) {
            JSONArray friendData = friendArray.getJSONArray(i);
            short idFriend = (short) friendData.getInt(0);
            Friend friend;
            Player playerOnGame = ClientManager.getPlayer(idFriend);
            if (playerOnGame != null) {
                friend = Friend.builder().id(idFriend).name(friendData.getString(1)).head(playerOnGame.getInfo().getHead()).level(playerOnGame.getInfo().getLevel()).idClan((short) friendData.getInt(4)).isMaster((byte) friendData.getInt(5)).items(loadDataItemFriend(playerOnGame.getInventory().getItemBody())).build();
            } else {
                friend = Friend.builder().id(idFriend).name(friendData.getString(1)).head((byte) friendData.getInt(2)).level((byte) friendData.getInt(3)).idClan((short) friendData.getInt(4)).isMaster((byte) friendData.getInt(5)).items(loadDataItemFriend(friendData.getJSONArray(6))).build();
            }
            friends.add(friend);
        }
        return friends;
    }

    private static Info buildInfo(int id, String infoString) throws JSONException {
        JSONArray infoArray = new JSONArray(infoString);
        short idClan = (short) infoArray.getInt(6);
        Clan clan = null;
        if (idClan != -1) {
            clan = Manager.getClan(idClan);
        }
        if (clan != null && !clan.hasMember(id)) {
            clan = null;
        }
        return Info.builder()
                .classPlayer((byte) infoArray.getInt(0))
                .head((byte) infoArray.getInt(1))
                .gender((byte) infoArray.getInt(2))
                .idNation((byte) infoArray.getInt(3))
                .he((byte) infoArray.getInt(4))
                .level((byte) infoArray.getInt(5))
                .clan(clan)
                .killer((short) infoArray.getInt(7))
                .build();
    }

    private static Horse buildHorse(String horse) throws JSONException {
        JSONArray horseJSONArray = new JSONArray(horse);
        if (horseJSONArray.isNull(0)) {
            return Horse.builder().useHorse(HorseConst.NON_HORSE).build();
        }
        return Horse.builder()
                .useHorse((byte) horseJSONArray.getInt(0))
                .imageHorse((byte) horseJSONArray.getInt(1))
                .idItem((short) horseJSONArray.getInt(2))
                .isFly(horseJSONArray.getBoolean(3))
                .animalUse(loadAnimalItem(horseJSONArray.getJSONArray(4)))
                .build();
    }

    private static Location buildLocation(String locationString, byte idNation) throws JSONException {
        JSONArray locationArray = new JSONArray(locationString);
        byte inCountry = (byte) locationArray.getInt(3);
        Zone zone = MapService.instance.getValidZone(Manager.getMap(inCountry, (short) locationArray.getInt(0)), (byte) 0);
        Location location = Location.builder()
                .zone(zone)
                .x((short) locationArray.getInt(1))
                .y((short) locationArray.getInt(2))
                .inCountry(inCountry)
                .build();
        location.setMapVillage((byte) (idNation == Const.THANH_LONG ? 70 : 0));
        return location;
    }

    private static Point buildPoint(String pointString) throws JSONException {
        JSONArray pointArray = new JSONArray(pointString);
        return Point.builder()
                .hp(pointArray.getInt(0))
                .mp(pointArray.getInt(1))
                .strength((short) pointArray.getInt(2))
                .agility((short) pointArray.getInt(3))
                .spirit((short) pointArray.getInt(4))
                .health((short) pointArray.getInt(5))
                .luck((short) pointArray.getInt(6))
                .basePoint((short) pointArray.getInt(7))
                .skillPoint((short) pointArray.getInt(8))
                .dedicationPoint(pointArray.getInt(9))
                .baoKich((short) pointArray.getInt(10))
                .exp(pointArray.getLong(11))
                .build();
    }

    private static Inventory buildInventory(String inventoryString, String itemBodyString, String itemBagString, String itemBoxString, String itemPotionString, String itemQuestString, String itemGem, String itemSpecial, String itemGemLock, String itemSold, String animal, String animalExpiry) throws JSONException {
        JSONArray inventoryArray = new JSONArray(inventoryString);
        return Inventory.builder()
                .luong(inventoryArray.getInt(0))
                .luongKhoa(inventoryArray.getInt(1))
                .xu(inventoryArray.getLong(2))
                .limItemBag((byte) inventoryArray.getInt(3))
                .maxIdItem((short) inventoryArray.getInt(4))
                .itemBody(loadDataItemEquipment(itemBodyString))
                .itemBag(loadDataItemEquipment(itemBagString))
                .itemBox(loadDataItemEquipment(itemBoxString))
                .itemPotion(loadDataItemPotion(itemPotionString))
                .itemQuest(loadDataItemQuest(itemQuestString))
                .itemGem(loadDataItemGem(itemGem))
                .itemGemLock(loadDataItemGem(itemGemLock))
                .itemSold(loadDataItemEquipment(itemSold))
                .itemAnimal(loadDataItemAnimal(animal))
                .itemAnimalExpiry(loadDataItemAnimal(animalExpiry))
                .build();
    }

    private static Skill buildSkill(String skillString) throws JSONException {
        JSONArray skillData = new JSONArray(skillString);
        JSONArray levelSkill = skillData.getJSONArray(0);
        byte[] levelSkills = new byte[levelSkill.length()];
        long[] lastTimeUse = new long[levelSkills.length];

        for (int i = 0; i < levelSkills.length; i++) {
            levelSkills[i] = (byte) levelSkill.getInt(i);
        }

        return Skill.builder()
                .levelSkill(levelSkills)
                .timeLastUseSkills(lastTimeUse)
                .build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Load Item">
    private static List<ItemEquip> loadDataItemEquipment(String data) throws JSONException {
        List<ItemEquip> listItem = new ArrayList<>();
        JSONArray arrAll = new JSONArray(data);
        for (int i = 0; i < arrAll.length(); i++) {
            JSONArray dataItem = arrAll.getJSONArray(i);
            short idTemp = (short) dataItem.getInt(1);
            ItemEquipTemplate template = Manager.getItemEquipment(idTemp);
            ItemEquip item = ItemEquip.builder().template(template).itemAttributes(new ArrayList<>()).build();
            item.setIdItem((short) dataItem.getInt(0));
            item.setClassChar((byte) dataItem.getInt(2));
            item.setLevel((byte) dataItem.getInt(3));
            item.setPlusTemplate((byte) dataItem.getInt(4));
            item.setColorName((byte) dataItem.getInt(5));
            item.setLock(dataItem.getBoolean(6));
            item.setMDurable((short) dataItem.getInt(7));
            item.setDurable((short) dataItem.getInt(8));
            item.setViTriVe((byte) dataItem.getInt(9));
            item.setRank((byte) dataItem.getInt(10));
            item.setDamageType((byte) dataItem.getInt(11));
            item.setNameCharSeal(dataItem.getString(12));
            item.setDayUse(dataItem.getInt(13));
            item.setTimeCreateItem(dataItem.getLong(14));
            item.setHe((byte) dataItem.getInt(15));
            JSONArray attributes = dataItem.getJSONArray(16);
            for (int j = 0; j < attributes.length(); j++) {
                JSONArray dataAttribute = attributes.getJSONArray(j);
                Attribute att = Attribute.builder().template(Manager.getAttributeTemplate((short) dataAttribute.getInt(0))).value((short) dataAttribute.getInt(1)).build();
                item.getItemAttributes().add(att);
            }
            listItem.add(item);
        }
        return listItem;
    }

    private static ItemAnimal loadAnimalItem(JSONArray arr) throws JSONException {
        if (arr.isNull(0)) {
            return null;
        }
        ItemAnimal animal = ItemService.instance.createNewItemAnimal((short) arr.getInt(0));
        animal.setLevel((byte) arr.getInt(1));
        JSONArray attributes = arr.getJSONArray(2);
        for (int j = 0; j < attributes.length(); j++) {
            JSONArray dataAttribute = attributes.getJSONArray(j);
            Attribute att = Attribute.builder().template(Manager.getAttributeTemplate((short) dataAttribute.getInt(0))).value((short) dataAttribute.getInt(1)).build();
            animal.getAttributes().add(att);
        }
        animal.setItemBody(loadDataItemEquipment(arr.getJSONArray(3).toString()));
        animal.setMinutes(arr.getInt(4));
        animal.setTimeStart(arr.getLong(5));
        return animal;
    }

    private static List<ItemAnimal> loadDataItemAnimal(String data) throws JSONException {
        List<ItemAnimal> listItem = new ArrayList<>();
        JSONArray arrAll = new JSONArray(data);
        for (byte i = 0; i < arrAll.length(); i++) {
            JSONArray dataItem = arrAll.getJSONArray(i);
            ItemAnimal animal = ItemService.instance.createNewItemAnimal((short) dataItem.getInt(0));
            animal.setLevel((byte) dataItem.getInt(1));
            JSONArray attributes = dataItem.getJSONArray(2);
            for (int j = 0; j < attributes.length(); j++) {
                JSONArray dataAttribute = attributes.getJSONArray(j);
                Attribute att = Attribute.builder().template(Manager.getAttributeTemplate((short) dataAttribute.getInt(0))).value((short) dataAttribute.getInt(1)).build();
                animal.getAttributes().add(att);
            }
            animal.setItemBody(loadDataItemEquipment(dataItem.getJSONArray(3).toString()));
            animal.setMinutes(dataItem.getInt(4));
            animal.setTimeStart(dataItem.getLong(5));
            listItem.add(animal);
        }
        return listItem;
    }

    private static List<ItemFriend> loadDataItemFriend(List<ItemEquip> items) throws JSONException {
        List<ItemFriend> listItem = new ArrayList<>();
        for (ItemEquip item : items) {
            listItem.add(ItemService.instance.createNewItemFriend(item));
        }
        return listItem;
    }

    public static List<ItemFriend> loadDataItemFriend(JSONArray arrAll) throws JSONException {
        List<ItemFriend> listItem = new ArrayList<>();
        for (int i = 0; i < arrAll.length(); i++) {
            JSONArray dataItem = arrAll.getJSONArray(i);
            ItemFriend item = ItemFriend.builder().idTemplate((short) dataItem.getInt(0)).classChar((byte) dataItem.getInt(1)).level((byte) dataItem.getInt(2)).plusTemplate((byte) dataItem.getInt(3)).build();
            listItem.add(item);
        }
        return listItem;
    }

    private static List<ItemPotion> loadDataItemPotion(String data) throws JSONException {
        List<ItemPotion> listItem = new ArrayList<>();
        JSONArray arrAll = new JSONArray(data);
        for (byte i = 0; i < arrAll.length(); i++) {
            JSONArray dataItem = arrAll.getJSONArray(i);
            ItemPotion item = ItemService.instance.createNewItemPotion((short) dataItem.getInt(0), dataItem.getInt(1));
            listItem.add(item);
        }
        return listItem;
    }

    private static List<ItemGem> loadDataItemGem(String data) throws JSONException {
        List<ItemGem> listItem = new ArrayList<>();
        JSONArray arrAll = new JSONArray(data);
        for (int i = 0; i < arrAll.length(); i++) {
            JSONArray dataItem = arrAll.getJSONArray(i);
            ItemGem item = ItemService.instance.createNewItemGem((short) dataItem.getInt(1), (short) dataItem.getInt(2));
            item.setIdItem((short) dataItem.getInt(0));
            listItem.add(item);
        }
        return listItem;
    }

    private static List<ItemQuest> loadDataItemQuest(String data) throws JSONException {
        List<ItemQuest> listItem = new ArrayList<>();
        JSONArray arrAll = new JSONArray(data);
        for (int i = 0; i < arrAll.length(); i++) {
            JSONArray dataItem = arrAll.getJSONArray(i);
            ItemQuest item = ItemService.instance.createNewItemQuest((byte) dataItem.getInt(0), (short) dataItem.getInt(1));
            listItem.add(item);
        }
        return listItem;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Default Data Player">
    private static String getDefaultInfo(byte clazz, byte head, byte gender, byte idNation) {
        return String.format("[%s,%s,%s,%s,%s,%s,-1,0]", clazz, head, gender, idNation, Util.getHe(clazz), 1);
    }

    private static String getDefaultSkillLevels(byte clazz) {
        if (clazz == Const.PHAP_SU) {
            return "[[1,0,0,0,0,0,0,0,-1,-1,-1,-1,-1,-1,-1]]";
        }
        return "[[1,0,0,0,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1]]";
    }

    private static String getDefaultLocation(byte idNation) {
        return String.format("[105,184,280,%s]", idNation);
    }

    private static String getDefaultInventory() {
        return String.format("[%s,%s,%s,%s,%s]", Const.LUONG_START, Const.LUONG_KHOA_START, Const.XU_START, 5, Short.MIN_VALUE + 2);
    }

    private static String getDefaultPoint(byte clazz) {
        switch (clazz) {
            case Const.KIEM_KHACH -> {
                return "[-1,-1,25,20,10,25,10,0,0,0,0,0]";
            }
            case Const.CHIEN_BINH -> {
                return "[-1,-1,30,20,10,20,10,0,0,0,0,0]";
            }
            case Const.PHAP_SU -> {
                return "[-1,-1,10,25,35,10,10,0,0,0,0,0]";
            }
            case Const.DAU_SI -> {
                return "[-1,-1,20,30,10,20,10,0,0,0,0,0]";
            }
            case Const.CUNG_THU -> {
                return "[-1,-1,20,30,15,15,10,0,0,0,0,0]";
            }
        }
        return "[]";
    }

    private static String getDefaultItemWeapon(byte clazz, byte gender) {
        @Cleanup("dispose")
        ItemEquip ao = null;
        @Cleanup("dispose")
        ItemEquip quan = null;
        switch (gender) {
            case Const.MALE -> {
                ao = ItemService.instance.createNewItemEquipment((short) 2, clazz);
                quan = ItemService.instance.createNewItemEquipment((short) 28, clazz);
            }
            case Const.FEMALE -> {
                ao = ItemService.instance.createNewItemEquipment((short) 1, clazz);
                quan = ItemService.instance.createNewItemEquipment((short) 27, clazz);
            }
        }
        @Cleanup("dispose")
        ItemEquip vuKhi = null;
        switch (clazz) {
            case Const.CHIEN_BINH ->
                vuKhi = ItemService.instance.createNewItemEquipment((short) 86, clazz);
            case Const.CUNG_THU ->
                vuKhi = ItemService.instance.createNewItemEquipment((short) 107, clazz);
            case Const.KIEM_KHACH ->
                vuKhi = ItemService.instance.createNewItemEquipment((short) 79, clazz);
            case Const.PHAP_SU ->
                vuKhi = ItemService.instance.createNewItemEquipment((short) 93, clazz);
            case Const.DAU_SI ->
                vuKhi = ItemService.instance.createNewItemEquipment((short) 100, clazz);
        }
        if (ao != null && quan != null && vuKhi != null) {
            vuKhi.setIdItem((short) -32768);
            ao.setIdItem((short) -32767);
            quan.setIdItem((short) -32766);
            return "[" + ao.toString() + ", " + quan.toString() + ", " + vuKhi.toString() + "]";
        }
        return "[]";
    }

    private static String getDefaultItemPotion() {
        @Cleanup("dispose")
        ItemPotion hp = ItemService.instance.createNewItemPotion((byte) 1, 10);
        @Cleanup("dispose")
        ItemPotion mp = ItemService.instance.createNewItemPotion((byte) 4, 10);
        if (hp != null && mp != null) {
            return "[" + hp.toString() + ", " + mp.toString() + "]";
        }
        return "[]";
    }
    // </editor-fold>
}
