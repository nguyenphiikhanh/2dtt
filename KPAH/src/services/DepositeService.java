package services;

import consts.Const;
import consts.NpcConst;
import deposite.Deposite;
import deposite.DepositeItemEquip;
import deposite.DepositeItemGem;
import item.Attribute;
import item.ItemEquip;
import item.ItemGem;
import java.io.IOException;
import lombok.NonNull;
import manager.Manager;
import network.Message;
import player.Player;
import utils.CommandMessage;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class DepositeService {

    public static final DepositeService instance = new DepositeService();

    private static final byte SELL = 1;
    private static final byte BUY = 0;

    public static final byte EQUIP = 0;
    public static final byte GEM = 1;

    private static final byte TRANG_SUC = 0;
    private static final byte VU_KHI = 1;
    private static final byte GIAP = 2;

    public void requestSellItem(@NonNull Player player, boolean isSell, byte typeNpc, byte indexShop, short idItem, int price, byte typeItem) throws IOException {
        String nameDeposite = String.format("%s_%s", typeNpc, indexShop);
        Deposite deposite = Manager.getDeposite(nameDeposite);
        if (deposite == null) {
            return;
        }
        if (isSell && deposite.isMaxSeller()) {
            Service.instance.sendLogOut(player.getSession(), "Gian hàng đã hết chỗ đăng bán");
            return;
        }
        if (price < 0) {
            return;
        }
        byte typeItemSell = getTypeCanSell(typeNpc);
        player.getSundry().setPriceDeposite(price);
        player.getSundry().setItemIdDeposite(idItem);
        if (isSell) {
            switch (typeItem) {
                case EQUIP -> {
                    ItemEquip itemEquip = InventoryService.instance.findItemBag(player, idItem);
                    if (itemEquip == null) {
                        return;
                    }
                    if (itemEquip.isLock() || itemEquip.getTemplate().getNdayLoan() != 0) {
                        Service.instance.sendLogOut(player.getSession(), "Vật khẩm không thể bán");
                        return;
                    }
                    if (typeItemSell == VU_KHI && !itemEquip.isWeapon()) {
                        Service.instance.sendLogOut(player.getSession(), "Cửa hàng chỉ bán vũ khí");
                        return;
                    }
                    if (typeItemSell == GIAP && !itemEquip.isArmor()) {
                        Service.instance.sendLogOut(player.getSession(), "Cửa hàng chỉ bán giáp");
                        return;
                    }
                    if (typeItemSell == TRANG_SUC && (!itemEquip.isJewelry() || (itemEquip.getTemplate().getId() >= 720 && itemEquip.getTemplate().getId() <= 722))) {
                        Service.instance.sendLogOut(player.getSession(), "Cửa hàng chỉ bán trang sức, trứng thú cưng, tụ hồn đan, huyết bồ đề, huyết linh thảo, sách kỹ năng pet và các vật phẩm khác: luyện kim dược, ngọc khảm...");
                        return;
                    }
                    player.getSundry().setCategoryDeposite(Const.CATEGORY_ITEM);
                    TextBoxService.instance.sendPlayerCanBuy(player);
                }
                case GEM -> {
                    ItemGem itemGem = InventoryService.instance.findItemGemByItemId(player, idItem);
                    if (itemGem == null) {
                        return;
                    }
                    if (typeItemSell == VU_KHI) {
                        Service.instance.sendLogOut(player.getSession(), "Cửa hàng chỉ bán vũ khí");
                        return;
                    }
                    if (typeItemSell == GIAP) {
                        Service.instance.sendLogOut(player.getSession(), "Cửa hàng chỉ bán giáp");
                        return;
                    }
                    player.getSundry().setCategoryDeposite(Const.CATEGORY_GEM_ITEM);
                    TextBoxService.instance.sendPlayerCanBuy(player);
                }
            }
        } else {
            switch (typeItem) {
                case EQUIP -> {
                    ItemEquip itemEquip = InventoryService.instance.findItemBag(player, idItem);
                    if (!player.getSundry().containsDepositeItemEquip(itemEquip)) {
                        return;
                    }
                    player.getSundry().removeDepositeItemEquip(itemEquip);
                    onPlayerRemoveSellItemEquip(player, itemEquip);
                }
                case GEM -> {
                    DepositeItemGem itemGem = player.getSundry().findItemGemDeposite(idItem);
                    if (!player.getSundry().containsDepositeItemGem(itemGem)) {
                        return;
                    }
                    player.getSundry().removeDepositeItemGem(itemGem);
                    onPlayerRemoveSellItemGem(player, itemGem);
                    if (!player.getSundry().containsDepositeItemGem(itemGem.getItem().getTemplate().getId())) {
                        InventoryService.instance.sendItemGem(player);
                    }
                }
            }
        }
    }

    public void onDepositeItem(@NonNull Player player, byte type, short idPlayer, byte npcType, byte indexShop) throws IOException {
        String nameDeposite = String.format("%s_%s", npcType, indexShop);
        Deposite deposite = Manager.getDeposite(nameDeposite);
        if (deposite == null) {
            return;
        }
        player.getSundry().setIndexShopDeposite(indexShop);
        player.getSundry().setNpcTypeDeposite(npcType);
        switch (type) {
            case SELL -> {
                if (idPlayer == player.getIdPlayer()) {
                    sendItemDeposite(player, player, nameDeposite);
                    return;
                }
                Player playerDeposite = Manager.getDeposite(nameDeposite).getPlayer(idPlayer);
                if (playerDeposite == null) {
                    Service.instance.sendLogOut(player.getSession(), "Người chơi hiện không hoạt động");
                    return;
                }
                sendItemDeposite(player, playerDeposite, nameDeposite);
            }
            case BUY ->
                sendListUser(player, nameDeposite);
        }
    }

    private byte getTypeCanSell(byte idNpc) {
        return switch (idNpc) {
            case NpcConst.NHAT_NGUU ->
                TRANG_SUC;
            case NpcConst.NGU_GIAP, NpcConst.TU_GIAP ->
                VU_KHI;
            default ->
                GIAP;
        };
    }

    public void onPlayerRemoveSellItemGem(@NonNull Player player, @NonNull DepositeItemGem gem) throws IOException {
        Message msg = new Message(CommandMessage.REQUEST_SELL_ITEM);
        msg.writer().writeBoolean(false);
        msg.writer().writeShort(gem.getIdReal());
        msg.writer().writeByte(1);
        msg.writer().writeShort(-1);
        player.getSession().sendMessage(msg);
    }

    public void onPlayerRemoveSellItemEquip(@NonNull Player player, @NonNull ItemEquip equip) throws IOException {
        Message msg = new Message(CommandMessage.REQUEST_SELL_ITEM);
        msg.writer().writeBoolean(false);
        msg.writer().writeShort(equip.getIdItem());
        msg.writer().writeByte(0);
        player.getSession().sendMessage(msg);
    }

    public void onPlayerSellItemEquip(@NonNull Player player, @NonNull ItemEquip equip, int price) throws IOException {
        Message msg = new Message(CommandMessage.REQUEST_SELL_ITEM);
        msg.writer().writeBoolean(true);
        msg.writer().writeShort(equip.getIdItem());
        msg.writer().writeInt(price);
        msg.writer().writeByte(0);
        player.getSession().sendMessage(msg);
    }

    public void onPlayerSellItemGem(@NonNull Player player, @NonNull ItemGem gem, int price) throws IOException {
        Message msg = new Message(CommandMessage.REQUEST_SELL_ITEM);
        msg.writer().writeBoolean(true);
        msg.writer().writeShort(gem.getIdItem());
        msg.writer().writeInt(price);
        msg.writer().writeByte(1);
        msg.writer().writeShort(gem.getTemplate().getId());
        player.getSession().sendMessage(msg);
    }

    public void sendListUser(@NonNull Player pl, String nameDeposite) throws IOException {
        Deposite deposite = Manager.getDeposite(nameDeposite);
        if (deposite == null) {
            return;
        }
        deposite.removePlayerOffline();
        if (deposite.getPlayerSell().size() <= 0) {
            Service.instance.sendLogOut(pl.getSession(), "Hết hàng");
            return;
        }
        Message msg = new Message(CommandMessage.GET_DEPOSITE_ITEM);
        msg.writer().writeByte(BUY);
        msg.writer().writeByte(pl.getSundry().getIdNpcOpen());
        msg.writer().writeByte(pl.getSundry().getIndexShopDeposite());
        msg.writer().writeByte(deposite.getPlayerSell().size());
        for (int i = 0; i < deposite.getPlayerSell().size(); i++) {
            Player playerSell = deposite.getPlayerSell().get(i);
            msg.writer().writeUTF(playerSell.getName());
            msg.writer().writeShort(playerSell.getIdPlayer());
        }
        pl.getSession().sendMessage(msg);
    }

    public void sendItemDeposite(@NonNull Player pl, @NonNull Player playerDeposite, String nameDeposite) throws IOException {
        Message msg = new Message(CommandMessage.GET_DEPOSITE_ITEM);
        msg.writer().writeByte(SELL);
        msg.writer().writeByte(playerDeposite.getSundry().getIdNpcOpen());
        msg.writer().writeByte(playerDeposite.getSundry().getIndexShopDeposite());
        msg.writer().writeByte((int) playerDeposite.getSundry().getDepositeItemEquips().stream().filter(i -> i != null && i.getNameDeposite().equals(nameDeposite)).count());
        msg.writer().writeShort(playerDeposite.getIdPlayer());
        for (int i = 0; i < playerDeposite.getSundry().getDepositeItemEquips().size(); i++) {
            DepositeItemEquip item = playerDeposite.getSundry().getDepositeItemEquips().get(i);
            if (item != null && item.getNameDeposite().equals(nameDeposite)) {
                msg.writer().writeByte(item.getItem().getClassChar());
                msg.writer().writeShort(item.getItem().getIdItem());
                msg.writer().writeShort(item.getItem().getTemplate().getId());
                msg.writer().writeByte(item.getItem().getPlusTemplate());
                msg.writer().writeInt(item.getPriceDeposite());
                msg.writer().writeByte(item.getItem().getLevel());
                msg.writer().writeShort(item.getItem().getDurable());
                msg.writer().writeByte(-1); // num kham
                msg.writer().writeByte(-1); // total kham
                msg.writer().writeByte(item.getItem().getItemAttributes().size());
                for (int j = 0; j < item.getItem().getItemAttributes().size(); j++) {
                    Attribute att = item.getItem().getItemAttributes().get(j);
                    msg.writer().writeByte(att.getTemplate().getId());
                    msg.writer().writeShort(att.getValue());
                }
                msg.writer().writeByte(item.getItem().getColorName());
                msg.writer().writeByte(item.getItem().getHe());
                msg.writer().writeByte(0); // bekicked
                msg.writer().writeByte(item.getItem().getRank());
                msg.writer().writeByte(item.getItem().getDamageType());
                msg.writer().writeByte(item.getItem().isLock() ? 1 : 0);
                msg.writer().writeUTF(item.getItem().getNameCharSeal());
            }
        }
        msg.writer().writeByte((int) playerDeposite.getSundry().getDepositeItemGems().stream().filter(i -> i != null && i.getNameDeposite().equals(nameDeposite)).count());
        for (int i = 0; i < playerDeposite.getSundry().getDepositeItemGems().size(); i++) {
            DepositeItemGem item = playerDeposite.getSundry().getDepositeItemGems().get(i);
            if (item != null && item.getNameDeposite().equals(nameDeposite)) {
                msg.writer().writeShort(item.getIdReal());
                msg.writer().writeShort(item.getItem().getTemplate().getId());
                msg.writer().writeInt(item.getPriceDeposite());
            }
        }
        pl.getSession().sendMessage(msg);
    }

    public void sendGianHang(@NonNull Player pl, byte type) throws IOException {
        Message msg = new Message(CommandMessage.NPC_INFO);
        msg.writer().writeByte(4);
        msg.writer().writeByte(0);
        msg.writer().writeByte(pl.getSundry().getIdNpcOpen());
        msg.writer().writeByte(type);
        pl.getSession().sendMessage(msg);
    }
}
