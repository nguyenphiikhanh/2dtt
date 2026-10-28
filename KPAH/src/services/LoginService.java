package services;

import daos.PlayerDAO;
import effects.EffectData;
import interfaces.ISession;
import item.ItemEquip;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.Synchronized;
import manager.ClientManager;
import manager.ExecutorVirtualThread;
import manager.Manager;
import manager.Settings;
import player.Player;
import network.Message;
import template.ItemQuestTemplate;
import template.PotionTemplate;
import template.SkillNewTemplate;
import utils.CommandMessage;
import consts.Const;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class LoginService {

    public static final LoginService instance = new LoginService();

    @Synchronized
    public void selectChar(@NonNull ISession session, byte type, int selectId) throws IOException, SQLException {
        switch (type) {
            case Const.SELECT_CHAR -> {
                if (session.getPlayer() == null) {
                    Player pl = session.findPlayer(selectId);
                    if (pl == null) {
                        session.disconnect();
                        return;
                    }
                    if (ClientManager.containsPlayers(pl)) {
                        session.disconnect();
                        pl.getSession().disconnect();
                        return;
                    }
                    if (pl.getSundry().getDayCanRestore() == -1) {
                        if (!Util.canDoWithTime(pl.getSundry().getLastTimeLogout(), Settings.MILISECOND_WAIT_LOGIN)) {
                            Service.instance.sendLogOut(session, "Bạn chỉ có thể vào lại game sau " + (Settings.SECOND_WAIT_LOGIN - Util.getSecondDifference(System.currentTimeMillis(), pl.getSundry().getLastTimeLogout())) + " giây nữa");
                            return;
                        }
                        session.emptyListChar(pl.getIdDatabase());
                        pl.setSession(session);
                        session.setPlayer(pl);
                        pl.setUp();
                        sendDataWhenLogin(pl);
                        ExecutorVirtualThread.submitThreadPlayer(pl.updatePlayer());
                        ChangeMapService.instance.changeMap(pl, pl.getLocation().getZone(), pl.getLocation().getX(), pl.getLocation().getY());
                    }
                }
            }
            case Const.DELETE_CHAR -> {
                PlayerDAO.deletePlayer(session, selectId);
            }
            case Const.RESTORE_CHAR -> {
                PlayerDAO.restorePlayer(session, selectId);
            }
        }
    }

    public void sendDataWhenLogin(@NonNull Player pl) throws IOException {
        Service.instance.sendEffectData(pl);
        sendDataLogin(pl);
        MapService.instance.sendLocationServer(pl);
        Service.instance.sendConfigGame(pl, Const.SHOW_TREE_AND_PLAYER);
        ChatService.instance.sendChatDelay(pl, String.format("Chào mừng đến với %s. Chúc bạn chơi game vui vẻ", Settings.NAME_SERVER));
        MapService.instance.sendXaPhuTemplate(pl);
        SkillService.instance.sendSkillInfo(pl);
        Service.instance.sendMainCharInfo(pl);
        Service.instance.sendImage(pl, (byte) 1);
        ItemService.instance.sendItemTemplate(pl);
        InventoryService.instance.sendItemBody(pl);
        InventoryService.instance.sendItemBodyAnimal(pl);
        InventoryService.instance.sendItemPotion(pl);
        InventoryService.instance.sendItemBag(pl);
        InventoryService.instance.sendItemAnimal(pl);
        InventoryService.instance.sendItemQuest(pl);
        InventoryService.instance.sendItemGem(pl);
        InventoryService.instance.sendItemSpecial(pl);
        InventoryService.instance.sendItemGemLock(pl);
        FriendService.instance.sendListFriend(pl);
    }

    @Synchronized
    public void sendDataLogin(@NonNull Player pl) throws IOException {
        Message m = new Message(CommandMessage.LOGIN);
        m.writer().writeByte(pl.getPoint().getSpeed());
        m.writer().writeShort(Manager.getPotionTemplate((byte) 20).getPrice());
        m.writer().writeByte(Manager.POTION_TEMPLATES.size());
        Enumeration<Short> keys = Manager.POTION_TEMPLATES.keys();
        while (keys.hasMoreElements()) {
            short key = keys.nextElement();
            PotionTemplate itemPotion = Manager.getPotionTemplate(key);
            m.writer().writeByte(itemPotion.getIdImage());
            m.writer().writeUTF(itemPotion.getName());
            m.writer().writeUTF(itemPotion.getName2());
            m.writer().writeShort(itemPotion.getDelay());
            m.writer().writeBoolean(itemPotion.isTrade());
        }
        for (int i = 0; i < Manager.NUM_SKILLS.length; i++) {
            m.writer().writeByte(Manager.NUM_SKILLS[i]);
        }
        for (byte i = 0; i < 5; i++) {
            @Cleanup("clear")
            List<SkillNewTemplate> skillNewTemplates = Manager.getListSkillNew(i);
            m.writer().writeByte(skillNewTemplates.size());
            for (int j = 0; j < skillNewTemplates.size(); j++) {
                SkillNewTemplate skillNewTemplate = skillNewTemplates.get(j);
                m.writer().writeByte(skillNewTemplate.getIdSkill());
                m.writer().writeUTF(skillNewTemplate.getName());
                m.writer().writeUTF(skillNewTemplate.getDecript());
                m.writer().writeInt(skillNewTemplate.getPrice());
            }
        }
        m.writer().writeShort(Manager.ID_MAP_PK);
        m.writer().writeByte(Manager.POTION_SHOP.length);
        for (int i = 0; i < Manager.POTION_SHOP.length; i++) {
            m.writer().writeByte(Manager.POTION_SHOP[i]);
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                m.writer().writeByte(Manager.MAIN_DEFEND[i][j]);
                m.writer().writeByte(Manager.MAIN_ATTACK[i][j]);
                m.writer().writeByte(Manager.PERCENT_ATTACK[i][j]);
                m.writer().writeByte(Manager.PERCENT_DEFEND[i][j]);
            }
        }
        m.writer().writeByte(Manager.ARENA[0]);
        m.writer().writeByte(Manager.ARENA[1]);
        m.writer().writeByte(Manager.ARENA[2]);
        m.writer().writeByte(Manager.ARENA[3]);
        m.writer().writeByte(Manager.KHAM_PROPERTY.length);
        for (int i = 0; i < Manager.KHAM_PROPERTY.length; i++) {
            m.writer().writeUTF(Manager.KHAM_PROPERTY[i]);
            m.writer().writeByte(Manager.ID_NGOC_KHAM[i]);
        }
        m.writer().writeUTF(Settings.NUMBER_SUPPORT);
        m.writer().writeByte(Manager.TYPE_MP_HP[0].length);
        for (int i = 0; i < Manager.TYPE_MP_HP[0].length; i++) {
            m.writer().writeByte(Manager.TYPE_MP_HP[0][i]);
            m.writer().writeInt(Manager.VALUE_MP_HP[0][i]);
            m.writer().writeByte(Manager.TYPE_MP_HP[1][i]);
            m.writer().writeInt(Manager.VALUE_MP_HP[1][i]);
        }
        m.writer().writeByte(Manager.ITEM_QUEST_TEMPLATES.size());
        Enumeration<Byte> keyBytes = Manager.ITEM_QUEST_TEMPLATES.keys();
        while (keyBytes.hasMoreElements()) {
            byte key = keyBytes.nextElement();
            ItemQuestTemplate itemQuestTemplate = Manager.getItemQuestTemplate(key);
            m.writer().writeByte(itemQuestTemplate.getIdIcon());
            m.writer().writeUTF(itemQuestTemplate.getName());
        }
        m.writer().writeUTF(Settings.URL);
        m.writer().writeByte(0);
        @Cleanup("clear")
        List<EffectData> effThanThu = Manager.getEffectData(Const.THAN_THU_EFFECT);
        m.writer().writeByte(effThanThu.size());
        for (EffectData effect : effThanThu) {
            m.writer().writeShort(effect.getData().length);
            for (int j = 0; j < effect.getData().length; j++) {
                m.writer().writeByte(effect.getData()[j]);
            }
        }
        pl.getSession().sendMessage(m);
    }

    public void sendListChar(@NonNull ISession session) throws IOException {
        Message m = new Message(CommandMessage.CHARLIST);
        m.writer().writeByte(session.getListChar().size());
        for (int i = 0; i < session.getListChar().size(); i++) {
            Player pl = session.getListChar().get(i);
            m.writer().writeInt(pl.getIdDatabase());
            m.writer().writeUTF(pl.getName());
            m.writer().writeByte(pl.getHead());
            m.writer().writeByte(pl.getInventory().getItemBody().size());
            for (int j = 0; j < pl.getInventory().getItemBody().size(); j++) {
                ItemEquip item = pl.getInventory().getItemBody().get(j);
                m.writer().writeByte(item.getTemplate().getType());
                m.writer().writeByte(item.getTemplate().getStyle());
            }
            m.writer().writeShort(pl.getInfo().getLevel());
            m.writer().writeByte(pl.getSundry().getDayCanRestore() == -1 ? 1 : 0);
            m.writer().writeByte(pl.getInfo().getIdNation());
            m.writer().writeShort(pl.getSundry().getDayCanRestore());
            ItemEquip weapon = InventoryService.instance.findItemBodyByType(pl, (byte) (3 + pl.getInfo().getClassPlayer()));
            if (weapon == null) {
                m.writer().writeByte(-1);
            } else {
                m.writer().writeByte(0);
                byte[][] img = Util.readFileAndSplit("data/image/weapon/" + weapon.getTemplate().getId() + ".png");
                if (img != null) {
                    m.writer().writeShort(img[0].length);
                    for (int k = 0; k < img[0].length; k++) {
                        m.writer().writeByte(img[0][k]);
                    }
                    m.writer().writeShort(img[1].length);
                    for (int k = 0; k < img[1].length; k++) {
                        m.writer().writeByte(img[1][k]);
                    }
                    m.writer().writeByte(weapon.getTemplate().getDxWear());
                    m.writer().writeByte(weapon.getTemplate().getDyWear());
                }
            }
        }
        session.sendMessage(m);
    }
}
