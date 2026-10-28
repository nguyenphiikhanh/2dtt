package services;

import item.ItemPotion;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.Synchronized;
import network.Message;
import player.Player;
import utils.CommandMessage;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class TradeService {

    public static final TradeService instance = new TradeService();

    private static final byte EXIST_TRADE = -2;
    private static final byte REFUSE_TRADE = -1;
    private static final byte SEND_TRADE = 0;
    private static final byte ACCEPT_TRADE = 1;
    private static final byte ADD_ITEM_TRADE = 2;
    private static final byte CANCEL_TRADE = 3;
    private static final byte FINISH_TRADE = 4;
    private static final byte CONFIRM_TRADE = 5;

    private static final byte ADD_ITEM_EQUIP = 0;
    private static final byte ADD_ITEM_POTION = 1;
    private static final byte REMOVE_ITEM_POTION_TRADE = 2;

    public void doTrade(@NonNull Player player, @NonNull Message msg) throws IOException {
        byte type = msg.reader().readByte();
        switch (type) {
            case SEND_TRADE -> {
                short idPlayerTraded = msg.reader().readShort();
                Player playerTraded = player.getLocation().getZone().findPlayer(idPlayerTraded);
                if (playerTraded == null) {
                    return;
                }
                sendTrade(player, playerTraded);
            }
            case ACCEPT_TRADE -> {
                short idPlayerTrader = msg.reader().readShort();
                Player playerTrader = player.getLocation().getZone().findPlayer(idPlayerTrader);
                if (playerTrader == null) {
                    return;
                }
                acceptTrade(player, playerTrader);
            }
            case ADD_ITEM_TRADE -> {
                if (player.getSundry().getTrader() == null) {
                    return;
                }
                byte typeItem = msg.reader().readByte();
                short indexItem = -1;
                short quantity = 1;
                switch (typeItem) {
                    case ADD_ITEM_EQUIP -> {
                        indexItem = msg.reader().readShort();
                    }
                    case ADD_ITEM_POTION -> {
                        indexItem = msg.reader().readByte();
                        quantity = msg.reader().readShort();
                    }
                    case REMOVE_ITEM_POTION_TRADE -> {
                        indexItem = msg.reader().readByte();
                    }
                }
                addItemTrade(player, typeItem, indexItem, quantity);
            }
            case CANCEL_TRADE -> {
                msg.reader().readShort();
                cancelTrade(player);
            }
            case FINISH_TRADE -> {
                msg.reader().readShort();
                confirmTrade(player);
            }
            case CONFIRM_TRADE -> {
                finishTrade(player);
            }
            case REFUSE_TRADE -> {
                short idPlayerTrader = msg.reader().readShort();
                Player playerTrader = player.getLocation().getZone().findPlayer(idPlayerTrader);
                if (playerTrader == null) {
                    return;
                }
                sendRefuseTrade(playerTrader, player);
            }
        }
    }

    public void finishTrade(@NonNull Player player) throws IOException {
        Player trader = player.getSundry().getTrader();
        if (trader == null) {
            return;
        }
        if (trader.getInventory().isFullInventory() || player.getInventory().isFullInventory()) {
            cancelTrade(player);
            return;
        }
        player.getSundry().setFinishTrade(true);
        if (trader.getSundry().isFinishTrade() && player.getSundry().isFinishTrade()) {
            List<ItemPotion> itemMe = player.getSundry().getItemPotionTrade();
            List<ItemPotion> itemYou = trader.getSundry().getItemPotionTrade();

            for (ItemPotion potion : itemMe) {
                ItemPotion inventory = InventoryService.instance.findItemPotion(trader, potion.getTemplate().getId());
                if (inventory == null) {
                    InventoryService.instance.addItemPotion(trader, potion);
                } else {
                    inventory.plusQuantity(potion.getQuantity());
                }
            }

            for (ItemPotion potion : itemYou) {
                ItemPotion inventory = InventoryService.instance.findItemPotion(player, potion.getTemplate().getId());
                if (inventory == null) {
                    InventoryService.instance.addItemPotion(player, potion);
                } else {
                    inventory.plusQuantity(potion.getQuantity());
                }
            }
            InventoryService.instance.sendItemPotion(player);
            InventoryService.instance.sendItemPotion(trader);
            itemMe.clear();
            itemYou.clear();
            sendFinishTrade(player);
            sendFinishTrade(trader);
            disposeTrade(player);
            disposeTrade(trader);
        }
    }

    public void confirmTrade(@NonNull Player player) throws IOException {
        Player trader = player.getSundry().getTrader();
        if (trader == null) {
            return;
        }
        player.getSundry().setConfirmTrade(true);
        if (trader.getSundry().isConfirmTrade() && player.getSundry().isConfirmTrade()) {
            sendConfirmTrade(player);
            sendConfirmTrade(trader);
        }
    }

    public void cancelTrade(@NonNull Player player) throws IOException {
        Player trader = player.getSundry().getTrader();
        if (trader == null) {
            return;
        }
        disposeTrade(player);
        disposeTrade(trader);
        sendCancelTrade(trader);
    }

    public void addItemTrade(@NonNull Player player, byte typeItem, short indexItem, short quantity) throws IOException {
        if (quantity <= 0) {
            return;
        }
        switch (typeItem) {
            case ADD_ITEM_EQUIP -> {
                Service.instance.sendLogOut(player.getSession(), "Không thể giao dịch vật phẩm này");
            }
            case ADD_ITEM_POTION -> {
                if (player.getSundry().getItemPotionTrade().size() >= 15) {
                    Service.instance.sendLogOut(player.getSession(), "Chỉ có thể trao đổi tối đa 15 vật phẩm");
                    return;
                }
                if (indexItem > Byte.MAX_VALUE || indexItem < 0) {
                    Service.instance.sendLogOut(player.getSession(), "Không thể giao dịch vật phẩm này");
                    return;
                }
                ItemPotion item = InventoryService.instance.findItemPotion(player, (byte) indexItem);
                if (item == null || item.getQuantity() < quantity || !item.getTemplate().isTrade()) {
                    return;
                }
                addItemPotionTrade(player, item, quantity);
            }
            case REMOVE_ITEM_POTION_TRADE -> {
                if (indexItem > Byte.MAX_VALUE || indexItem < 0) {
                    return;
                }
                removeItemPotionTrade(player, (byte) indexItem);
            }
        }
    }

    @Synchronized
    public void addItemPotionTrade(@NonNull Player player, @NonNull ItemPotion itemPotion, short quantity) throws IOException {
        if (player.getSundry().hasItemPotionTrade(itemPotion.getTemplate().getId())) {
            return;
        }
        InventoryService.instance.minusQuantityItemPotion(player, itemPotion, quantity);
        ItemPotion potionTrade = ItemService.instance.createNewItemPotion(itemPotion.getTemplate().getId(), quantity);
        player.getSundry().getItemPotionTrade().add(potionTrade);
        sendAddItemTrade(player, player.getSundry().getTrader(), potionTrade);
        sendAddItemTrade(player, player, potionTrade);
    }

    @Synchronized
    public void removeItemPotionTrade(@NonNull Player player, byte id) throws IOException {
        ItemPotion potion = player.getSundry().findItemPotion(id);
        if (potion == null) {
            return;
        }
        player.getSundry().getItemPotionTrade().remove(potion);
        ItemPotion inventory = InventoryService.instance.findItemPotion(player, potion.getTemplate().getId());
        if (inventory == null) {
            InventoryService.instance.addItemPotion(player, potion);
        } else {
            inventory.plusQuantity(potion.getQuantity());
        }
        sendRemoveItemTrade(player, player.getSundry().getTrader(), potion);
        sendRemoveItemTrade(player, player, potion);
    }

    public void acceptTrade(@NonNull Player traded, @NonNull Player trader) throws IOException {
        disposeTrade(traded);
        disposeTrade(trader);
        sendAcceptTrade(traded, trader);
        sendAcceptTrade(trader, traded);
        traded.getSundry().setTrader(trader);
        trader.getSundry().setTrader(traded);
    }

    private void sendCancelTrade(@NonNull Player trade) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        msg.writer().writeByte(CANCEL_TRADE);
        trade.getSession().sendMessage(msg);
    }

    private void sendRemoveItemTrade(@NonNull Player trade, @NonNull Player trader, @NonNull ItemPotion potion) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        msg.writer().writeByte(ADD_ITEM_TRADE);
        msg.writer().writeByte(REMOVE_ITEM_POTION_TRADE);
        msg.writer().writeShort(trade.getIdPlayer());
        msg.writer().writeByte(potion.getTemplate().getId());
        trader.getSession().sendMessage(msg);
    }

    private void sendAddItemTrade(@NonNull Player trade, @NonNull Player trader, @NonNull ItemPotion potion) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        msg.writer().writeByte(ADD_ITEM_TRADE);
        msg.writer().writeByte(ADD_ITEM_POTION);
        msg.writer().writeShort(trade.getIdPlayer());
        msg.writer().writeByte(potion.getTemplate().getId());
        msg.writer().writeShort(potion.getQuantity());
        trader.getSession().sendMessage(msg);
    }

    private void sendFinishTrade(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        msg.writer().writeByte(FINISH_TRADE);
        player.getSession().sendMessage(msg);
    }

    private void sendConfirmTrade(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        msg.writer().writeByte(CONFIRM_TRADE);
        player.getSession().sendMessage(msg);
    }

    private void sendRefuseTrade(@NonNull Player trader, @NonNull Player traded) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        msg.writer().writeByte(REFUSE_TRADE);
        msg.writer().writeShort(traded.getIdPlayer());
        trader.getSession().sendMessage(msg);
    }

    private void sendAcceptTrade(@NonNull Player player, @NonNull Player player2) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        msg.writer().writeByte(ACCEPT_TRADE);
        msg.writer().writeShort(player.getIdPlayer());
        player2.getSession().sendMessage(msg);
    }

    public void sendTrade(@NonNull Player trader, @NonNull Player traded) throws IOException {
        Message msg = new Message(CommandMessage.TRADE);
        if (traded.getSundry().getTrader() != null) {
            msg.writer().writeByte(EXIST_TRADE);
            trader.getSession().sendMessage(msg);
            return;
        }
        msg.writer().writeByte(SEND_TRADE);
        msg.writer().writeShort(trader.getIdPlayer());
        traded.getSession().sendMessage(msg);
    }

    public void disposeTrade(@NonNull Player player) {
        player.getSundry().setTrader(null);
        if (!player.getSundry().getItemPotionTrade().isEmpty()) {
            for (ItemPotion potion : player.getSundry().getItemPotionTrade()) {
                ItemPotion inventory = InventoryService.instance.findItemPotion(player, potion.getTemplate().getId());
                if (inventory == null) {
                    InventoryService.instance.addItemPotion(player, potion);
                } else {
                    inventory.plusQuantity(potion.getQuantity());
                }
            }
        }
        player.getSundry().setConfirmTrade(false);
        player.getSundry().setFinishTrade(false);
        player.getSundry().getItemPotionTrade().clear();
    }
}
