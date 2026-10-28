package services;

import item.ItemEquip;
import item.ItemBuyNpc;
import item.ItemGem;
import item.ItemPotion;
import java.io.IOException;
import lombok.NonNull;
import manager.Manager;
import network.Message;
import player.Player;
import shop.NpcShop;
import template.GemTemplate;
import template.ShopTemplate;
import utils.CommandMessage;
import consts.Const;
import consts.NpcConst;
import deposite.Deposite;
import deposite.DepositeItemEquip;
import deposite.DepositeItemGem;
import manager.ClientManager;
import template.ItemEquipTemplate;
import template.PotionTemplate;
import utils.Printer;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ShopService {

    public static final ShopService instance = new ShopService();

    public void buyItemSpecial(@NonNull Player player, short idItem) throws IOException {
        if (player.getSundry().getTrader() != null) {
            return;
        }
        ShopTemplate shopTemplate = Manager.getShopTemplate(idItem);
        if (shopTemplate == null || !shopTemplate.isSell()) {
            return;
        }
        if (player.getInventory().isFullInventory()) {
            return;
        }
        int price = shopTemplate.getPrice();
        if (shopTemplate.getTypeMoney() == 1) {
            if (!player.getInventory().minusXu(price)) {
                Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(price)));
                return;
            }
        } else {
            if (!player.getInventory().minusLuong(price)) {
                Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(price)));
                return;
            }
        }
        if (shopTemplate.getIdItemEquip() != -1) {
            ItemEquip itemEquipment = ItemService.instance.createNewItemEquipment(shopTemplate.getIdItemEquip(), player.getInfo().getClassPlayer());
            InventoryService.instance.addItemBagEquipment(player, itemEquipment);
            InventoryService.instance.sendItemBag(player);
        } else if (shopTemplate.getIdItemPotion() != -1) {
            ItemPotion potion = ItemService.instance.createNewItemPotion(shopTemplate.getIdItemPotion(), shopTemplate.getValue());
            InventoryService.instance.addItemPotion(player, potion);
            InventoryService.instance.sendItemPotion(player);
        } else if (shopTemplate.getIdItemGem() != -1) {
            ItemGem itemGem = ItemService.instance.createNewItemGem(shopTemplate.getIdItemGem(), shopTemplate.getValue());
            if (shopTemplate.isGemLock()) {
                InventoryService.instance.addItemGemLock(player, itemGem);
                InventoryService.instance.sendItemGemLock(player);
            } else {
                InventoryService.instance.addItemGem(player, itemGem);
                InventoryService.instance.sendItemGem(player);
            }
        } else {
            switch (shopTemplate.getId()) {
                case 153, 154, 155, 156, 157, 158, 159 -> {
                    String message = player.getInfo().getGender() == Const.MALE ? "Chỉ phù hợp cho nữ" : "";
                    if (!message.isEmpty()) {
                        if (shopTemplate.getTypeMoney() == 1) {
                            player.getInventory().plusXu(price);
                        } else {
                            player.getInventory().plusLuong(price);
                        }
                        Service.instance.sendLogOut(player.getSession(), message);
                        InventoryService.instance.sendItemPotion(player);
                        return;
                    }
                    player.getInfo().setHead((byte) shopTemplate.getValue());
                    Service.instance.sendMainCharInfo(player);
                    MapService.instance.sendInfoMe(player);
                }
                case 160, 161, 162, 163 -> {
                    String message = player.getInfo().getGender() == Const.FEMALE ? "Chỉ phù hợp cho nam" : "";
                    if (!message.isEmpty()) {
                        if (shopTemplate.getTypeMoney() == 1) {
                            player.getInventory().plusXu(price);
                        } else {
                            player.getInventory().plusLuong(price);
                        }
                        Service.instance.sendLogOut(player.getSession(), message);
                        InventoryService.instance.sendItemPotion(player);
                        return;
                    }
                    player.getInfo().setHead((byte) shopTemplate.getValue());
                    Service.instance.sendMainCharInfo(player);
                    MapService.instance.sendInfoMe(player);
                }
            }
        }
        InventoryService.instance.sendItemPotion(player);
        sendSuccessBuyItemShop(player);
    }

    public void buyItemNpcShop(@NonNull Player player, byte typeDamage) throws IOException {
        if (player.getSundry().getTrader() != null) {
            return;
        }
        if (!player.getSundry().getItemNpcShop().isEmpty()) {
            for (int i = 0; i < player.getSundry().getItemNpcShop().size(); i++) {
                ItemBuyNpc item = player.getSundry().getItemNpcShop().get(i);
                if (item != null) {
                    if (player.getInventory().isFullInventory()) {
                        Service.instance.sendLogOut(player.getSession(), "Hành trang đã đầy");
                        return;
                    }
                    if (item.getCategory() == Const.CATEGORY_ITEM) {
                        ItemEquipTemplate template = Manager.getItemEquipment(item.getIdItem());
                        if (!player.getInventory().minusXu(template.getPrice())) {
                            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(template.getPrice())));
                            return;
                        }
                        if (template.getNdayLoan() != 0 && !player.getInventory().minusLuong(template.getPrice())) {
                            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(template.getPrice())));
                            return;
                        }
                        ItemEquip itemEquipment = ItemService.instance.createNewItemEquipment(item.getIdItem(), item.getClassChar(), typeDamage);
                        if (template.getNdayLoan() != 0) {
                            itemEquipment.setDayUse(template.getNdayLoan());
                        }
                        InventoryService.instance.addItemBagEquipment(player, itemEquipment);
                    }
                    if (item.getCategory() == Const.CATEGORY_GEM_ITEM) {
                        GemTemplate gemTemplate = Manager.getGemTemplate(item.getIdItem());
                        if (!gemTemplate.isSell()) {
                            continue;
                        }
                        if (gemTemplate.getTypeMoney() == Const.XU) {
                            if (!player.getInventory().minusXu(gemTemplate.getPrice())) {
                                Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(gemTemplate.getPrice())));
                                return;
                            }
                        } else if (gemTemplate.getTypeMoney() == Const.LUONG) {
                            if (!player.getInventory().minusLuong(gemTemplate.getPrice())) {
                                Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s lượng", Util.formatNumber(gemTemplate.getPrice())));
                                return;
                            }
                        }
                        ItemGem itemGem = ItemService.instance.createNewItemGem(item.getIdItem(), item.getQuantity());
                        InventoryService.instance.addItemGem(player, itemGem);
                    } else if (item.getCategory() == Const.CATEGORY_POTION) {
                        int money = Manager.getPotionTemplate(item.getIdItem()).getPrice();
                        if (!player.getInventory().minusXu(money * item.getQuantity())) {
                            return;
                        }
                        ItemPotion potion = ItemService.instance.createNewItemPotion(item.getIdItem(), item.getQuantity());
                        InventoryService.instance.addItemPotion(player, potion);
                    }
                }
            }
            Service.instance.sendEndDialog(player);
            player.getSundry().getItemNpcShop().clear();
            InventoryService.instance.sendItemBag(player);
            InventoryService.instance.sendItemPotion(player);
            InventoryService.instance.sendItemGem(player);
        } else {
            Service.instance.sendEndDialog(player);
        }
    }

    public void buyItemDeposite(@NonNull Player player, byte npcId, byte indexShop, short idPlayer, short idItem, byte type) throws IOException {
        switch (npcId) {
            case NpcConst.THAY_NGU_HANH -> {
                if (idPlayer != player.getIdPlayer() || type != 0) {
                    return;
                }
                ItemEquip itemSold = InventoryService.instance.findItemSold(player, idItem);
                if (itemSold == null) {
                    return;
                }
                int price = itemSold.getTemplate().getPrice() / 5 * 2;
                if (player.getInventory().isFullInventory()) {
                    Service.instance.sendLogOut(player.getSession(), "Hành trang không đủ ô trống");
                    return;
                }
                if (!player.getInventory().minusXu(price)) {
                    Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(price)));
                    return;
                }
                InventoryService.instance.sendItemPotion(player);
                InventoryService.instance.removeItemSoldEquipment(player, itemSold);
                InventoryService.instance.addItemBagEquipment(player, itemSold);
                InventoryService.instance.sendItemBag(player);
                sendSuccessBuyItemDeposite(player, idPlayer, idItem, type);
                Service.instance.sendLogOut(player.getSession(), String.format("Đã mua %s với giá %s xu", itemSold.getTemplate().getName(), Util.formatNumber(price)));
            }
            case NpcConst.NHAT_GIAP, NpcConst.NHI_GIAP, NpcConst.TAM_GIAP, NpcConst.TU_GIAP, NpcConst.NGU_GIAP, NpcConst.NHAT_NGUU -> {
                if (player.getIdPlayer() == idPlayer) {
                    return;
                }
                String depositeName = String.format("%s_%s", player.getSundry().getNpcTypeDeposite(), player.getSundry().getIndexShopDeposite());
                Deposite deposite = Manager.getDeposite(depositeName);
                if (deposite == null) {
                    return;
                }
                Player playerSell = deposite.getPlayer(idPlayer);
                if (playerSell == null || !ClientManager.containsPlayers(player)) {
                    Service.instance.sendLogOut(player.getSession(), "Người chơi hiện không hoạt động");
                    return;
                }
                switch (type) {
                    case DepositeService.EQUIP -> {
                        DepositeItemEquip item = playerSell.getSundry().findItemEquipDeposite(idItem);
                        if (item == null) {
                            Service.instance.sendLogOut(player.getSession(), "Vật phẩm đã có người mua hoặc không còn trong hành trang");
                            sendSuccessBuyItemDeposite(player, playerSell.getIdPlayer(), idItem, DepositeService.EQUIP);
                            return;
                        }
                        ItemEquip itemEquip = InventoryService.instance.findItemBag(playerSell, idItem);
                        if (itemEquip == null) {
                            Service.instance.sendLogOut(player.getSession(), "Vật phẩm đã có người mua hoặc không còn trong hành trang");
                            sendSuccessBuyItemDeposite(player, playerSell.getIdPlayer(), idItem, DepositeService.EQUIP);
                            return;
                        }
                        if (!item.getPlayerCanBuy().equals(player.getName())) {
                            Service.instance.sendLogOut(player.getSession(), String.format("Vật phẩm này chỉ bán cho %s", item.getPlayerCanBuy()));
                            return;
                        }
                        if (player.getInventory().isFullInventory()) {
                            Service.instance.sendLogOut(player.getSession(), "Hành trang không đủ ô trống");
                            return;
                        }
                        int price = item.getPriceDeposite();
                        if (!player.getInventory().minusXu(price)) {
                            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(price)));
                            return;
                        }
                        InventoryService.instance.removeItemBagEquipment(playerSell, itemEquip);
                        ItemEquip equipNew = ItemService.instance.createNewItemEquipment(itemEquip);
                        equipNew.setNameCharSeal(playerSell.getName());
                        InventoryService.instance.addItemBagEquipment(player, equipNew);
                        itemEquip.dispose();
                        InventoryService.instance.sendItemBag(player);
                        InventoryService.instance.sendItemBag(playerSell);
                        InventoryService.instance.sendItemPotion(player);
                        sendSuccessBuyItemDeposite(player, player.getIdPlayer(), idItem, DepositeService.EQUIP);
                        sendSuccessBuyItemDeposite(playerSell, player.getIdPlayer(), idItem, DepositeService.EQUIP);
                    }
                    case DepositeService.GEM -> {
                        DepositeItemGem item = playerSell.getSundry().findItemGemDeposite(idItem);
                        if (item == null) {
                            Service.instance.sendLogOut(player.getSession(), "Vật phẩm đã có người mua hoặc không còn trong hành trang");
                            sendSuccessBuyItemDeposite(player, playerSell.getIdPlayer(), idItem, DepositeService.GEM);
                            return;
                        }
                        ItemGem itemGem = InventoryService.instance.findItemGemByItemId(playerSell, item.getItem().getIdItem());
                        if (itemGem == null) {
                            Service.instance.sendLogOut(player.getSession(), "Vật phẩm đã có người mua hoặc không còn trong hành trang");
                            sendSuccessBuyItemDeposite(player, playerSell.getIdPlayer(), idItem, DepositeService.GEM);
                            return;
                        }
                        if (!item.getPlayerCanBuy().equals(player.getName())) {
                            Service.instance.sendLogOut(player.getSession(), String.format("Vật phẩm này chỉ bán cho %s", item.getPlayerCanBuy()));
                            return;
                        }
                        if (player.getInventory().isFullInventory()) {
                            Service.instance.sendLogOut(player.getSession(), "Hành trang không đủ ô trống");
                            return;
                        }
                        int price = item.getPriceDeposite();
                        if (!player.getInventory().minusXu(price)) {
                            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(price)));
                            return;
                        }
                        InventoryService.instance.minusQuantityItemGem(player, itemGem, (short) 1);
                        InventoryService.instance.addItemGem(player, ItemService.instance.createNewItemGem(itemGem.getTemplate().getId(), (short) 1));
                        InventoryService.instance.sendItemGem(player);
                        InventoryService.instance.sendItemGem(playerSell);
                        InventoryService.instance.sendItemPotion(player);
                        sendSuccessBuyItemDeposite(player, player.getIdPlayer(), idItem, DepositeService.GEM);
                        sendSuccessBuyItemDeposite(playerSell, player.getIdPlayer(), idItem, DepositeService.GEM);
                    }
                }
            }
            default ->
                Printer.printRed(String.format("Buy Item Deposite Not Found Npc %s", npcId));
        }
    }

    public void buyTicket(@NonNull Player player) throws IOException {
        if (player.getInventory().isFullInventory()) {
            Service.instance.sendLogOut(player.getSession(), "Hành trang không đủ ô trống");
            return;
        }
        PotionTemplate veTau = Manager.getPotionTemplate((short) 20);
        if (!player.getInventory().minusXu(veTau.getPrice())) {
            Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(veTau.getPrice())));
            return;
        }
        ItemPotion item = ItemService.instance.createNewItemPotion((short) 20, 1);
        InventoryService.instance.addItemPotion(player, item);
        InventoryService.instance.sendItemPotion(player);
        sendSuccessBuyTicket(player);
    }

    public void onSellItem(@NonNull Player player, short indexItem) throws IOException {
        if (player.getSundry().getTrader() != null) {
            return;
        }
        ItemEquip item = InventoryService.instance.findItemBag(player, indexItem);
        if (item.getTemplate().getNdayLoan() != 0 || item.getTemplate().getAttribute()[Const.ATTRIBUTE_CLOTH] == 1) {
            InventoryService.instance.sendItemBag(player);
            return;
        }
        InventoryService.instance.removeItemBagEquipment(player, item);
        InventoryService.instance.addItemSoldEquipment(player, item);
        int price = item.getTemplate().getPrice() / 5;
        player.getInventory().plusXu(price);
        sendSuccessSellItem(player, indexItem);
        InventoryService.instance.sendItemBag(player);
    }

    private void sendSuccessBuyItemShop(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.BUY_ITEM_SPECIAL);
        player.getSession().sendMessage(msg);
    }

    private void sendSuccessSellItem(@NonNull Player player, short indexItem) throws IOException {
        Message msg = new Message(CommandMessage.SELL_ITEM);
        msg.writer().writeShort(indexItem);
        player.getSession().sendMessage(msg);
    }

    private void sendSuccessBuyTicket(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.BUY_TICKET);
        player.getSession().sendMessage(msg);
    }

    public void openNpcShop(@NonNull Player player, String name, byte typeDamage) throws IOException {
        Message msg = new Message(CommandMessage.NPC_INFO);
        NpcShop npcShop = Manager.getItemsNpcShop(name);
        if (npcShop == null) {
            return;
        }
        msg.writer().writeByte(npcShop.getTypeShop());
        switch (npcShop.getTypeShop()) {
            case Const.SHOP_POTION -> {
                msg.writer().writeByte(npcShop.getIdItems().length);
                for (int i = 0; i < npcShop.getIdItems().length; i++) {
                    msg.writer().writeByte(npcShop.getIdItems()[i]);
                }
            }
            case Const.SHOP_EQUIPMENT -> {
                msg.writer().writeByte(player.getSundry().getSelected());
                msg.writer().writeShort(npcShop.getIdItems().length);
                for (int i = 0; i < npcShop.getIdItems().length; i++) {
                    msg.writer().writeShort(npcShop.getIdItems()[i]);
                }
                msg.writer().writeByte(typeDamage);
            }
        }
        player.getSession().sendMessage(msg);
    }

    private void sendSuccessBuyItemDeposite(@NonNull Player pl, short idChar, short idItem, byte type) throws IOException {
        Message msg = new Message(CommandMessage.BUY_DEPOSITE_ITEM);
        msg.writer().writeShort(idItem);
        msg.writer().writeShort(idChar);
        msg.writer().writeByte(type);
        pl.getSession().sendMessage(msg);
    }
}
