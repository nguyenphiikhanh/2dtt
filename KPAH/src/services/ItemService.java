package services;

import item.Attribute;
import item.ItemEquip;
import item.ItemFriend;
import item.ItemGem;
import item.ItemMap;
import item.ItemPotion;
import item.ItemQuest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import lombok.NonNull;
import manager.Manager;
import map.Zone;
import player.Player;
import network.Message;
import template.GemTemplate;
import template.AttributeEquipTemplate;
import template.ItemEquipTemplate;
import template.PotionTemplate;
import template.ShopTemplate;
import utils.CommandMessage;
import consts.Const;
import consts.ItemEquipConst;
import item.ItemAnimal;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ItemService {

    public static final ItemService instance = new ItemService();

    public ItemEquip createNewItemEquipment(short id, byte classChar, byte... typeDamages) {
        ItemEquipTemplate template = Manager.getItemEquipment(id);
        if (template.getClassChar() != -1) {
            classChar = template.getClassChar();
        }
        byte typeDamage = (byte) (typeDamages.length <= 0 ? ItemEquipConst.DAMAGE_NONE : typeDamages[0]);
        ItemEquip item = ItemEquip.builder().he(ItemEquipConst.NONE_HE).template(template).colorName(ItemEquipConst.NONE_COLOR).level(template.getLevel()).plusTemplate((byte) 0).classChar(classChar).isLock(false).damageType(typeDamage).durable(template.getDurable()).mDurable(template.getDurable()).viTriVe((byte) 0).rank(ItemEquipConst.NONE_RANK).nameCharSeal("").itemAttributes(new ArrayList<>()).dayUse(0).timeCreateItem(System.currentTimeMillis()).build();
        if (item.getTemplate().getAttribute()[0] > 0) {
            item.getItemAttributes().add(new Attribute(Manager.getAttributeTemplate((short) 0), item.getTemplate().getAttribute()[0]));
        }
        for (short i = 1; i < 7; i++) {
            short value = (short) ((item.getTemplate().getAttribute()[i] + (!item.isAnimalArmor() ? item.getTemplate().getAttribute()[i] * Manager.PERCENT_ATTRIBUTE[classChar] / 100 : 0)));
            if (value > 0 && Manager.ATTRIBUTE_FOR_TYPE[item.getTemplate().getType()][i]) {
                item.getItemAttributes().add(new Attribute(Manager.getAttributeTemplate(i), value));
            }
        }
        item.subDefend(typeDamage);
        return item;
    }

    public ItemEquip createNewItemEquipment(@NonNull ItemMap itemMap) {
        ItemEquip item = this.createNewItemEquipment(itemMap.getItemTemplateID(), (byte) Util.nextInt(Const.KIEM_KHACH, Const.CUNG_THU));
        return item;
    }

    public ItemEquip createNewItemEquipment(@NonNull ItemEquip itemEquip) {
        ItemEquip item = ItemEquip.builder().template(itemEquip.getTemplate()).plusTemplate(itemEquip.getPlusTemplate()).classChar(itemEquip.getClassChar()).isLock(itemEquip.isLock()).durable(itemEquip.getDurable()).mDurable(itemEquip.getMDurable()).level(itemEquip.getLevel()).colorName(itemEquip.getColorName()).viTriVe(itemEquip.getViTriVe()).he(itemEquip.getHe()).rank(itemEquip.getRank()).damageType(itemEquip.getDamageType()).nameCharSeal(itemEquip.getNameCharSeal()).dayUse(itemEquip.getDayUse()).timeCreateItem(itemEquip.getTimeCreateItem()).itemAttributes(new ArrayList<>()).build();
        for (Attribute att : itemEquip.getItemAttributes()) {
            Attribute newAtt = Attribute.builder().template(att.getTemplate()).value(att.getValue()).build();
            item.getItemAttributes().add(newAtt);
        }
        return item;
    }

    public ItemAnimal createNewItemAnimal(short id) {
        ItemAnimal item = ItemAnimal.builder().template(Manager.getAnimalTemplate(id)).attributes(new ArrayList<>()).minutes(-1).level((byte) 1).timeStart(0).build();
        return item;
    }

    public ItemPotion createNewItemPotion(short id, int quantity) {
        ItemPotion item = ItemPotion.builder().template(Manager.getPotionTemplate(id)).quantity(quantity).build();
        return item;
    }

    public ItemGem createNewItemGem(short id, short quantity) {
        ItemGem item = ItemGem.builder().template(Manager.getGemTemplate(id)).quantity(quantity).build();
        return item;
    }

    public ItemPotion createNewItemPotion(@NonNull ItemMap itemMap) {
        ItemPotion item = createNewItemPotion((byte) itemMap.getItemTemplateID(), itemMap.getQuantity());
        return item;
    }

    public ItemQuest createNewItemQuest(byte id, short quantity) {
        ItemQuest item = ItemQuest.builder().template(Manager.getItemQuestTemplate(id)).quantity(quantity).build();
        return item;
    }

    public ItemMap createNewItemMap(short id, short quantity, byte catagory, short x, short y, short playerDrop, Zone zone) {
        ItemMap item = new ItemMap(catagory, id, (short) -1, quantity, x, y, zone, System.currentTimeMillis(), playerDrop);
        return item;
    }

    public ItemFriend createNewItemFriend(ItemEquip item) {
        ItemFriend itemF = ItemFriend.builder().classChar(item.getClassChar()).idTemplate(item.getTemplate().getId()).level(item.getLevel()).plusTemplate(item.getPlusTemplate()).build();
        return itemF;
    }

    public void onRemoveItemMap(@NonNull ItemMap itemMap) throws IOException {
        Message msg = new Message(CommandMessage.REMOVE_ACTOR);
        msg.writer().writeByte(itemMap.getItemCatagory());
        msg.writer().writeShort(itemMap.getItemMapId());
        MapService.instance.sendAllPlayerInMap(itemMap.getZone(), msg);
    }

    public void sendItemInMap(@NonNull Player player, @NonNull ItemMap itemMap) throws IOException {
        Message msg = new Message(CommandMessage.MOVE_CHAR);
        msg.writer().writeByte(itemMap.getItemCatagory());
        msg.writer().writeByte(itemMap.getItemTemplateID());
        msg.writer().writeShort(itemMap.getItemMapId());
        msg.writer().writeShort(itemMap.getX());
        msg.writer().writeShort(itemMap.getY());
        msg.writer().writeByte(-1);
        msg.writer().writeInt(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeBoolean(true);
        player.getSession().sendMessage(msg);
    }

    public void removeItemGemFromGround(@NonNull Player playerPick, @NonNull ItemMap itemMap) throws IOException {
        Message msg = new Message(CommandMessage.GET_GEM_FROM_GROUND);
        msg.writer().writeShort(playerPick.getIdPlayer());
        msg.writer().writeByte(itemMap.getItemCatagory());
        msg.writer().writeShort(itemMap.getItemMapId());
        playerPick.getLocation().getZone().removeItem(itemMap);
        MapService.instance.sendAllPlayerInMap(playerPick, msg);
    }

    public void removeItemPotionFromGround(@NonNull Player playerPick, @NonNull ItemMap itemMap) throws IOException {
        Message msg = new Message(CommandMessage.GET_POTION_FROM_GROUND);
        msg.writer().writeShort(playerPick.getIdPlayer());
        msg.writer().writeShort(itemMap.getItemMapId());
        msg.writer().writeByte(itemMap.getItemTemplateID());
        msg.writer().writeShort(itemMap.getQuantity());
        playerPick.getLocation().getZone().removeItem(itemMap);
        MapService.instance.sendAllPlayerInMap(playerPick, msg);
    }

    public void removeItemEquipmentFromGround(@NonNull Player playerPick, @NonNull ItemMap itemMap, @NonNull ItemEquip equipmentPick) throws IOException {
        Message msg = new Message(CommandMessage.GET_ITEM_FROM_GROUND);
        msg.writer().writeShort(playerPick.getIdPlayer());
        msg.writer().writeByte(equipmentPick.getClassChar());
        msg.writer().writeShort(itemMap.getItemMapId());
        msg.writer().writeShort(equipmentPick.getIdItem());
        msg.writer().writeShort(equipmentPick.getTemplate().getId());
        msg.writer().writeByte(equipmentPick.getPlusTemplate());
        msg.writer().writeByte(equipmentPick.getTemplate().getLevel());
        msg.writer().writeShort(equipmentPick.getDurable());
        msg.writer().writeShort(equipmentPick.getTemplate().getDurable());
        playerPick.getLocation().getZone().removeItem(itemMap);
        MapService.instance.sendAllPlayerInMap(playerPick, msg);
    }

    public void sendItemInfo(@NonNull Player player, short index, short idTemp) throws IOException {
        ItemEquip item = idTemp == player.getIdPlayer() ? InventoryService.instance.findItemBag(player, index) : InventoryService.instance.findItemBody(player, index, idTemp);
        if (item == null) {
            return;
        }
        Message msg = new Message(CommandMessage.ITEM_INFO);
        msg.writer().writeShort(item.getIdItem());
        msg.writer().writeShort(item.getDurable());
        msg.writer().writeByte(item.getClassChar());
        msg.writer().writeShort(item.getDayUse());
        msg.writer().writeByte(item.getItemAttributes().size());
        for (int i = 0; i < item.getItemAttributes().size(); i++) {
            Attribute att = item.getItemAttributes().get(i);
            msg.writer().writeByte(att.getTemplate().getId());
            msg.writer().writeShort(att.getValue());
        }
        msg.writer().writeByte(item.getPlusTemplate());
        msg.writer().writeByte(item.isLock() ? 1 : 0);
        player.getSession().sendMessage(msg);
    }

    public void sendItemTemplate(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.ITEM_TEMPLATE);
        msg.writer().writeByte(Manager.ITEM_ATTRIBUTE_TEMPLATES.size());
        Enumeration<Short> keysByte = Manager.ITEM_ATTRIBUTE_TEMPLATES.keys();
        while (keysByte.hasMoreElements()) {
            short key = keysByte.nextElement();
            AttributeEquipTemplate attributeTemplate = Manager.getAttributeTemplate(key);
            msg.writer().writeByte(attributeTemplate.getId());
            msg.writer().writeUTF(attributeTemplate.getName());
            msg.writer().writeByte(attributeTemplate.getIsPercent());
            msg.writer().writeByte(attributeTemplate.getColorPaint());
        }
        msg.writer().writeByte(20);
        for (byte i = 0; i < 20; i++) {
            PotionTemplate potionTemplate = Manager.getPotionTemplate(i);
            msg.writer().writeShort(potionTemplate.getPrice());
            msg.writer().writeShort(potionTemplate.getRecovered());
        }
        for (int i = 0; i < 5; i++) {
            msg.writer().writeByte(Manager.PERCENT_ATTRIBUTE[i]);
        }
        msg.writer().writeShort(Manager.ITEM_EQUIPMENTS.size());
        Enumeration<Short> keys = Manager.ITEM_EQUIPMENTS.keys();
        while (keys.hasMoreElements()) {
            short key = keys.nextElement();
            ItemEquipTemplate itemTemplate = Manager.getItemEquipment(key);
            msg.writer().writeShort(itemTemplate.getId());
            msg.writer().writeUTF(itemTemplate.getName());
            msg.writer().writeByte(itemTemplate.getType());
            msg.writer().writeByte(itemTemplate.getStyle());
            msg.writer().writeByte(itemTemplate.getHe());
            msg.writer().writeByte(itemTemplate.getGender());
            msg.writer().writeByte(itemTemplate.getLevel());
            msg.writer().writeShort(itemTemplate.getDurable());
            for (int j = 0; j < 10; j++) {
                msg.writer().writeShort(itemTemplate.getAttribute()[j]);
            }
            msg.writer().writeInt(itemTemplate.getPrice());
            msg.writer().writeByte(itemTemplate.getClassChar());
            msg.writer().writeByte(itemTemplate.getColorItem());
            msg.writer().writeShort(itemTemplate.getIdIcon());
            msg.writer().writeShort(itemTemplate.getNdayLoan());
        }

        msg.writer().writeShort(Manager.GEM_TEMPLATES.size());
        keys = Manager.GEM_TEMPLATES.keys();
        while (keys.hasMoreElements()) {
            short key = keys.nextElement();
            GemTemplate gemTemplate = Manager.getGemTemplate(key);
            msg.writer().writeShort(gemTemplate.getId());
            msg.writer().writeByte(gemTemplate.getIdImage());
            msg.writer().writeInt(gemTemplate.getPrice());
            msg.writer().writeUTF(gemTemplate.getName());
            msg.writer().writeUTF(gemTemplate.getDecript());
            msg.writer().writeByte(gemTemplate.getType());
            msg.writer().writeBoolean(gemTemplate.isSell());
            msg.writer().writeByte(gemTemplate.getTypeEp());
            msg.writer().writeByte(gemTemplate.getTypeMoney());
        }

        msg.writer().writeByte(Manager.SHOP_TEMPLATES.size());
        keys = Manager.SHOP_TEMPLATES.keys();
        while (keys.hasMoreElements()) {
            short key = keys.nextElement();
            ShopTemplate shopTemplate = Manager.getShopTemplate(key);
            msg.writer().writeByte(shopTemplate.getId());
            msg.writer().writeByte(shopTemplate.getIdImage());
            msg.writer().writeInt(shopTemplate.getPrice());
            msg.writer().writeUTF(shopTemplate.getName());
            msg.writer().writeUTF(shopTemplate.getDecript());
            msg.writer().writeByte(shopTemplate.getTypeMoney());
            msg.writer().writeByte(shopTemplate.getShopType());
            msg.writer().writeBoolean(shopTemplate.isSell());
            msg.writer().writeShort(shopTemplate.getValue());
        }
        pl.getSession().sendMessage(msg);
    }
}
