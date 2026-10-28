package services;

import consts.Const;
import consts.HorseConst;
import item.ItemAnimal;
import item.ItemEquip;
import item.ItemPotion;
import java.io.IOException;
import lombok.NonNull;
import manager.Manager;
import network.Message;
import player.Player;
import utils.CommandMessage;
import utils.Printer;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class UseItemService {

    public static final UseItemService instance = new UseItemService();

    public void useItemPotion(@NonNull Player player, byte id) throws IOException {
        if (player.isDie()) {
            return;
        }
        ItemPotion potion = InventoryService.instance.findItemPotion(player, id);
        if (potion == null || potion.getQuantity() <= 0) {
            return;
        }
        if (!Util.canDoWithTime(player.getInventory().getLastTimeUsePotion()[id], potion.getTemplate().getDelay())
                || (player.getPoint().isFullHp() && potion.isHpAverage())
                || (player.getPoint().isFullMp() && potion.isMpAverage())
                || (id == 19 && player.getLocation().getZone().getMap().isMapVillage())) {
            return;
        }
        player.getInventory().getLastTimeUsePotion()[id] = System.currentTimeMillis();
        if (potion.isHpAverage()) {
            short valueAdd = (short) Manager.getMpHpPlus(0, id);
            player.getPoint().plusHp(valueAdd);
            onUsePotionHp(player, potion, valueAdd);
            InventoryService.instance.minusQuantityItemPotion(player, potion, (short) 1);
        } else if (potion.isMpAverage()) {
            short valueAdd = (short) Manager.getMpHpPlus(1, id);
            player.getPoint().plusMp(valueAdd);
            onUsePotionMp(player, potion, valueAdd);
            InventoryService.instance.minusQuantityItemPotion(player, potion, (short) 1);
        } else {
            switch (id) {
                case 19 -> {
                    if (player.getLocation().getZone().getMap().isMapVillage()) {
                        Service.instance.sendLogOut(player.getSession(), "Không thể sử dụng trong làng. ");
                        return;
                    }
                    player.getSundry().setLastTimeComeHome(System.currentTimeMillis());
                    player.getSundry().setComeHome(true);
                }
                case 14, 15, 16, 17, 18 -> {
                    if (player.getSundry().getPk() == 0 && player.getSundry().isKiller()) {
                        Service.instance.sendLogOut(player.getSession(), "Không thể đeo khăn khi đang phạm tội");
                        return;
                    }
                    if (player.getSundry().getPk() != 0 && Util.canDoWithTime(player.getSundry().getLastTimeChangePk(), 180000)) {
                        player.getSundry().setPk((byte) 0);
                        onUseItemPk(player);
                        return;
                    }
                    player.getSundry().setPk(id);
                    onUseItemPk(player);
                }
                case 100 -> {
                    TextBoxService.instance.sendChatWorld(player);
                }
                case 91 -> {
                    ChangeMapService.instance.changeMap(player, (short) 17, (short) -1, (short) -1);
                    InventoryService.instance.minusQuantityItemPotion(player, potion, (short) 1);
                }
                case 29 -> {
                    player.getInfo().minusKiller((byte) 100);
                    if (player.getInfo().getKiller() <= 0) {
                        player.getSundry().setKiller(false);
                    }
                    MapService.instance.sendKiller(player);
                    InventoryService.instance.minusQuantityItemPotion(player, potion, (short) 1);
                }
                case 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 -> {
                    byte head = (byte) (id - 37);
                    String message = (head == 1 || head == 3 || head == 5 || head == 7 || head == 9 || head == 11)
                            ? (player.getInfo().getGender() == Const.MALE ? "Chỉ dành cho nữ" : "")
                            : (player.getInfo().getGender() == Const.FEMALE ? "Chỉ dành cho nam" : "");
                    if (!message.isEmpty()) {
                        Service.instance.sendLogOut(player.getSession(), message);
                        return;
                    }
                    player.getInfo().setHead(head);
                    Service.instance.sendMainCharInfo(player);
                    MapService.instance.sendInfoMe(player);
                    InventoryService.instance.minusQuantityItemPotion(player, potion, (short) 1);
                }
                case 30 -> {
                    if (player.getHorse().getUseHorse() != HorseConst.NON_HORSE) {
                        Service.instance.sendLogOut(player.getSession(), "Vui lòng xuống ngựa");
                        return;
                    }
                    player.getHorse().setFly(false);
                    player.getHorse().setUseHorse(HorseConst.HORSE);
                    player.getHorse().setImageHorse(HorseConst.IMAGE_THIEN_LY_MA);
                    player.getHorse().setIdItem(id);
                    player.getPoint().initPoint();
                    Service.instance.sendMainCharInfo(player);
                    MapService.instance.sendInfoMe(player);
                    InventoryService.instance.minusQuantityItemPotion(player, potion, (short) 1);
                }
                case 34 -> {
                    if (player.getHorse().getUseHorse() != HorseConst.NON_HORSE) {
                        Service.instance.sendLogOut(player.getSession(), "Vui lòng xuống ngựa");
                        return;
                    }
                    player.getHorse().setFly(false);
                    player.getHorse().setUseHorse(HorseConst.HORSE);
                    player.getHorse().setImageHorse(HorseConst.IMAGE_XICH_THO);
                    player.getHorse().setIdItem(id);
                    player.getPoint().initPoint();
                    Service.instance.sendMainCharInfo(player);
                    MapService.instance.sendInfoMe(player);
                    InventoryService.instance.minusQuantityItemPotion(player, potion, (short) 1);
                }
                default -> {
                    Service.instance.sendLogOut(player.getSession(), "Không thể sử dụng");
                    Printer.printRed("Id Potion Has't Been Written " + id);
                }
            }
        }
        InventoryService.instance.sendItemPotion(player);
    }

    public void riderAnimal(@NonNull Player player, short id, byte typeAnimal) throws IOException {
        if (player.getHorse().getUseHorse() != HorseConst.NON_HORSE) {
            Service.instance.sendLogOut(player.getSession(), "Vui lòng xuống ngựa");
            return;
        }
        ItemAnimal animal = InventoryService.instance.findItemAnimal(player, id, typeAnimal);
        if (animal == null) {
            return;
        }
        InventoryService.instance.removeItemAnimal(player, animal);
        byte useHorse = -1;
        byte imgHorse = -1;
        player.getHorse().setFly(false);
        switch (animal.getTemplate().getIdImage()) {
            case HorseConst.BACH_MA -> {
                useHorse = HorseConst.HORSE;
                imgHorse = HorseConst.IMAGE_BACH_MA;
            }
            case HorseConst.MANH_HO -> {
                useHorse = HorseConst.HORSE_MANH_HO;
                imgHorse = HorseConst.IMAGE_MANH_HO;
            }
            case HorseConst.SOI_XAM -> {
                useHorse = HorseConst.HORSE_SOI_XAM;
                imgHorse = HorseConst.IMAGE_SOI_XAM;
            }
            case HorseConst.TIEN_HAC -> {
                useHorse = HorseConst.HORSE_TIEN_HAC;
                imgHorse = HorseConst.IMAGE_TIEN_HAC;
                player.getHorse().setFly(true);
            }
            case HorseConst.HAC_NGUU -> {
                useHorse = HorseConst.HORSE_HAC_NGUU;
                imgHorse = HorseConst.IMAGE_HAC_NGUU;
            }
        }
        player.getHorse().setUseHorse(useHorse);
        player.getHorse().setImageHorse(imgHorse);
        player.getHorse().setAnimalUse(animal);
        player.getPoint().initPoint();
        Service.instance.sendMainCharInfo(player);
        InventoryService.instance.sendItemBody(player);
        InventoryService.instance.sendItemBodyAnimal(player);
        InventoryService.instance.sendItemAnimal(player);
        MapService.instance.sendInfoMe(player);
    }

    public void useItemEquipment(@NonNull Player player, short index) throws IOException {
        if (player.isDie()) {
            return;
        }
        ItemEquip equipment = InventoryService.instance.findItemBag(player, index);
        if (equipment == null) {
            return;
        }
        if (equipment.getTemplate().getGender() != 0 && equipment.getTemplate().getGender() != player.getInfo().getGender()) {
            return;
        }
        if (equipment.getClassChar() != -1 && equipment.getClassChar() != player.getInfo().getClassPlayer() && equipment.isWeapon()) {
            return;
        }
        if (equipment.getLevel() > equipment.getLevel()) {
            return;
        }
        ItemEquip hasEquipment = InventoryService.instance.findItemBodyByType(player, equipment.getTemplate().getType());
        if (hasEquipment != null) {
            InventoryService.instance.swapItemBagToBody(player, equipment, hasEquipment);
        } else {
            InventoryService.instance.removeItemBagEquipment(player, equipment);
            InventoryService.instance.addItemBodyEquipment(player, equipment);
        }
        InventoryService.instance.sendWeaponImage(player);
        InventoryService.instance.sendItemBag(player);
        InventoryService.instance.sendItemBody(player);
        player.getPoint().initPoint();
        MapService.instance.onNewHpMp(player);
    }

    private void onUseItemPk(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.USE_ITEM_PK);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(player.getSundry().getPk() != 0 ? 1 : 0);
        msg.writer().writeByte(player.getSundry().getPk());
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    private void onUsePotionMp(@NonNull Player player, ItemPotion itemPotion, short valueAdd) throws IOException {
        Message msg = new Message(CommandMessage.USE_POTION);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(itemPotion.getTemplate().getId());
        msg.writer().writeShort(valueAdd);
        msg.writer().writeInt(player.getPoint().getMp());
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    private void onUsePotionHp(@NonNull Player player, ItemPotion itemPotion, short valueAdd) throws IOException {
        Message msg = new Message(CommandMessage.USE_POTION);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(itemPotion.getTemplate().getId());
        msg.writer().writeShort(valueAdd);
        msg.writer().writeInt(player.getPoint().getHp());
        msg.writer().writeByte(1);
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    public void onPlusHp(@NonNull Player player, short valueAdd) throws IOException {
        Message msg = new Message(CommandMessage.USE_POTION);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(4);
        msg.writer().writeShort(valueAdd);
        msg.writer().writeInt(player.getPoint().getHp());
        msg.writer().writeByte(1);
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    public void onPlusMp(@NonNull Player player, short valueAdd) throws IOException {
        Message msg = new Message(CommandMessage.USE_POTION);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(4);
        msg.writer().writeShort(valueAdd);
        msg.writer().writeInt(player.getPoint().getMp());
        MapService.instance.sendAllPlayerInMap(player, msg);
    }
}
