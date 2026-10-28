package services;

import clan.Clan;
import clan.ClanMessage;
import item.ItemEquip;
import item.ItemFriend;
import item.ItemPotion;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import manager.ClanManager;
import manager.Manager;
import manager.Settings;
import network.Message;
import player.Friend;
import player.Player;
import consts.ClanConst;
import utils.CommandMessage;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ClanService {

    public static final ClanService instance = new ClanService();

    public void onChat(@NonNull Player player, byte type, String text, int idMessage) throws IOException {
        switch (type) {
            case ClanConst.CHAT -> {
                addChat(player, text);
            }
            case ClanConst.VIEW_CHAT -> {
                viewChat(player);
            }
            case ClanConst.REMOVE_CHAT -> {
                removeChat(player, idMessage);
            }
        }
    }

    public void transMoney(@NonNull Player player, int money) throws IOException {
        Clan clan = player.getInfo().getClan();
        if (clan == null) {
            return;
        }
        if (money <= 0) {
            return;
        }
        player.getSundry().setXuQuyenGop(money);
        PopupService.instance.sendPopupConfirmQuyenGop(player);
    }

    public void chatAllClan(@NonNull Player player, String chat) throws IOException {
        Clan clan = player.getInfo().getClan();
        if (clan == null) {
            return;
        }
        Message msg = new Message(CommandMessage.CHAT_CLAN);
        int isMaster = player.getSundry().getClanMember().getIsMaster();
        String prefix = isMaster == ClanConst.BANG_CHU ? "BC"
                : isMaster == ClanConst.PHO_BANG ? "PB"
                        : isMaster == ClanConst.TRUONG_LAO ? "TL"
                                : "TV";
        msg.writer().writeUTF(String.format("(%s) %s: %s", prefix, player.getName(), chat));
        sendMessageToAllPlayerOnGame(clan, msg);
    }

    public void viewChat(@NonNull Player player) throws IOException {
        Clan clan = player.getInfo().getClan();
        if (clan == null) {
            return;
        }
        sendListMessage(player, clan);
    }

    public void addChat(@NonNull Player player, String text) {
        Clan clan = player.getInfo().getClan();
        if (clan == null) {
            return;
        }
        ClanMessage clanMessage = new ClanMessage(player.getName(), text);
        clan.addMessage(clanMessage);
    }

    public void removeChat(@NonNull Player player, int index) {
        Clan clan = player.getInfo().getClan();
        if (clan == null) {
            return;
        }
        clan.removeMessage(index);
    }

    public void onActionInvite(@NonNull Player player, byte type, short idPlayer, boolean confirm) throws SQLException, IOException {
        switch (type) {
            case ClanConst.ACCEPT_INVITATION -> {
                Player playerInviter = player.getLocation().getZone().findPlayer(idPlayer);
                if (playerInviter == null || playerInviter.getInfo().getClan() == null || player.getInfo().getClan() != null) {
                    return;
                }
                if (confirm) {
                    acceptInvitation(player, playerInviter);
                } else {
                    sendRefuseInvitation(playerInviter, player);
                }
            }
            case ClanConst.INVITE_TO_CLAN -> {
                if (player.getInfo().getClan() == null || player.getSundry().getClanMember().getIsMaster() < 0 || player.getSundry().getClanMember().getIsMaster() >= 3) {
                    return;
                }
                Player playerInvited = player.getLocation().getZone().findPlayer(idPlayer);
                if (playerInvited == null || playerInvited.getInfo().getClan() != null) {
                    return;
                }
                sendInvitation(player, playerInvited);
            }
        }
    }

    public void leaveClan(@NonNull Player player) throws IOException {
        Clan clan = player.getInfo().getClan();
        if (clan == null || player.getName().equals(clan.getNameLeader())) {
            return;
        }
        Friend member = clan.findClanMember(player.getName());
        if (member == null) {
            return;
        }
        clan.removeMember(member);
        player.getInfo().setClan(null);
        player.getSundry().setClanMember(null);
        MapService.instance.sendInfoMe(player);
    }

    public void kickMember(@NonNull Player player, String nameKicked) throws IOException {
        Clan clan = player.getInfo().getClan();
        if (clan == null || !player.getName().equals(clan.getNameLeader())) {
            return;
        }
        Friend member = clan.findClanMember(nameKicked);
        if (member == null) {
            return;
        }
        clan.removeMember(member);
        Player memberOnGame = clan.findMemberOnGame(nameKicked);
        if (memberOnGame != null) {
            memberOnGame.getInfo().setClan(null);
            memberOnGame.getSundry().setClanMember(null);
            sendKickMember(memberOnGame);
            MapService.instance.sendInfoMe(memberOnGame);
        }
    }

    public void acceptInvitation(@NonNull Player playerInvited, @NonNull Player playerInviter) throws IOException {
        Clan clan = playerInviter.getInfo().getClan();
        List<ItemFriend> itemFriend = new ArrayList<>();
        for (ItemEquip item : playerInvited.getInventory().getItemBody()) {
            itemFriend.add(ItemService.instance.createNewItemFriend(item));
        }
        Friend member = Friend.builder().id(playerInvited.getIdDatabase()).name(playerInvited.getName()).head(playerInvited.getInfo().getHead()).level(playerInvited.getInfo().getLevel()).idClan(clan.getIndexIcon()).isMaster(ClanConst.THANH_VIEN).items(itemFriend).build();
        clan.addMember(member);
        clan.addMemberOnGame(playerInvited);
        playerInvited.getSundry().setClanMember(member);
        playerInvited.getInfo().setClan(clan);
        sendAcceptInvitation(playerInvited, playerInviter);
        MapService.instance.sendInfoMe(playerInvited);
    }

    public void doRegisterClan(@NonNull Player player) throws IOException {
        if (player.getInfo().getClan() != null) {
            return;
        }
        ItemPotion kimbai = InventoryService.instance.findItemPotion(player, (short) 31);
        if (kimbai == null) {
            Service.instance.sendLogOut(player.getSession(), "Không tìm thấy kim bài");
            return;
        }
        if (player.getInfo().getLevel() < ClanConst.LEVEL_NEED_TO_REGISTER) {
            Service.instance.sendLogOut(player.getSession(), String.format("Yêu cầu cấp độ %s để tạo bang hội", ClanConst.LEVEL_NEED_TO_REGISTER));
            return;
        }
        if (player.getInventory().getXu() < ClanConst.XU_NEED_TO_REGISTER) {
            Service.instance.sendLogOut(player.getSession(), String.format("Cần %s xu tạo bang hội", Util.formatNumber(ClanConst.LEVEL_NEED_TO_REGISTER)));
            return;
        }
        PopupService.instance.sendPopupConfirmRegClan(player);
    }

    public void dissolveClan(@NonNull Player player) throws IOException {
        Clan clan = player.getInfo().getClan();
        if (clan == null || !clan.getNameLeader().equals(player.getName())) {
            return;
        }
        if (!clan.isDissolve()) {
            if (Util.canDoWithTime(clan.getLastTimeCreate(), ClanConst.MINUTES_DELETE_CLAN * 60000)) {
                clan.setLastTimeEndDelete(System.currentTimeMillis() + ClanConst.MINUTES_DELETE_CLAN * 60000);
            } else {
                clan.setLastTimeEndDelete(clan.getLastTimeCreate() + ClanConst.MINUTES_DELETE_CLAN * 60000);
            }
            clan.setDissolve(true);
            Service.instance.sendLogOut(player.getSession(), String.format("Bang %s sẽ bị giải tán sau %s phút", clan.getName(), ClanConst.MINUTES_DELETE_CLAN - Util.getMinutesDifference(System.currentTimeMillis(), clan.getLastTimeEndDelete())));
        } else {
            clan.setDissolve(false);
            if (clan.getMembers().size() < 10) {
                Service.instance.sendLogOut(player.getSession(), String.format("Bang %s đã được khôi phục nhưng sẽ bị giải tán sau %s vì bang chưa đủ 10 thành viên", clan.getName(), ClanConst.MINUTES_DELETE_CLAN - Util.getMinutesDifference(System.currentTimeMillis(), clan.getLastTimeEndDelete())));
            } else {
                clan.setLastTimeEndDelete(0);
                Service.instance.sendLogOut(player.getSession(), String.format("Bang %s đã được khôi phục", clan.getName()));
            }
        }
    }

    public void confirmRegisterClan(@NonNull Player player, short indexIcon, String nameClan) throws IOException, SQLException {
        if (player.getInfo().getClan() != null) {
            return;
        }
        ItemPotion kimbai = InventoryService.instance.findItemPotion(player, (short) 31);
        if (kimbai == null) {
            Service.instance.sendLogOut(player.getSession(), "Không tìm thấy kim bài");
            return;
        }
        if (player.getInventory().getXu() < ClanConst.XU_NEED_TO_REGISTER) {
            Service.instance.sendLogOut(player.getSession(), String.format("Cần %s xu tạo bang hội", Util.formatNumber(ClanConst.LEVEL_NEED_TO_REGISTER)));
            return;
        }
        if (Manager.hasNameLeader(player.getName())) {
            Service.instance.sendLogOut(player.getSession(), "Có vẻ đã có bang hội tồi tại dưới trướng bạn");
            return;
        }
        if (Manager.hasNameClan(nameClan)) {
            Service.instance.sendLogOut(player.getSession(), String.format("Bang hội %s đã tồn tại", nameClan));
            return;
        }
        if (ClanManager.CLANS.containsKey(indexIcon)) {
            Service.instance.sendLogOut(player.getSession(), "Vui lòng chọn biểu tượng khác");
            return;
        }
        InventoryService.instance.minusQuantityItemPotion(player, kimbai, (short) 1);
        Clan clan = createNewClan(player, indexIcon, nameClan);
        List<ItemFriend> itemFriend = new ArrayList<>();
        for (ItemEquip item : player.getInventory().getItemBody()) {
            itemFriend.add(ItemService.instance.createNewItemFriend(item));
        }
        Friend leader = Friend.builder().id(player.getIdDatabase()).name(player.getName()).head(player.getInfo().getHead()).level(player.getInfo().getLevel()).idClan(clan.getIndexIcon()).isMaster(ClanConst.BANG_CHU).items(itemFriend).build();
        clan.addMember(leader);
        clan.addMemberOnGame(player);
        ClanManager.addClan(clan);
        player.getSundry().setClanMember(leader);
        player.getInfo().setClan(clan);
        sendFinishCreateClan(player);
        InventoryService.instance.sendItemPotion(player);
        MapService.instance.sendInfoMe(player);
    }

    private Clan createNewClan(@NonNull Player leader, short indexIcon, String nameClan) throws SQLException {
        long now = System.currentTimeMillis();
        Clan clan = Clan.builder().indexIcon(indexIcon).name(nameClan).nameLeader(leader.getName()).slogan(Settings.NAME_SERVER).level((byte) 1).members(new ArrayList<>()).messages(new ArrayList<>()).membersOnGame(new ArrayList<>()).lastTimeEndDelete(now + ClanConst.MINUTES_DELETE_CLAN * 60000).nationID(leader.getInfo().getIdNation()).lastTimeCreate(now).date(Util.convertTimeToString(now)).build();
        return clan;
    }

    public void sendKickMember(@NonNull Player player) {
        Message msg = new Message(CommandMessage.EVICTION_CLAN);
        player.getSession().sendMessage(msg);
    }

    public void sendInfoClan(@NonNull Player player, short idClan) throws IOException {
        Clan clan = Manager.getClan(idClan);
        if (clan == null) {
            return;
        }
        Message msg = new Message(CommandMessage.CLAN_INFO);
        msg.writer().writeShort(clan.getIndexIcon());
        msg.writer().writeUTF(clan.getName());
        msg.writer().writeUTF(clan.getNameLeader());
        msg.writer().writeByte(clan.getLevel());
        msg.writer().writeShort(clan.getMembers().size());
        msg.writer().writeLong(clan.getXu());
        msg.writer().writeLong(clan.getDedicationPoint());
        msg.writer().writeLong(clan.getExp());
        msg.writer().writeUTF(clan.getDate());
        msg.writer().writeUTF(clan.getSlogan());
        msg.writer().writeBoolean((clan.isDissolve() || (clan.getMembers().size() < 10 && clan.getLastTimeEndDelete() != 0)));
        msg.writer().writeByte(clan.getNationID());
        if (clan.isDissolve() || (clan.getMembers().size() < 10 && clan.getLastTimeEndDelete() != 0)) {
            msg.writer().writeUTF(String.format("Bang còn %s phút nữa sẽ bị giải tái", Util.getMinutesDifference(clan.getLastTimeEndDelete(), System.currentTimeMillis())));
        }
        player.getSession().sendMessage(msg);
    }

    public void sendMembers(@NonNull Player player) throws IOException {
        Clan clan = player.getInfo().getClan();
        if (clan == null) {
            return;
        }
        Message msg = new Message(CommandMessage.CLAN_LIST);
        msg.writer().writeUTF(clan.getName());
        msg.writer().writeShort(clan.getMembers().size());
        for (Friend member : clan.getMembers()) {
            msg.writer().writeUTF(member.getName());
            msg.writer().writeByte(member.getHead());
            msg.writer().writeByte(member.getLevel());
            msg.writer().writeByte(member.getItems().size());
            for (ItemFriend item : member.getItems()) {
                msg.writer().writeByte(item.getClassChar());
                msg.writer().writeShort(item.getIdTemplate());
                msg.writer().writeByte(item.getLevel());
                msg.writer().writeByte(item.getPlusTemplate());
            }
            msg.writer().writeShort(clan.getIndexIcon());
            msg.writer().writeByte(member.getIsMaster());
        }
        player.getSession().sendMessage(msg);
    }

    public void sendFinishCreateClan(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CHOOSE_ICON_CLAN);
        msg.writer().writeShort(player.getInfo().getClan().getIndexIcon());
        msg.writer().writeUTF(String.format("Tạo bang hội %s thành công", player.getInfo().getClan().getName()));
        player.getSession().sendMessage(msg);
    }

    public void sendRefuseInvitation(@NonNull Player inviter, @NonNull Player invited) throws IOException {
        Message msg = new Message(CommandMessage.ADD_CLAN);
        msg.writer().writeByte(1);
        msg.writer().writeUTF(String.format("%s đã từ chối lời mời vào bang", invited.getName()));
        inviter.getSession().sendMessage(msg);
    }

    public void sendAcceptInvitation(@NonNull Player invited, @NonNull Player inviter) throws IOException {
        Message msg = new Message(CommandMessage.ADD_CLAN);
        msg.writer().writeByte(1);
        msg.writer().writeUTF(String.format("%s đã gia nhập bang", invited.getName()));
        msg.writer().writeBoolean(true);
        msg.writer().writeShort(invited.getIdPlayer());
        msg.writer().writeShort(inviter.getInfo().getClan().getIndexIcon());
        inviter.getSession().sendMessage(msg);
    }

    public void sendInvitation(@NonNull Player inviter, @NonNull Player invited) throws IOException {
        Message msg = new Message(CommandMessage.ADD_CLAN);
        msg.writer().writeByte(0);
        msg.writer().writeShort(inviter.getIdPlayer());
        msg.writer().writeUTF(String.format("%s muốn mời bạn gia nhập bang %s", inviter.getName(), inviter.getInfo().getClan().getName()));
        invited.getSession().sendMessage(msg);
    }

    public void sendListMessage(@NonNull Player player, @NonNull Clan clan) throws IOException {
        Message msg = new Message(CommandMessage.MESSAGE_CLAN);
        msg.writer().writeShort(clan.getMessages().size());
        for (int i = 0; i < clan.getMessages().size(); i++) {
            ClanMessage message = clan.getMessages().get(i);
            msg.writer().writeInt(i);
            msg.writer().writeUTF(message.getName());
            msg.writer().writeUTF(message.getContent());
        }
        player.getSession().sendMessage(msg);
    }

    public void sendChooseIcon(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.REG_CLAN);
        msg.writer().writeShort(ClanConst.ICON_CLANS.length);
        for (int i = 0; i < ClanConst.ICON_CLANS.length; i++) {
            msg.writer().writeShort(ClanConst.ICON_CLANS[i]);
        }
        msg.writer().writeUTF("");
        player.getSession().sendMessage(msg);
    }

    private void sendMessageToAllPlayerOnGame(@NonNull Clan clan, @NonNull Message msg) {
        for (Player pl : clan.getMembersOnGame()) {
            if (pl != null && pl.getSession() != null) {
                pl.getSession().sendMessage(msg);
            }
        }
    }
}
