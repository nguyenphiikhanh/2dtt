package services;

import clan.Clan;
import java.io.IOException;
import lombok.NonNull;
import network.Message;
import player.Player;
import consts.ClanConst;
import utils.CommandMessage;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class PopupService {

    public static final PopupService instance = new PopupService();

    private static final byte OK = 1;
    private static final byte CANCEL = 0;

    private static final byte REVIVAL = -1;
    private static final byte CONFIRM_REVIVAL = 0;
    private static final byte CONFIRM_REGISTER_CLAN = 1;
    private static final byte CONFIRM_QUYEN_GOP = 2;

    public void doAction(@NonNull Player player, byte typePop, byte button) throws IOException {
        if (button == CANCEL) {
            return;
        }
        switch (typePop) {
            case REVIVAL -> {
                if (player.isDie()) {
                    sendPopupOkCancel(player, String.format("Bạn có muốn hồi sinh tại chỗ. Chi phí %s xu", Util.formatNumber(player.getPoint().getXuRevive())), CONFIRM_REVIVAL);
                }
            }
            case CONFIRM_REVIVAL -> {
                if (player.isDie()) {
                    if (player.getInventory().minusXu(player.getPoint().getXuRevive())) {
                        MapService.instance.revivePlayer(player, (byte) 100);
                        InventoryService.instance.sendItemPotion(player);
                    } else {
                        Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s xu", Util.formatNumber(player.getPoint().getXuRevive())));
                    }
                }
            }
            case CONFIRM_QUYEN_GOP -> {
                Clan clan = player.getInfo().getClan();
                if (clan == null) {
                    return;
                }
                if (!clan.minusDedicationPoint(player.getSundry().getXuQuyenGop() * 100)) {
                    Service.instance.sendLogOut(player.getSession(), String.format("Không đủ %s điểm cống hiến", Util.formatNumber(player.getSundry().getXuQuyenGop() * 100)));
                    return;
                }
                player.getInventory().minusXu(player.getSundry().getXuQuyenGop());
                clan.plusXu(player.getSundry().getXuQuyenGop());
                player.getSundry().setXuQuyenGop(0);
                InventoryService.instance.sendItemPotion(player);
            }
            case CONFIRM_REGISTER_CLAN -> {
                ClanService.instance.sendChooseIcon(player);
            }
        }
    }

    public void sendPopupConfirmQuyenGop(@NonNull Player player) throws IOException {
        sendPopupOkCancel(player, String.format("Bạn có muốn dùng %s điểm cống hiến để quyên góp nhanh %s xu vào bang không?", player.getSundry().getXuQuyenGop() * 100, player.getSundry().getXuQuyenGop()), CONFIRM_QUYEN_GOP);
    }

    public void sendPopupConfirmRegClan(@NonNull Player player) throws IOException {
        sendPopupOkCancel(player, String.format("Chi phí tạo bang là %s xu và phải đạt 10 thành viên trong 3 ngày tính từ thời điểm tạo bang", Util.formatNumber(ClanConst.XU_NEED_TO_REGISTER)), CONFIRM_REGISTER_CLAN);
    }

    private void sendPopupOkCancel(@NonNull Player player, String text, byte id) throws IOException {
        Message msg = new Message(CommandMessage.CUSTOM_POPUP);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(id);
        msg.writer().writeUTF(text);
        player.getSession().sendMessage(msg);
    }
}
