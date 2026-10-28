package services;

import consts.CombineConst;
import item.ItemEquip;
import item.ItemGem;
import java.io.IOException;
import lombok.NonNull;
import network.Message;
import player.Player;
import utils.CommandMessage;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class CombineService {

    public static final CombineService instance = new CombineService();

    public void doTachNguyenLieu(@NonNull Player player, byte type, short indexItem, short idMaterial, byte iLock) throws IOException {
        ItemEquip itemBag = InventoryService.instance.findItemBag(player, indexItem);
        if (itemBag == null || itemBag.getTemplate().getNdayLoan() != 0) {
            return;
        }
        switch (type) {
            case CombineConst.NGUYEN_BOT -> {
                InventoryService.instance.removeItemBagEquipment(player, itemBag);
                itemBag.dispose();
                ItemGem bot = ItemService.instance.createNewItemGem((short) 246, (short) 1);
                sendSuccessNghienBot(player, bot, type);
                InventoryService.instance.addItemGem(player, bot);
                InventoryService.instance.sendItemGem(player);
                InventoryService.instance.sendItemBag(player);
            }
        }
    }

    private void sendSuccessNghienBot(@NonNull Player player, @NonNull ItemGem bot, byte type) throws IOException {
        Message msg = new Message(CommandMessage.TACH_NGUYEN_LIEU);
        msg.writer().writeByte(type);
        if (type == CombineConst.NGUYEN_BOT) {
            msg.writer().writeShort(bot.getTemplate().getIdImage());
            msg.writer().writeByte(1);
            msg.writer().writeByte(1);
        }
        player.getSession().sendMessage(msg);
    }
}
