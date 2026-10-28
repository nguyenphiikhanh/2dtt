package services;

import consts.Const;
import deposite.Deposite;
import deposite.DepositeItemEquip;
import deposite.DepositeItemGem;
import item.ItemEquip;
import item.ItemGem;
import item.ItemPotion;
import java.io.IOException;
import lombok.NonNull;
import manager.Manager;
import network.Message;
import player.Player;
import utils.CommandMessage;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class TextBoxService {

    public static final TextBoxService instance = new TextBoxService();
    private static final byte INPUT_TYPE_ANY = 0;
    private static final byte INPUT_TYPE_NUMERIC = 1;
    private static final byte INPUT_TYPE_PASSWORD = 2;
    private static final byte INPUT_ALPHA_NUMBER_ONLY = 3;

    private static final byte CHAT_WORLD = 0;
    private static final byte PLAYER_CAN_BUY = 1;

    public void onTextBox(@NonNull Player player, byte type, String text) throws IOException {
        switch (type) {
            case CHAT_WORLD -> {
                ItemPotion loa = InventoryService.instance.findItemPotion(player, (short) 100);
                if (loa == null) {
                    return;
                }
                ChatService.instance.sendChatWorld(player, text);
                InventoryService.instance.minusQuantityItemPotion(player, loa, (short) 1);
                InventoryService.instance.sendItemPotion(player);
            }
            case PLAYER_CAN_BUY -> {
                if (Util.isNullOrEmpty(text)) {
                    return;
                }
                short idItem = player.getSundry().getItemIdDeposite();
                byte category = player.getSundry().getCategoryDeposite();
                int price = player.getSundry().getPriceDeposite();
                String depositeName = String.format("%s_%s", player.getSundry().getNpcTypeDeposite(), player.getSundry().getIndexShopDeposite());
                Deposite deposite = Manager.getDeposite(depositeName);
                if (deposite == null) {
                    return;
                }
                switch (category) {
                    case Const.CATEGORY_ITEM -> {
                        ItemEquip item = InventoryService.instance.findItemBag(player, idItem);
                        if (item == null || player.getSundry().containsDepositeItemEquip(item)) {
                            return;
                        }
                        deposite.addPlayer(player);
                        DepositeItemEquip depositeItemEquip = new DepositeItemEquip(item, depositeName, price, text);
                        player.getSundry().addDepositeItemEquip(depositeItemEquip);
                        DepositeService.instance.onPlayerSellItemEquip(player, item, price);
                    }
                    case Const.CATEGORY_GEM_ITEM -> {
                        ItemGem item = InventoryService.instance.findItemGemByItemId(player, idItem);
                        if (item == null || item.getQuantity() <= player.getSundry().countItemGemDeposite(idItem)) {
                            return;
                        }
                        deposite.addPlayer(player);
                        DepositeItemGem depositeItemGem = new DepositeItemGem((short) 0, item, depositeName, price, text);
                        player.getSundry().addDepositeItemGem(depositeItemGem);
                        depositeItemGem.setIdReal((short) (idItem + player.getSundry().indexOfGem(depositeItemGem)));
                        DepositeService.instance.onPlayerSellItemGem(player, item, price);
                        DepositeService.instance.sendItemDeposite(player, player, depositeName);
                    }
                }
            }
        }
    }

    public void sendPlayerCanBuy(@NonNull Player player) throws IOException {
        sendTextBox(player, PLAYER_CAN_BUY, "Tên Người Mua", INPUT_TYPE_ANY);
    }

    public void sendChatWorld(@NonNull Player player) throws IOException {
        sendTextBox(player, CHAT_WORLD, "Nhập nội dung", INPUT_TYPE_ANY);
    }

    private void sendTextBox(@NonNull Player player, byte idMenu, String name, byte typeInput) throws IOException {
        Message msg = new Message(CommandMessage.TEXT_BOX);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(idMenu);
        msg.writer().writeUTF(name);
        msg.writer().writeByte(typeInput);
        player.getSession().sendMessage(msg);
    }
}
