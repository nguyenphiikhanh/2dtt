package services;

import item.Attribute;
import item.ItemEquip;
import item.ItemGem;
import item.ItemPotion;
import item.ItemQuest;
import java.io.IOException;
import lombok.NonNull;
import lombok.Synchronized;
import manager.Manager;
import network.Message;
import player.Player;
import template.NpcTemplate;
import utils.CommandMessage;
import consts.Const;
import consts.ItemEquipConst;
import consts.NpcConst;
import item.ItemAnimal;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class InventoryService {

    public static final InventoryService instance = new InventoryService();

    public void swapItemBagToBody(@NonNull Player player, @NonNull ItemEquip itemBag, @NonNull ItemEquip itemBody) {
        removeItemBagEquipment(player, itemBag);
        removeItemBodyEquipment(player, itemBody);
        addItemBodyEquipment(player, itemBag);
        addItemBagEquipment(player, itemBody);
    }

    public void addItemBoxEquipment(@NonNull Player player, short idItem) throws IOException {
        if (player.getInventory().getItemBox().size() >= 15) {
            Service.instance.sendLogOut(player.getSession(), "Rương đầy");
            return;
        }
        ItemEquip itemBag = findItemBag(player, idItem);
        if (itemBag == null) {
            return;
        }
        removeItemBagEquipment(player, itemBag);
        addItemBoxEquipment(player, itemBag);
        sendPutItemToBox(player, idItem);
    }

    public void getItemEquipmentFromBox(@NonNull Player player, short idItem) throws IOException {
        if (player.getInventory().isFullInventory()) {
            Service.instance.sendLogOut(player.getSession(), "Hành trang không đủ ô trống");
            return;
        }
        ItemEquip itemBox = findItemBox(player, idItem);
        if (itemBox == null) {
            return;
        }
        addItemBagEquipment(player, itemBox);
        sendGetItemToBox(player, idItem);
    }

    public void repairItem(@NonNull Player player, byte type) throws IOException {
        int price = player.getInventory().getPriceRepair(type);
        if (!player.getInventory().minusXu(price)) {
            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(price)));
            return;
        }
        switch (type) {
            case ItemEquipConst.REPAIR_EQUIP -> {
                for (int i = 0; i < player.getInventory().getItemBody().size(); i++) {
                    ItemEquip item = player.getInventory().getItemBody().get(i);
                    if (item.getTemplate().getType() == 3 || item.getTemplate().getType() == 4 || item.getTemplate().getType() == 5 || item.getTemplate().getType() == 6 || item.getTemplate().getType() == 7) {
                        continue;
                    }
                    short mDurable = item.getTemplate().getDurable();
                    item.setDurable(mDurable);
                    item.setMDurable(mDurable);
                }
            }
            case ItemEquipConst.REPAIR_WEAPON -> {
                ItemEquip weapon = findItemBodyByType(player, (byte) (3 + player.getInfo().getClassPlayer()));
                short mDurable = weapon.getTemplate().getDurable();
                weapon.setDurable(mDurable);
                weapon.setMDurable(mDurable);
            }
            case ItemEquipConst.REPAIR_ALL -> {
                for (int i = 0; i < player.getInventory().getItemBody().size(); i++) {
                    ItemEquip item = player.getInventory().getItemBody().get(i);
                    short mDurable = item.getTemplate().getDurable();
                    item.setDurable(mDurable);
                    item.setMDurable(mDurable);
                }
            }
        }
        InventoryService.instance.sendItemBody(player);
        sendSuccessRepairItem(player);
    }

    @Synchronized
    public void addItemBodyEquipment(@NonNull Player player, @NonNull ItemEquip itemEquipment) {
        player.getInventory().getItemBody().add(itemEquipment);
    }

    @Synchronized
    public void addItemSoldEquipment(@NonNull Player player, @NonNull ItemEquip itemEquipment) throws IOException {
        if (player.getInventory().getItemSold().size() >= 60) {
            player.getInventory().getItemSold().remove(0);
        }
        player.getInventory().getItemSold().add(itemEquipment);
    }

    @Synchronized
    public void addItemBagEquipment(@NonNull Player player, @NonNull ItemEquip itemEquipment) {
        player.getInventory().getItemBag().add(itemEquipment);
        initIdItemEquip(player, itemEquipment);
    }

    @Synchronized
    public void addItemBoxEquipment(@NonNull Player player, @NonNull ItemEquip itemEquipment) {
        player.getInventory().getItemBox().add(itemEquipment);
    }

    @Synchronized
    public void removeItemBodyEquipment(@NonNull Player player, @NonNull ItemEquip item) {
        player.getInventory().getItemBody().remove(item);
    }

    @Synchronized
    public void removeItemBoxEquipment(@NonNull Player player, @NonNull ItemEquip item) {
        player.getInventory().getItemBox().remove(item);
    }

    @Synchronized
    public void removeItemSoldEquipment(@NonNull Player player, @NonNull ItemEquip item) {
        player.getInventory().getItemSold().remove(item);
    }

    @Synchronized
    public void removeItemBagEquipment(@NonNull Player player, @NonNull ItemEquip item) {
        player.getInventory().getItemBag().remove(item);
        player.getSundry().removeDepositeItemEquip(item);
    }

    @Synchronized
    public void addItemPotion(@NonNull Player player, @NonNull ItemPotion itemPotion) {
        ItemPotion haveItem = findItemPotion(player, itemPotion.getTemplate().getId());
        if (haveItem != null) {
            haveItem.plusQuantity(itemPotion.getQuantity());
            itemPotion.dispose();
            return;
        }
        player.getInventory().getItemPotion().add(itemPotion);
    }

    @Synchronized
    public void addItemAnimal(@NonNull Player player, @NonNull ItemAnimal itemAnimal) {
        if (itemAnimal.getTemplate().getType() == 0) {
            player.getInventory().getItemAnimal().add(itemAnimal);
        } else {
            player.getInventory().getItemAnimalExpiry().add(itemAnimal);
        }
    }

    @Synchronized
    public void removeItemAnimal(@NonNull Player player, @NonNull ItemAnimal itemAnimal) {
        if (itemAnimal.getTemplate().getType() == 0) {
            player.getInventory().getItemAnimal().remove(itemAnimal);
        } else {
            player.getInventory().getItemAnimalExpiry().remove(itemAnimal);
        }
    }

    @Synchronized
    public void minusQuantityItemPotion(@NonNull Player player, @NonNull ItemPotion itemPotion, short quantity) {
        itemPotion.minusQuantity(quantity);
        if (itemPotion.getQuantity() <= 0) {
            InventoryService.instance.removeItemPotion(player, itemPotion);
        }
    }

    @Synchronized
    public void removeItemPotion(@NonNull Player player, @NonNull ItemPotion itemPotion) {
        player.getInventory().getItemPotion().remove(itemPotion);
    }

    @Synchronized
    public void addItemGem(@NonNull Player player, @NonNull ItemGem itemGem) {
        ItemGem haveItem = findItemGem(player, itemGem.getTemplate().getId());
        if (haveItem != null) {
            haveItem.plusQuantity(itemGem.getQuantity());
            itemGem.dispose();
            return;
        }
        player.getInventory().getItemGem().add(itemGem);
        initIdItemGem(player, itemGem);
    }

    @Synchronized
    public void removeItemGem(@NonNull Player player, @NonNull ItemGem itemGem) {
        player.getInventory().getItemGem().remove(itemGem);
        player.getSundry().removeDepositeItemGem(itemGem);
    }

    @Synchronized
    public void minusQuantityItemGem(@NonNull Player player, @NonNull ItemGem itemGem, short quantity) {
        itemGem.minusQuantity(quantity);
        if (itemGem.getQuantity() <= 0) {
            InventoryService.instance.removeItemGem(player, itemGem);
        }
    }

    @Synchronized
    public void addItemGemLock(@NonNull Player player, @NonNull ItemGem itemGem) {
        ItemGem haveItem = findItemGemLock(player, itemGem.getTemplate().getId());
        if (haveItem != null) {
            haveItem.plusQuantity(itemGem.getQuantity());
            itemGem.dispose();
            return;
        }
        player.getInventory().getItemGemLock().add(itemGem);
        initIdItemGem(player, itemGem);
    }

    @Synchronized
    public void removeItemGemLock(@NonNull Player player, @NonNull ItemGem itemGem) {
        player.getInventory().getItemGemLock().remove(itemGem);
    }

    @Synchronized
    public void minusQuantityItemGemLock(@NonNull Player player, @NonNull ItemGem itemGem, short quantity) {
        itemGem.minusQuantity(quantity);
        if (itemGem.getQuantity() <= 0) {
            InventoryService.instance.removeItemGemLock(player, itemGem);
        }
    }

    public ItemAnimal findItemAnimal(@NonNull Player player, short id, byte type) {
        if (type == 0) {
            return player.getInventory().getItemAnimal().stream().filter(it -> it != null && it.getId() == id).findFirst().orElse(null);
        } else {
            return player.getInventory().getItemAnimalExpiry().stream().filter(it -> it != null && it.getId() == id).findFirst().orElse(null);
        }
    }

    public ItemGem findItemGemLock(@NonNull Player player, short id) {
        return player.getInventory().getItemGemLock().stream().filter(it -> it != null && it.getTemplate().getId() == id).findFirst().orElse(null);
    }

    public ItemGem findItemGem(@NonNull Player player, short id) {
        return player.getInventory().getItemGem().stream().filter(it -> it != null && it.getTemplate().getId() == id).findFirst().orElse(null);
    }

    public ItemGem findItemGemByItemId(@NonNull Player player, short id) {
        return player.getInventory().getItemGem().stream().filter(it -> it != null && it.getIdItem() == id).findFirst().orElse(null);
    }

    public ItemPotion findItemPotion(@NonNull Player player, short id) {
        return player.getInventory().getItemPotion().stream().filter(it -> it != null && it.getTemplate().getId() == id).findFirst().orElse(null);
    }

    public ItemEquip findItemSold(@NonNull Player player, short id) {
        return player.getInventory().getItemSold().stream().filter(it -> it != null && it.getIdItem() == id).findFirst().orElse(null);
    }

    public ItemEquip findItemBox(@NonNull Player player, short id) {
        return player.getInventory().getItemBox().stream().filter(it -> it != null && it.getIdItem() == id).findFirst().orElse(null);
    }

    public ItemEquip findItemBag(@NonNull Player player, short id) {
        return player.getInventory().getItemBag().stream().filter(it -> it != null && it.getIdItem() == id).findFirst().orElse(null);
    }

    public ItemEquip findItemBody(@NonNull Player player, short id, short idTemplate) {
        return player.getInventory().getItemBody().stream().filter(it -> it != null && it.getIdItem() == id && it.getTemplate().getId() == idTemplate).findFirst().orElse(null);
    }

    public ItemEquip findItemBodyByType(@NonNull Player player, byte type) {
        return player.getInventory().getItemBody().stream().filter(it -> it != null && it.getTemplate().getType() == type).findFirst().orElse(null);
    }

    public int sumAttributeValueForId(@NonNull Player player, byte attributeId) {
        int sum = 0;
        for (ItemEquip item : player.getInventory().getItemBody()) {
            if (item != null) {
                sum += item.getValue(attributeId);
            }
        }
        return sum;
    }

    public void sendItemBody(@NonNull Player player, Player... playerRecive) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_WEARING);
        msg.writer().writeByte(0);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(player.getInventory().getItemBody().size());
        for (short i = 0; i < player.getInventory().getItemBody().size(); i++) {
            ItemEquip itemEquipment = player.getInventory().getItemBody().get(i);
            msg.writer().writeByte(itemEquipment.getClassChar());
            msg.writer().writeShort(itemEquipment.getIdItem());
            msg.writer().writeShort(itemEquipment.getTemplate().getId());
            msg.writer().writeByte(itemEquipment.getPlusTemplate());
            msg.writer().writeByte(itemEquipment.getLevel());
            msg.writer().writeShort(itemEquipment.getMDurable());
            msg.writer().writeShort(itemEquipment.getDurable());
            msg.writer().writeByte(itemEquipment.getColorName());
            msg.writer().writeByte(itemEquipment.getViTriVe());
            msg.writer().writeByte(itemEquipment.getHe());
            msg.writer().writeByte(0); //bekicked
            msg.writer().writeByte(itemEquipment.getRank());
            msg.writer().writeByte(itemEquipment.getDamageType());
            msg.writer().writeByte(itemEquipment.isLock() ? 1 : 0);
            msg.writer().writeUTF(itemEquipment.getNameCharSeal());
            msg.writer().writeShort(itemEquipment.getDayUse());
            msg.writer().writeByte(itemEquipment.getItemAttributes().size());
            for (int j = 0; j < itemEquipment.getItemAttributes().size(); j++) {
                Attribute att = itemEquipment.getItemAttributes().get(j);
                msg.writer().writeByte(att.getTemplate().getId());
                msg.writer().writeShort(att.getValue());
            }
        }
        msg.writer().writeByte(-1);// id fashion
        if (player.isNpc()) {
            NpcTemplate npcTemplate = Manager.getNpcTemplate((short) player.getIdDatabase());
            for (int k = 0; k < 5; k++) {
                msg.writer().writeShort(npcTemplate.getIdModels()[k]);
            }
        } else {
            for (int k = 0; k < 5; k++) {
                msg.writer().writeShort(-1);
            }
        }
        ItemAnimal animal = player.getHorse().getAnimalUse();
        msg.writer().writeByte((animal == null ? -1 : animal.getTemplate().getIdImage()));
        if (animal != null) {
            msg.writer().writeUTF(animal.getInfo());
            msg.writer().writeByte(animal.getTemplate().getIdImage());
        }
        msg.writer().writeByte(-1);// animal move
        if (playerRecive != null && playerRecive.length > 0) {
            playerRecive[0].getSession().sendMessage(msg);
            return;
        }
        player.getSession().sendMessage(msg);
    }

    public void sendWeaponImage(@NonNull Player player) throws IOException {
        Message m = new Message(CommandMessage.GET_WEAPONE);
        m.writer().writeByte(1);
        ItemEquip weapon = findItemBodyByType(player, (byte) (3 + player.getInfo().getClassPlayer()));
        if (weapon != null) {
            m.writer().writeByte(weapon.getIdItem());
            byte[][] image = Manager.getImageWeapon(weapon.getTemplate().getId());
            m.writer().writeShort(image[0].length);
            for (int i = 0; i < image[0].length; i++) {
                m.writer().writeByte(image[0][i]);
            }
            m.writer().writeShort(image[1].length);
            for (int i = 0; i < image[1].length; i++) {
                m.writer().writeByte(image[1][i]);
            }
            m.writer().writeByte(weapon.getTemplate().getDxWear());
            m.writer().writeByte(weapon.getTemplate().getDyWear());
        } else {
            m.writer().writeByte(-1);
        }
        player.getSession().sendMessage(m);
    }

    public void sendPutItemToBox(@NonNull Player player, short idItem) throws IOException {
        Message msg = new Message(CommandMessage.PUT_ITEM_2_BAG);
        msg.writer().writeShort(idItem);
        player.getSession().sendMessage(msg);
    }

    public void sendGetItemToBox(@NonNull Player player, short index) throws IOException {
        Message msg = new Message(CommandMessage.GET_ITEM_OUT_BAG);
        msg.writer().writeShort(index);
        player.getSession().sendMessage(msg);
    }

    public void sendItemBodyAnimal(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_WEARING);
        msg.writer().writeByte(1);
        msg.writer().writeShort(player.getIdPlayer());
        ItemAnimal animal = player.getHorse().getAnimalUse();
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
            msg.writer().writeByte(itemEquipment.getViTriVe());
            msg.writer().writeByte(itemEquipment.getHe());
            msg.writer().writeByte(0); //bekicked
            msg.writer().writeByte(itemEquipment.getRank());
            msg.writer().writeByte(itemEquipment.getDamageType());
            msg.writer().writeByte(itemEquipment.isLock() ? 1 : 0);
            msg.writer().writeUTF(itemEquipment.getNameCharSeal());
            msg.writer().writeShort(itemEquipment.getDayUse());
            msg.writer().writeByte(itemEquipment.getItemAttributes().size());
            for (int j = 0; j < itemEquipment.getItemAttributes().size(); j++) {
                Attribute att = itemEquipment.getItemAttributes().get(j);
                msg.writer().writeByte(att.getTemplate().getId());
                msg.writer().writeShort(att.getValue());
            }
        }
        msg.writer().writeByte(animal.getTemplate().getNFrame());
        byte[] image = Manager.getImageAnimal((byte) animal.getTemplate().getIdImage());
        if (image != null) {
            for (int i = 0; i < image.length; i++) {
                msg.writer().writeByte(image[i]);
            }
        }
        player.getSession().sendMessage(msg);
    }

    public void sendItemAnimal(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_INVENTORY);
        msg.writer().writeByte(2);
        msg.writer().writeInt(player.getInventory().getLuong());
        msg.writer().writeByte(player.getInventory().getItemAnimal().size());
        for (short i = 0; i < player.getInventory().getItemAnimal().size(); i++) {
            ItemAnimal animal = player.getInventory().getItemAnimal().get(i);
            animal.setId((short) (player.getIdPlayer() + i));
            msg.writer().writeShort(animal.getId());
            msg.writer().writeByte(animal.getTemplate().getIdImage());
            msg.writer().writeByte(animal.getTemplate().getIdImage());
            msg.writer().writeByte(animal.getLevel());
            msg.writer().writeUTF(animal.getTemplate().getName());
            msg.writer().writeUTF(animal.getInfo());
            msg.writer().writeByte(animal.getTemplate().getType());
        }
        msg.writer().writeByte(player.getInventory().getItemAnimalExpiry().size());
        for (short i = 0; i < player.getInventory().getItemAnimalExpiry().size(); i++) {
            ItemAnimal animal = player.getInventory().getItemAnimalExpiry().get(i);
            animal.setId((short) (player.getIdPlayer() + i));
            msg.writer().writeShort(animal.getId());
            msg.writer().writeByte(animal.getTemplate().getIdImage());
            msg.writer().writeByte(animal.getTemplate().getIdImage());
            msg.writer().writeUTF(animal.getTemplate().getName());
            msg.writer().writeByte(animal.getTemplate().getType());
            msg.writer().writeUTF(animal.getInfo());
            msg.writer().writeInt(animal.getMinutes());
            msg.writer().writeByte(animal.getTemplate().getType());
        }
        msg.writer().writeInt(player.getInventory().getLuongKhoa());
        player.getSession().sendMessage(msg);
    }

    public void sendOpenBox(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.NPC_INFO);
        msg.writer().writeByte(Const.SHOP_KEEP_ITEM);
        msg.writer().writeByte(player.getInventory().getItemBox().size());
        for (short i = 0; i < player.getInventory().getItemBox().size(); i++) {
            ItemEquip itemEquipment = player.getInventory().getItemBox().get(i);
            msg.writer().writeByte(itemEquipment.getClassChar());
            msg.writer().writeShort(itemEquipment.getIdItem());
            msg.writer().writeShort(itemEquipment.getTemplate().getId());
            msg.writer().writeByte(itemEquipment.getPlusTemplate());
            msg.writer().writeByte(itemEquipment.getLevel());
            msg.writer().writeShort(itemEquipment.getMDurable());
            msg.writer().writeShort(itemEquipment.getDurable());
        }
        player.getSession().sendMessage(msg);
    }

    public void sendItemGem(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.GEM_ITEM);
        msg.writer().writeShort(player.getInventory().getItemGem().size());
        for (short i = 0; i < player.getInventory().getItemGem().size(); i++) {
            ItemGem itemGem = player.getInventory().getItemGem().get(i);
            msg.writer().writeShort(itemGem.getIdItem());
            msg.writer().writeShort(itemGem.getTemplate().getId());
            msg.writer().writeShort(itemGem.getQuantity());
        }
        msg.writer().writeByte(0);
        player.getSession().sendMessage(msg);
    }

    public void sendItemGemLock(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.GEM_ITEM);
        msg.writer().writeShort(player.getInventory().getItemGemLock().size());
        for (short i = 0; i < player.getInventory().getItemGemLock().size(); i++) {
            ItemGem itemGem = player.getInventory().getItemGemLock().get(i);
            msg.writer().writeShort(itemGem.getIdItem());
            msg.writer().writeShort(itemGem.getTemplate().getId());
            msg.writer().writeShort(itemGem.getQuantity());
        }
        msg.writer().writeByte(1);
        player.getSession().sendMessage(msg);
    }

    public void sendItemSpecial(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.SPECIAL_ITEM);
        msg.writer().writeByte(0);
        player.getSession().sendMessage(msg);
    }

    public void sendItemPotion(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_INVENTORY);
        msg.writer().writeByte(0);
        msg.writer().writeLong(player.getInventory().getXu());
        msg.writer().writeByte(player.getInventory().getItemPotion().size());
        for (byte i = 0; i < player.getInventory().getItemPotion().size(); i++) {
            ItemPotion itemPotion = player.getInventory().getItemPotion().get(i);
            msg.writer().writeByte(itemPotion.getTemplate().getId());
            msg.writer().writeInt(itemPotion.getQuantity());
        }
        msg.writer().writeInt(player.getInventory().getLuong());
        msg.writer().writeInt(player.getInventory().getLuongKhoa());
        player.getSession().sendMessage(msg);
    }

    public void sendItemSold(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.GET_DEPOSITE_ITEM);
        msg.writer().writeByte(1);
        msg.writer().writeByte(NpcConst.THAY_NGU_HANH);
        msg.writer().writeByte(0);
        msg.writer().writeByte(player.getInventory().getItemSold().size());
        msg.writer().writeShort(player.getIdPlayer());
        for (int i = 0; i < player.getInventory().getItemSold().size(); i++) {
            ItemEquip itemSold = player.getInventory().getItemSold().get(i);
            msg.writer().writeByte(itemSold.getClassChar());
            msg.writer().writeShort(itemSold.getIdItem());
            msg.writer().writeShort(itemSold.getTemplate().getId());
            msg.writer().writeByte(itemSold.getPlusTemplate());
            msg.writer().writeInt(itemSold.getTemplate().getPrice() / 5 * 2);
            msg.writer().writeByte(itemSold.getLevel());
            msg.writer().writeShort(itemSold.getDurable());
            msg.writer().writeByte(0); // num kham
            msg.writer().writeByte(0); // total kham
            msg.writer().writeByte(itemSold.getItemAttributes().size());
            for (int j = 0; j < itemSold.getItemAttributes().size(); j++) {
                Attribute att = itemSold.getItemAttributes().get(j);
                msg.writer().writeByte(att.getTemplate().getId());
                msg.writer().writeShort(att.getValue());
            }
            msg.writer().writeByte(itemSold.getColorName());
            msg.writer().writeByte(itemSold.getHe()); // he item
            msg.writer().writeByte(0); // bekicked
            msg.writer().writeByte(itemSold.getRank());
            msg.writer().writeByte(itemSold.getDamageType());
            msg.writer().writeByte(itemSold.isLock() ? 1 : 0);
            msg.writer().writeUTF(itemSold.getNameCharSeal());
        }
        msg.writer().writeByte(0);
        player.getSession().sendMessage(msg);
    }

    public void sendItemBag(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_INVENTORY);
        msg.writer().writeByte(1);
        msg.writer().writeShort(player.getInventory().getItemBag().size());
        for (short i = 0; i < player.getInventory().getItemBag().size(); i++) {
            ItemEquip itemEquipment = player.getInventory().getItemBag().get(i);
            msg.writer().writeByte(player.getInfo().getClassPlayer());
            msg.writer().writeShort(itemEquipment.getIdItem());
            msg.writer().writeShort(itemEquipment.getTemplate().getId());
            msg.writer().writeByte(itemEquipment.getPlusTemplate());
            msg.writer().writeByte(itemEquipment.getLevel());
            msg.writer().writeShort(itemEquipment.getMDurable());
            msg.writer().writeShort(itemEquipment.getDurable());
            msg.writer().writeByte(itemEquipment.getClassChar());
            msg.writer().writeByte(0); // num kham
            msg.writer().writeByte(0); // total kham
            msg.writer().writeByte(itemEquipment.getColorName());
            msg.writer().writeByte(itemEquipment.getHe());
            msg.writer().writeByte(0); // bekicked
            msg.writer().writeByte(itemEquipment.getRank());
            msg.writer().writeByte(itemEquipment.getDamageType());
            msg.writer().writeByte(itemEquipment.isLock() ? 1 : 0);
            msg.writer().writeUTF(itemEquipment.getNameCharSeal());
            msg.writer().writeShort(itemEquipment.getDayUse());
            msg.writer().writeByte(itemEquipment.getItemAttributes().size());
            for (int j = 0; j < itemEquipment.getItemAttributes().size(); j++) {
                Attribute att = itemEquipment.getItemAttributes().get(j);
                msg.writer().writeByte(att.getTemplate().getId());
                msg.writer().writeShort(att.getValue());
            }
            msg.writer().writeByte(1); // can drop
        }
        msg.writer().writeInt(player.getInventory().getLuong());
        msg.writer().writeInt(player.getInventory().getLuongKhoa());
        player.getSession().sendMessage(msg);
    }

    public void sendItemQuest(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CHAR_INVENTORY);
        msg.writer().writeByte(0);
        msg.writer().writeByte(player.getInventory().getItemQuest().size());
        for (int i = 0; i < player.getInventory().getItemQuest().size(); i++) {
            ItemQuest itemQuest = player.getInventory().getItemQuest().get(i);
            msg.writer().writeShort(itemQuest.getQuantity());
        }
        msg.writer().writeInt(player.getInventory().getLuong());
        msg.writer().writeInt(player.getInventory().getLuongKhoa());
        player.getSession().sendMessage(msg);
    }

    private void sendSuccessRepairItem(@NonNull Player player) {
        Message msg = new Message(CommandMessage.REPAIR_ITEM);
        player.getSession().sendMessage(msg);
    }

    private void initIdItemEquip(@NonNull Player player, @NonNull ItemEquip itemEquipment) {
        switch (itemEquipment.getIdItem()) {
            case 0 -> {
                short idMax = player.getInventory().getMaxIdItem();
                itemEquipment.setIdItem((short) (idMax + 1));
                player.getInventory().setMaxIdItem((short) (idMax + 1));
            }
            case -1 -> {
                itemEquipment.setIdItem((short) 1);
                player.getInventory().setMaxIdItem((short) 1);
            }
            case 32766 ->
                player.getInventory().initIdItem();
        }
    }

    private void initIdItemGem(@NonNull Player player, @NonNull ItemGem itemGem) {
        switch (itemGem.getIdItem()) {
            case 0 -> {
                short idMax = player.getInventory().getMaxIdItem();
                itemGem.setIdItem((short) (idMax + 1));
                player.getInventory().setMaxIdItem((short) (idMax + 1));
            }
            case -1 -> {
                itemGem.setIdItem((short) 1);
                player.getInventory().setMaxIdItem((short) 1);
            }
            case 32766 ->
                player.getInventory().initIdItem();
        }
    }
}
