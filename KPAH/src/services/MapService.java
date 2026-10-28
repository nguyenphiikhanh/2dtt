package services;

import clan.Clan;
import interfaces.IMap;
import item.Attribute;
import item.ItemEquip;
import item.ItemMap;
import item.ItemPotion;
import player.Player;
import java.io.IOException;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.Synchronized;
import manager.Manager;
import map.Actor;
import map.LoctionWayPoint;
import map.Monster;
import map.Zone;
import network.Message;
import template.NpcTemplate;
import utils.CommandMessage;
import consts.Const;
import consts.HorseConst;
import item.ItemAnimal;
import map.NpcServer;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class MapService {

    public static final MapService instance = new MapService();

    public void revivePlayer(@NonNull Player player, byte percentRecover) throws IOException {
        player.getPoint().plusHp(player.getPoint().getHpMax() * percentRecover / 100);
        player.getPoint().plusMp(player.getPoint().getMpMax() * percentRecover / 100);
        MapService.instance.onNewHpMp(player);
        player.getSundry().setNewlyRevived(true);
    }

    public void comeHome(@NonNull Player player) throws IOException {
        if (player.getSundry().isComeHome() && Util.canDoWithTime(player.getSundry().getLastTimeComeHome(), 9000)) {
            if (player.getLocation().getZone().getMap().isMapVillage()) {
                return;
            }
            ItemPotion thoDiaPhu = InventoryService.instance.findItemPotion(player, (byte) 19);
            InventoryService.instance.minusQuantityItemPotion(player, thoDiaPhu, (byte) 1);
            InventoryService.instance.sendItemPotion(player);
            if (player.getLocation().getInCountry() != player.getInfo().getIdNation()) {
                player.getLocation().setInCountry(player.getInfo().getIdNation());
                MapService.instance.sendLocationServer(player);
                Service.instance.sendMainCharInfo(player);
            }
            ChangeMapService.instance.changeMap(player, player.getLocation().getMapVillage(), (short) 384, (short) 672);
            player.getSundry().setComeHome(false);
        } else if (player.isDie()) {
            player.getPoint().plusHp(player.getPoint().getHpMax());
            player.getPoint().plusMp(player.getPoint().getMpMax());
            if (player.getLocation().getInCountry() != player.getInfo().getIdNation()) {
                player.getLocation().setInCountry(player.getInfo().getIdNation());
                MapService.instance.sendLocationServer(player);
                Service.instance.sendMainCharInfo(player);
            }
            ChangeMapService.instance.changeMap(player, player.getLocation().getMapVillage(), (short) 384, (short) 672);
            MapService.instance.onNewHpMp(player);
        }
    }

    public void getItemEquipmentFromGround(@NonNull Player player, short id) throws IOException {
        if (player.getSundry().getTrader() != null) {
            return;
        }
        ItemMap itemMap = player.getLocation().getZone().findItemMap(id);
        if (itemMap == null || itemMap.getItemCatagory() != Const.CATEGORY_ITEM) {
            return;
        }
        if (Util.getDistance(player.getLocation().getX(), player.getLocation().getY(), itemMap.getX(), itemMap.getY()) > 35) {
            return;
        }
        if (itemMap.getIdPlayerDrop() != -1 && itemMap.getIdPlayerDrop() != player.getIdPlayer()) {
            ChatService.instance.sendChatOnlyMe(player, "Không thể nhặt vật phẩm của người khác");
            return;
        }
        if (player.getInventory().isFullInventory()) {
            ChatService.instance.sendChatOnlyMe(player, "Hành trang đã đầy");
            return;
        }
        ItemEquip equipment = ItemService.instance.createNewItemEquipment(itemMap);
        byte levelPlus = 0;
        for (Attribute att : equipment.getItemAttributes()) {
            if (att != null && att.getValue() > 3 && Util.isTrue(87.2, 100.0)) {
                short value = att.getValue();
                if (att.getTemplate().getIsPercent() == 2) {
                    byte percent = (byte) Util.nextInt(-2, 6);
                    value += percent;
                    levelPlus += percent;
                } else if (att.getTemplate().getIsPercent() == 0) {
                    byte percent = (byte) Util.nextInt(-40, 60);
                    value += value * percent / 100;
                    levelPlus += (byte) (percent / 30);
                }
                att.setValue(value);
            }
        }
        short durable = (short) (equipment.getDurable() + equipment.getDurable() * Util.nextDouble(-20, 20) / 100);
        equipment.setDurable(durable);
        equipment.setMDurable(durable);
        equipment.setLevel((byte) (equipment.getLevel() + levelPlus));
        InventoryService.instance.addItemBagEquipment(player, equipment);
        ItemService.instance.removeItemEquipmentFromGround(player, itemMap, equipment);
        ChatService.instance.sendChatOnlyMe(player, "Bạn nhặt được " + equipment.getTemplate().getName());
    }

    public void getGemFromGround(@NonNull Player player, short id) throws IOException {
        if (player.getSundry().getTrader() != null) {
            return;
        }
        ItemMap itemMap = player.getLocation().getZone().findItemMap(id);
        if (itemMap == null || itemMap.getItemCatagory() != Const.CATEGORY_GEM_ITEM) {
            return;
        }
        if (Util.getDistance(player.getLocation().getX(), player.getLocation().getY(), itemMap.getX(), itemMap.getY()) > 35) {
            return;
        }
        if (itemMap.getIdPlayerDrop() != -1 && itemMap.getIdPlayerDrop() != player.getIdPlayer()) {
            ChatService.instance.sendChatOnlyMe(player, "Không thể nhặt vật phẩm của người khác");
            return;
        }
        if (player.getInventory().isFullInventory()) {
            ChatService.instance.sendChatOnlyMe(player, "Hành trang đã đầy");
            return;
        }
        ItemService.instance.removeItemGemFromGround(player, itemMap);
        InventoryService.instance.addItemGem(player, ItemService.instance.createNewItemGem(itemMap.getItemTemplateID(), itemMap.getQuantity()));
        InventoryService.instance.sendItemGem(player);
    }

    public void getPotionFromGround(@NonNull Player player, short id) throws IOException {
        if (player.getSundry().getTrader() != null) {
            return;
        }
        ItemMap itemMap = player.getLocation().getZone().findItemMap(id);
        if (itemMap == null || itemMap.getItemCatagory() != Const.CATEGORY_POTION) {
            return;
        }
        if (Util.getDistance(player.getLocation().getX(), player.getLocation().getY(), itemMap.getX(), itemMap.getY()) > 35) {
            return;
        }
        if (itemMap.getIdPlayerDrop() != -1 && itemMap.getIdPlayerDrop() != player.getIdPlayer()) {
            ChatService.instance.sendChatOnlyMe(player, "Không thể nhặt vật phẩm của người khác");
            return;
        }
        if (player.getInventory().isFullInventory()) {
            ChatService.instance.sendChatOnlyMe(player, "Hành trang đã đầy");
            return;
        }
        ItemService.instance.removeItemPotionFromGround(player, itemMap);
        InventoryService.instance.addItemPotion(player, ItemService.instance.createNewItemPotion(itemMap));
    }

    public void onNewHpMp(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.NEW_HP_MP);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeInt(pl.getPoint().getHpMax());
        msg.writer().writeInt(pl.getPoint().getMpMax());
        msg.writer().writeShort(pl.getPoint().getDefend());
        msg.writer().writeInt(pl.getPoint().getHp());
        sendAllPlayerInMap(pl, msg);
    }

    public void onPlayerDie(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.ACTOR_DIE);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        sendAllPlayerInMap(pl, msg);
    }

    public void checkMove(@NonNull Player pl, short x, short y) throws IOException {
        if (pl.isDie()) {
            return;
        }
        if (pl.getLocation().getZone().getMap().isOfflineMap()) {
            return;
        }
        pl.getLocation().setLastX(pl.getLocation().getX());
        pl.getLocation().setLastY(pl.getLocation().getY());
        if (pl.getSundry().isComeHome()) {
            pl.getSundry().setComeHome(false);
        }
        if (Math.abs(pl.getLocation().getX() - pl.getLocation().getLastX()) > pl.getPoint().getSpeed() || Math.abs(pl.getLocation().getY() - pl.getLocation().getLastY()) > pl.getPoint().getSpeed()) {
            return;
        }
        if (pl.getLocation().isStopCollectMessageMove() && (!Util.checkSuperiorOrInferior(x, pl.getLocation().getLastX(), pl.getPoint().getSpeed()) || !Util.checkSuperiorOrInferior(y, pl.getLocation().getLastY(), pl.getPoint().getSpeed()))) {
            sendPosPlayer(pl, pl.getLocation().getLastX(), pl.getLocation().getLastY());
            return;
        }
        IMap map = pl.getLocation().getZone().getMap();
        if (tileTypeAtPixel(pl, x, y, 2) || y / 16 * map.getMapData().getW() + x / 16 >= map.getMapData().getType().length) {
            pl.getLocation().setStopCollectMessageMove(true);
            sendPosPlayer(pl, pl.getLocation().getLastX(), pl.getLocation().getLastY());
            return;
        }
        pl.getLocation().setStopCollectMessageMove(false);
        if (!pl.getLocation().isStopCollectMessageMove()) {
            pl.getLocation().setX(x);
            pl.getLocation().setY(y);
            sendMove(pl);
            updateMobInside(pl);
            updateItemMapInside(pl);
        }
    }

    public void onDownHorse(@NonNull Player pl) throws IOException {
        if (pl.getHorse().getUseHorse() == HorseConst.NON_HORSE) {
            return;
        }
        if (!pl.getInventory().isFullInventory()) {
            if (pl.getHorse().getAnimalUse() == null) {
                InventoryService.instance.addItemPotion(pl, ItemService.instance.createNewItemPotion(pl.getHorse().getIdItem(), 1));
                InventoryService.instance.sendItemPotion(pl);
            } else {
                InventoryService.instance.addItemAnimal(pl, pl.getHorse().getAnimalUse());
                InventoryService.instance.sendItemBody(pl);
                InventoryService.instance.sendItemAnimal(pl);
                pl.getHorse().setAnimalUse(null);
            }
        }
        pl.getHorse().setUseHorse(HorseConst.NON_HORSE);
        pl.getPoint().initPoint();
        Service.instance.sendMainCharInfo(pl);
        sendInfoMe(pl);
    }

    public void onChangeKiller(@NonNull Player pl) throws IOException {
        if (pl.getSundry().getPk() != 0) {
            Service.instance.sendLogOut(pl.getSession(), "Không khả dụng khi đang đeo khăn");
            return;
        }
        if (pl.getSundry().isKiller() && pl.getInfo().getKiller() > 0) {
            return;
        }
        pl.getSundry().setKiller(!pl.getSundry().isKiller());
        Message msg = new Message(CommandMessage.KILLER);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeByte(pl.getSundry().isKiller() ? 1 : 0);
        msg.writer().writeShort(pl.getInfo().getKiller());
        sendAllPlayerInMap(pl, msg);
    }

    public void sendKiller(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.KILLER);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeByte(pl.getSundry().isKiller() ? 1 : 0);
        msg.writer().writeShort(pl.getInfo().getKiller());
        sendAllPlayerInMap(pl, msg);
    }

    public void removePlayerInMap(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_OUT);
        msg.writer().writeShort(pl.getIdPlayer());
        sendAnotherNotMeInMap(pl, msg);
    }

    public void sendPosPlayer(@NonNull Player pl, short x, short y) throws IOException {
        Message msg = new Message(CommandMessage.INFO_ACTOR_POS);
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        msg.writer().writeByte(0);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeShort(x);
        msg.writer().writeShort(y);
        msg.writer().writeByte(pl.getSundry().getPk());
        msg.writer().writeInt(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeBoolean(true);
        pl.getSession().sendMessage(msg);
    }

    public void sendMove(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.MOVE_CHAR);
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        msg.writer().writeByte(0);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeShort(pl.getLocation().getX());
        msg.writer().writeShort(pl.getLocation().getY());
        msg.writer().writeByte(pl.getSundry().getPk());
        msg.writer().writeInt(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeBoolean(true);
        updatePlayerInside(pl, msg);
    }

    public void sendMove(@NonNull Player pl, @NonNull Player plSend) throws IOException {
        Message msg = new Message(CommandMessage.MOVE_CHAR);
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        msg.writer().writeByte(0);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeShort(pl.getLocation().getX());
        msg.writer().writeShort(pl.getLocation().getY());
        msg.writer().writeByte(pl.getSundry().getPk());
        msg.writer().writeInt(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeBoolean(true);
        plSend.getSession().sendMessage(msg);
    }

    public void removePlayerInMap(@NonNull Player pl, @NonNull Player plSend) throws IOException {
        Message msg = new Message(CommandMessage.REMOVE_ACTOR);
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        msg.writer().writeShort(pl.getIdPlayer());
        plSend.getSession().sendMessage(msg);
    }

    public void onSetXP(@NonNull Player player, int xp) throws IOException {
        Message msg = new Message(CommandMessage.SET_XP);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeShort(Util.getPercentExp(player.getInfo().getLevel(), player.getPoint().getExp()));
        msg.writer().writeInt(xp);
        player.getSession().sendMessage(msg);
    }

    public void onLevelUp(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.LEVEL_UP);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(player.getInfo().getLevel());
        msg.writer().writeInt(player.getPoint().getHpMax());
        msg.writer().writeInt(player.getPoint().getMpMax());
        sendAllPlayerInMap(player, msg);
    }

    public void sendViewInfo(@NonNull Player player, byte type, short idView) throws IOException {
        Player playerView = player.getLocation().getZone().findPlayer(idView);
        if (playerView == null) {
            return;
        }
        Message msg = new Message(CommandMessage.VIEW_INFO);
        msg.writer().writeByte(type);
        if (type == 0) {
            msg.writer().writeUTF(playerView.getName());
            msg.writer().writeByte(playerView.getInfo().getHead());
            msg.writer().writeByte(playerView.getInfo().getLevel());
            msg.writer().writeByte(playerView.getInventory().getItemBody().size());
            for (short i = 0; i < playerView.getInventory().getItemBody().size(); i++) {
                ItemEquip itemEquipment = playerView.getInventory().getItemBody().get(i);
                msg.writer().writeByte(itemEquipment.getClassChar());
                msg.writer().writeShort(itemEquipment.getTemplate().getId());
                msg.writer().writeByte(itemEquipment.getLevel());
                msg.writer().writeByte(itemEquipment.getPlusTemplate());
                msg.writer().writeShort(itemEquipment.getIdItem());
                msg.writer().writeByte(itemEquipment.getColorName());
                for (int k = 0; k < 5; k++) {
                    msg.writer().writeShort(0);
                }
                msg.writer().writeByte(-1);
                msg.writer().writeByte(0);
                msg.writer().writeByte(itemEquipment.getRank());
                msg.writer().writeByte(itemEquipment.getDamageType());
                msg.writer().writeByte(itemEquipment.isLock() ? 1 : 0);
                msg.writer().writeUTF(itemEquipment.getNameCharSeal());
                for (int k = 0; k < 10; k++) {
                    msg.writer().writeByte(0);
                }
                for (int k = 0; k < 3; k++) {
                    msg.writer().writeByte(0);
                }
                for (int k = 0; k < 15; k++) {
                    msg.writer().writeByte(0);
                }
            }
            msg.writer().writeShort(playerView.getInfo().getClan() == null ? -1 : playerView.getInfo().getClan().getIndexIcon()); // id clan
            msg.writer().writeByte(-1); // id fashion
            for (int k = 0; k < 5; k++) {
                msg.writer().writeShort(-1);
            }
            ItemAnimal animal = playerView.getHorse().getAnimalUse();
            msg.writer().writeByte((animal == null ? -1 : animal.getTemplate().getIdImage()));
            if (animal != null) {
                msg.writer().writeUTF(animal.getInfo());
            }
        } else {
            ItemAnimal animal = playerView.getHorse().getAnimalUse();
            if (animal == null) {
                msg.writer().writeByte(-1);
                return;
            }
            msg.writer().writeByte(animal.getItemBody().size());
            for (short i = 0; i < animal.getItemBody().size(); i++) {
                ItemEquip itemEquipment = animal.getItemBody().get(i);
                msg.writer().writeByte(itemEquipment.getClassChar());
                msg.writer().writeShort(itemEquipment.getIdItem());
                msg.writer().writeShort(itemEquipment.getTemplate().getId());
                msg.writer().writeByte(itemEquipment.getPlusTemplate());
                msg.writer().writeByte(itemEquipment.getLevel());
                msg.writer().writeShort(itemEquipment.getMDurable());
                msg.writer().writeShort(itemEquipment.getDurable());
                msg.writer().writeByte(itemEquipment.getColorName());
                for (int k = 0; k < 5; k++) {
                    msg.writer().writeShort(0);
                }
                msg.writer().writeByte(-1);
                msg.writer().writeByte(0);
                msg.writer().writeByte(itemEquipment.getRank());
                msg.writer().writeByte(itemEquipment.getDamageType());
                msg.writer().writeByte(itemEquipment.isLock() ? 1 : 0);
                msg.writer().writeUTF(itemEquipment.getNameCharSeal());
                msg.writer().writeShort(itemEquipment.getDayUse());
                for (int k = 0; k < 10; k++) {
                    msg.writer().writeByte(0);
                }
                for (int k = 0; k < 3; k++) {
                    msg.writer().writeByte(0);
                }
                for (int k = 0; k < 15; k++) {
                    msg.writer().writeByte(0);
                }
            }
            msg.writer().writeByte(animal.getTemplate().getNFrame());
            byte[] image = Manager.getImageAnimal((byte) animal.getTemplate().getIdImage());
            if (image != null) {
                for (int i = 0; i < image.length; i++) {
                    msg.writer().writeByte(image[i]);
                }
            }
        }
        player.getSession().sendMessage(msg);
    }

    public void sendInfoMe(@NonNull Player me) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_INFO);
        msg.writer().writeShort(me.getIdPlayer());
        msg.writer().writeUTF(me.getName());
        msg.writer().writeShort(me.getLocation().getX());
        msg.writer().writeShort(me.getLocation().getY());
        msg.writer().writeInt(me.isNpc() ? 1 : me.getPoint().getHp());
        msg.writer().writeInt(me.getPoint().getHpMax());
        msg.writer().writeInt(me.getPoint().getMp());
        msg.writer().writeInt(me.getPoint().getMpMax());
        msg.writer().writeByte(me.getInfo().getHead());
        msg.writer().writeByte(me.getInfo().getClassPlayer());
        for (int j = 0; j < me.getSkillBuff().getIdBuff().length; j++) {
            msg.writer().writeByte(me.getSkillBuff().getIdBuff()[j]);
        }
        for (int j = 0; j < me.getSkillBuff().getCoolDown().length; j++) {
            msg.writer().writeShort(me.getSkillBuff().getCoolDown()[j]);
        }
        msg.writer().writeShort(me.getInfo().getKiller());
        msg.writer().writeByte(me.getSundry().getPk());
        msg.writer().writeShort(me.getPoint().getDefend());
        msg.writer().writeShort(me.getPoint().getDefendMagic());
        msg.writer().writeByte(me.getInfo().getLevel());
        msg.writer().writeByte(Util.getHe(me.getInfo().getClassPlayer()));
        msg.writer().writeByte(me.getHorse().getUseHorse());
        msg.writer().writeByte(me.getHorse().isFly() ? 1 : 0);
        msg.writer().writeByte(me.getHorse().getImageHorse());
        msg.writer().writeByte(me.getPoint().getSpeed());
        Clan clan = me.getInfo().getClan();
        msg.writer().writeShort(clan == null ? -1 : clan.getIndexIcon());
        msg.writer().writeByte(me.isNpc() ? -(me.getIdDatabase() + 1) : -1); //id boss
        if (clan != null) {
            msg.writer().writeByte(me.getSundry().getClanMember().getIsMaster());
        }
        msg.writer().writeByte(0); //id fashion
        if (me.isNpc()) {
            NpcTemplate npcTemplate = Manager.getNpcTemplate((short) me.getIdDatabase());
            for (int k = 0; k < 5; k++) {
                msg.writer().writeShort(npcTemplate.getIdModels()[k]);
            }
        } else {
            for (int k = 0; k < 5; k++) {
                msg.writer().writeShort(-1);
            }
        }
        msg.writer().writeBoolean(false); // nộ
        msg.writer().writeBoolean(false); // request animal
        msg.writer().writeByte(me.getInfo().getIdNation());
        msg.writer().writeByte(me.getLocation().getInCountry());
        msg.writer().writeBoolean(true); // paint hat
        sendAnotherNotMeInMap(me, msg);
    }

    public void sendInfoPlayer(@NonNull Player player, short id) throws IOException {
        for (int i = 0; i < player.getLocation().getZone().getPlayers().size(); i++) {
            Player plInMap = player.getLocation().getZone().getPlayers().get(i);
            if (plInMap != null && plInMap.getIdPlayer() == id) {
                Message msg = new Message(CommandMessage.CHAR_INFO);
                msg.writer().writeShort(plInMap.getIdPlayer());
                msg.writer().writeUTF(plInMap.getName());
                msg.writer().writeShort(plInMap.getLocation().getX());
                msg.writer().writeShort(plInMap.getLocation().getY());
                msg.writer().writeInt(plInMap.isNpc() ? 1 : plInMap.getPoint().getHp());
                msg.writer().writeInt(plInMap.getPoint().getHpMax());
                msg.writer().writeInt(plInMap.getPoint().getMp());
                msg.writer().writeInt(plInMap.getPoint().getMpMax());
                msg.writer().writeByte(plInMap.getInfo().getHead());
                msg.writer().writeByte(plInMap.getInfo().getClassPlayer());
                for (int j = 0; j < plInMap.getSkillBuff().getIdBuff().length; j++) {
                    msg.writer().writeByte(plInMap.getSkillBuff().getIdBuff()[j]);
                }
                for (int j = 0; j < plInMap.getSkillBuff().getCoolDown().length; j++) {
                    msg.writer().writeShort(plInMap.getSkillBuff().getCoolDown()[j]);
                }
                msg.writer().writeShort(plInMap.getInfo().getKiller());
                msg.writer().writeByte(plInMap.getSundry().getPk());
                msg.writer().writeShort(plInMap.getPoint().getDefend());
                msg.writer().writeShort(plInMap.getPoint().getDefendMagic());
                msg.writer().writeByte(plInMap.getInfo().getLevel());
                msg.writer().writeByte(Util.getHe(plInMap.getInfo().getClassPlayer()));
                msg.writer().writeByte(plInMap.getHorse().getUseHorse());
                msg.writer().writeByte(plInMap.getHorse().isFly() ? 1 : 0);
                msg.writer().writeByte(plInMap.getHorse().getImageHorse());
                msg.writer().writeByte(plInMap.getPoint().getSpeed());
                Clan clan = plInMap.getInfo().getClan();
                msg.writer().writeShort(clan == null ? -1 : clan.getIndexIcon());
                msg.writer().writeByte(plInMap.isNpc() ? -(plInMap.getIdDatabase() + 1) : -1);
                if (clan != null) {
                    msg.writer().writeByte(plInMap.getSundry().getClanMember().getIsMaster());
                }
                msg.writer().writeByte(0); //id fashion
                if (plInMap.isNpc()) {
                    NpcTemplate npcTemplate = Manager.getNpcTemplate((short) plInMap.getIdDatabase());
                    for (int k = 0; k < 5; k++) {
                        msg.writer().writeShort(npcTemplate.getIdModels()[k]);
                    }
                } else {
                    for (int k = 0; k < 5; k++) {
                        msg.writer().writeShort(-1);
                    }
                }
                msg.writer().writeBoolean(false); // nộ
                msg.writer().writeBoolean(false); // request animal
                msg.writer().writeByte(plInMap.getInfo().getIdNation());
                msg.writer().writeByte(plInMap.getLocation().getInCountry());
                msg.writer().writeBoolean(true); // paint hat
                player.getSession().sendMessage(msg);
                InventoryService.instance.sendItemBody(plInMap, player);
                break;
            }
        }
    }

    @Synchronized
    public void updatePlayerInside(@NonNull Player pl, Message msgMove) throws IOException {
        Zone zone = pl.getLocation().getZone();
        if (zone.getMap().isOfflineMap()) {
            return;
        }
        int distanceLoad = pl.getSession().getDistanceLoad();
        for (Player player : zone.getPlayers()) {
            if (player != null && player.getIdPlayer() != pl.getIdPlayer()) {
                if (Util.getDistance(pl, player) < distanceLoad) {
                    if (!pl.getOtherPlayerInside().contains(player.getIdPlayer())) {
                        pl.getOtherPlayerInside().add(player.getIdPlayer());
                        if (player.isPlayer()) {
                            BuffService.instance.sendEffectBuffToPlayer(player, pl);
                        }
                    }
                    if (msgMove != null && player.isPlayer()) {
                        player.getSession().sendMessage(msgMove);
                    }
                    if (pl.getOtherPlayerInside().contains(player.getIdPlayer())) {
                        sendMove(player, pl);
                    }
                } else if (pl.getOtherPlayerInside().contains(player.getIdPlayer())) {
                    if (player.isPlayer()) {
                        removePlayerInMap(pl, player);
                        removePlayerInMap(player, pl);
                    }
                    pl.getOtherPlayerInside().removeIf(p -> p == player.getIdPlayer());
                }
            }
        }
    }

    @Synchronized
    public void updateMobInside(@NonNull Player pl) throws IOException {
        Zone zone = pl.getLocation().getZone();
        if (zone.getMap().isOfflineMap()) {
            return;
        }
        int distanceLoad = pl.getSession().getDistanceLoad();
        for (Monster mob : zone.getMobs()) {
            if (mob != null && !mob.isDie()) {
                if (Util.getDistance(pl, mob) < distanceLoad) {
                    if (!pl.getOtherMobInside().contains(mob.getId())) {
                        pl.getOtherMobInside().add(mob.getId());
                        MonsterService.instance.sendMonsterMove(pl, mob);
                    }
                } else if (pl.getOtherMobInside().contains(mob.getId())) {
                    pl.getOtherMobInside().removeIf(m -> m == mob.getId());
                }
            }
        }
    }

    @Synchronized
    public void updateItemMapInside(@NonNull Player pl) throws IOException {
        Zone zone = pl.getLocation().getZone();
        if (zone.getMap().isOfflineMap()) {
            return;
        }
        int distanceLoad = pl.getSession().getDistanceLoad();
        for (ItemMap item : zone.getItems()) {
            if (item != null) {
                if (Util.getDistance(pl.getLocation().getX(), pl.getLocation().getY(), item.getX(), item.getY()) < distanceLoad) {
                    if (!pl.getOtherItemMapInside().contains(item.getItemMapId())) {
                        pl.getOtherItemMapInside().add(item.getItemMapId());
                        ItemService.instance.sendItemInMap(pl, item);
                    }
                } else if (pl.getOtherItemMapInside().contains(item.getItemMapId())) {
                    pl.getOtherItemMapInside().removeIf(it -> it == item.getItemMapId());
                }
            }
        }
    }

    public void doChangeMap(@NonNull Player pl, short toMap, short toX, short toY) throws IOException, SQLException {
        if (pl.isDie()) {
            return;
        }
        if (toMap == -500) {
            updateMobInside(pl);
            updateItemMapInside(pl);
            updatePlayerInside(pl, null);
        } else {
            if (toMap == 31) {
                pl.getSession().disconnect();
                return;
            }
            if (pl.getLocation().getZone().getMap().getMapId() == 105) {
                ChangeMapService.instance.changeMap(pl, pl.getLocation().getMapVillage(), (short) 384, (short) 672);
                return;
            }
            if (pl.getLocation().getZone().getMap().isOfflineMap()) {
                ChangeMapService.instance.changeMap(pl, pl.getLocation().getLastZone(), pl.getLocation().getLastX(), pl.getLocation().getLastY());
                return;
            }
            if (Manager.getMap(pl.getLocation().getInCountry(), toMap).isOfflineMap()) {
                ChangeMapService.instance.changeMap(pl, toMap, (short) (toX * 16 + 8), (short) (toY * 16 + 8));
                return;
            }
            for (int i = 0; i < Manager.X_CHECK.length; i++) {
                byte xCheck = Manager.X_CHECK[i];
                byte yCheck = Manager.Y_CHECK[i];
                short newX = (short) (pl.getLocation().getX() + Manager.X_FOWARD[i]);
                short newY = (short) (pl.getLocation().getY() + Manager.Y_FOWARD[i]);
                if (isInWayPoint(pl, newX + xCheck, newY + yCheck) || isInWayPoint(pl, newX + xCheck * 2, newY + yCheck * 2)) {
                    ChangeMapService.instance.changeMap(pl, toMap, (short) (toX * 16 + 8), (short) (toY * 16 + 8));
                    return;
                }
            }

        }
    }

    public void sendLocationServer(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.LOCATION_SERVER);
        if (player.getLocation().getInCountry() == Const.THANH_LONG) {
            msg.writer().write(Manager.DATA_LOCATION_THANH_LONG);
        } else {
            msg.writer().write(Manager.DATA_LOCATION_HAC_HO);
        }
        player.getSession().sendMessage(msg);
    }

    public void sendXaPhuTemplate(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.XAPHU_TEMPLATE);
        msg.writer().write(Manager.DATA_XA_PHU_TEMPLATE);
        pl.getSession().sendMessage(msg);
    }

    public void sendDataMap(@NonNull Player pl, @NonNull IMap mapJoin, short xTo, short yTo) throws IOException {
        IMap mapLoad = mapJoin.isChildMap() ? mapJoin.getMapParent() : mapJoin;
        Message msg = new Message(CommandMessage.CHANGE_MAP);
        msg.writer().writeShort(mapLoad.getMapId());
        msg.writer().writeShort(xTo);
        msg.writer().writeShort(yTo);
        msg.writer().writeByte(mapLoad.getMapData().getIdXaPhu());
        msg.writer().writeShort(mapJoin.isChildMap() ? -1 : mapLoad.getMapId());
        msg.writer().writeUTF(mapJoin.isChildMap() ? mapJoin.getName() : mapLoad.getName());
        msg.writer().writeBoolean(true);
        msg.writer().write(mapLoad.getMapData().getW());
        msg.writer().write(mapLoad.getMapData().getH());
        for (short s : mapLoad.getMapData().getMap()) {
            msg.writer().write(s);
        }
        for (int i = 0; i < mapLoad.getMapData().getTileTops().size(); i++) {
            Actor tile = mapLoad.getMapData().getTileTops().get(i);
            msg.writer().write(tile.getX());
            if (tile.getX() == 255) {
                break;
            }
            msg.writer().write(tile.getSizeY());
            msg.writer().write(tile.getSizeX());
            msg.writer().write(tile.getPlusY());
            for (int k = 0; k < tile.getIndexs().length; k++) {
                msg.writer().write(tile.getIndexs()[k]);
            }
        }
        for (int i = 0; i < mapLoad.getMapData().getTileTops2().size(); i++) {
            Actor tile = mapLoad.getMapData().getTileTops2().get(i);
            msg.writer().write(tile.getX());
            if (tile.getX() == 255) {
                break;
            }
            msg.writer().write(tile.getY());
            msg.writer().write(tile.getIndex());
        }
        for (int i = 0; i < mapLoad.getMapData().getTrees().size(); i++) {
            Actor tree = mapLoad.getMapData().getTrees().get(i);
            msg.writer().write(tree.getX());
            if (tree.getX() == 255) {
                break;
            }
            msg.writer().write(tree.getY());
            msg.writer().write(tree.getIndex());
        }
        msg.writer().write(mapLoad.isOfflineMap() ? 1 : 0);
        msg.writer().write(mapLoad.getMapData().getLocationWayPoints().size());
        for (int i = 0; i < mapLoad.getMapData().getLocationWayPoints().size(); i++) {
            LoctionWayPoint wp = mapLoad.getMapData().getLocationWayPoints().get(i);
            msg.writer().write(wp.getX());
            msg.writer().write(wp.getY());
            int[] array = new int[2];
            short toM = wp.getToMap();
            for (int num24 = 0; num24 < 2; num24++) {
                array[num24] = toM & 0xFF;
                toM >>= 8;
            }
            msg.writer().write(array[0]);
            msg.writer().write(array[1]);
            msg.writer().write(wp.getToX());
            msg.writer().write(wp.getToY());
        }

        msg.writer().write(mapLoad.getMapData().getNpcs().size());
        for (int i = 0; i < mapLoad.getMapData().getNpcs().size(); i++) {
            Actor npc = mapLoad.getMapData().getNpcs().get(i);
            msg.writer().write(npc.getX());
            msg.writer().write(npc.getY());
            msg.writer().write(npc.getIndex());
        }
        pl.getSession().sendMessage(msg);
    }

    public boolean isInWayPoint(@NonNull Player pl, int px, int py) {
        IMap map = pl.getLocation().getZone().getMap();
        if ((py >> Const.SIP) * map.getMapData().getW() + (px >> Const.SIP) >= map.getMapData().getType().length || (py >> Const.SIP) * map.getMapData().getW() + (px >> Const.SIP) < 0) {
            return false;
        }
        return (map.getMapData().getType()[(py >> Const.SIP) * map.getMapData().getW() + (px >> Const.SIP)] < 2000000000) ? false : (map.getMapData().getLocationWayPoints().get(map.getMapData().getType()[(py >> Const.SIP) * map.getMapData().getW() + (px >> Const.SIP)] - 2000000000)) != null;
    }

    public boolean tileTypeAtPixel(@NonNull Player pl, int px, int py, int t) {
        IMap map = pl.getLocation().getZone().getMap();
        int num = (py >> Const.SIP) * map.getMapData().getW() + (px >> Const.SIP);
        return num < 0 || num >= map.getMapData().getType().length || (map.getMapData().getType()[num] & t) == t;
    }

    public void sendAllPlayerInMap(@NonNull Monster mob, @NonNull Message msg) {
        for (int i = 0; i < mob.getZone().getPlayers().size(); i++) {
            Player plInMap = mob.getZone().getPlayers().get(i);
            if (plInMap != null && plInMap.isPlayer() && Util.getDistance(plInMap, mob) <= plInMap.getSession().getDistanceLoad()) {
                plInMap.getSession().sendMessage(msg);
            }
        }
    }

    public void sendAllPlayerInMap(@NonNull Player pl, @NonNull Message msg) {
        for (int i = 0; i < pl.getLocation().getZone().getPlayers().size(); i++) {
            Player plInMap = pl.getLocation().getZone().getPlayers().get(i);
            if (plInMap != null && plInMap.isPlayer() && Util.getDistance(pl, plInMap) <= pl.getSession().getDistanceLoad()) {
                plInMap.getSession().sendMessage(msg);
            }
        }
    }

    public void sendAllPlayerInMap(@NonNull Zone zone, @NonNull Message msg) {
        for (int i = 0; i < zone.getPlayers().size(); i++) {
            Player plInMap = zone.getPlayers().get(i);
            if (plInMap != null && plInMap.isPlayer()) {
                plInMap.getSession().sendMessage(msg);
            }
        }
    }

    public void sendAnotherNotMeInMap(@NonNull Player pl, @NonNull Message msg) {
        for (int i = 0; i < pl.getLocation().getZone().getPlayers().size(); i++) {
            Player plInMap = pl.getLocation().getZone().getPlayers().get(i);
            if (plInMap != null && plInMap.getIdPlayer() != pl.getIdPlayer() && plInMap.isPlayer() && Util.getDistance(pl, plInMap) <= pl.getSession().getDistanceLoad()) {
                plInMap.getSession().sendMessage(msg);
            }
        }
    }

    public void exitMap(@NonNull Player player) throws IOException {
        if (player.getLocation().getZone() != null) {
            player.getLocation().getZone().removePlayer(player);
            if (!player.getLocation().getZone().getMap().isOfflineMap()) {
                removePlayerInMap(player);
            }
        }
    }

    public void sendUpToBoard(@NonNull Player player) throws IOException {
        player.getSundry().setOnBoard(true);
        player.getSundry().setLastTimeOnBoard(System.currentTimeMillis());
        ChangeMapService.instance.changeMap(player, (short) 107, (short) 200, (short) 200);
        Message msg = new Message(CommandMessage.UP_TO_BOARD);
        msg.writer().writeByte(10);
        player.getSession().sendMessage(msg);
    }

    public void sendNpcServer(@NonNull Player player, @NonNull NpcServer npc) throws IOException {
        Message msg = new Message(CommandMessage.BOSS_IMG);
        msg.writer().writeUTF(npc.getTemplate().getName());
        msg.writer().writeShort(npc.getTemplate().getId());
        msg.writer().writeShort(npc.getTemplate().getIdImage());
        msg.writer().writeShort(npc.getX());
        msg.writer().writeShort(npc.getY());
        msg.writer().writeShort(npc.getTemplate().getW0());
        msg.writer().writeShort(npc.getTemplate().getH0());
        msg.writer().writeByte(npc.getTemplate().getFrame());
        msg.writer().writeByte(npc.getTemplate().getTypeLimit());
        player.getSession().sendMessage(msg);
    }

    public void playerJoinMap(@NonNull Player pl, @NonNull Zone z) throws IOException {
        Zone oldZone = pl.getLocation().getZone();
        if (oldZone != null) {
            exitMap(pl);
        }
        pl.getLocation().setZone(z);
        z.addPlayer(pl);
        pl.getOtherMobInside().clear();
        pl.getOtherItemMapInside().clear();
        pl.getOtherPlayerInside().clear();
    }

    public byte getValidZoneId(short zoneId, @NonNull IMap mapCheck) {
        return (zoneId == -1) ? (byte) Util.nextInt(getZoneCount(mapCheck)) : (byte) zoneId;
    }

    public Zone getValidZone(@NonNull IMap mapCheck, byte zone) {
        return mapCheck.getZone(zone);
    }

    public int getZoneCount(@NonNull IMap mapCheck) {
        return mapCheck.getZones().size();
    }
}
