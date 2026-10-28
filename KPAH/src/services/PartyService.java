package services;

import java.io.IOException;
import lombok.NonNull;
import manager.Settings;
import network.Message;
import player.Party;
import player.Player;
import utils.CommandMessage;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class PartyService {

    public static final PartyService instance = new PartyService();
    public static final byte CREATE_PARTY = -1;
    private static final byte INVITE_PARTY = 0;
    private static final byte ACCEPT_PARTY = 1;
    private static final byte REFUSE_PARTY = 2;

    private static final byte KICK_MEMBER = 0;
    private static final byte DISBAND_PARTY = 1;
    private static final byte LEAVE_PARTY = 2;

    public void doActionKick(@NonNull Player player, byte type, short idPlayer) throws IOException {
        switch (type) {
            case KICK_MEMBER -> {
                if (player.getParty().getIdLeader() == player.getIdPlayer()) {
                    kickMember(player, idPlayer);
                }
            }
            case DISBAND_PARTY -> {
                if (player.getParty().getIdLeader() == player.getIdPlayer()) {
                    disbandParty(player, false);
                }
            }
            case LEAVE_PARTY -> {
                leaverParty(player);
            }
        }
    }

    public void doActionInvite(@NonNull Player player, byte type, short idPlayerFocus) throws IOException {
        if (player.getIdPlayer() == idPlayerFocus) {
            return;
        }
        switch (type) {
            case CREATE_PARTY -> {
                createParty(player);
            }
            case INVITE_PARTY -> {
                invitePlayerToParty(player, idPlayerFocus);
            }
            case ACCEPT_PARTY -> {
                acceptToParty(player, idPlayerFocus);
            }
            case REFUSE_PARTY -> {
                refuseToParty(player, idPlayerFocus);
            }
        }
    }

    public void disbandParty(@NonNull Player leader, boolean isLogout) throws IOException {
        if (leader.getParty().isEmpty()) {
            return;
        }
        sendDisbandParty(leader);
        for (Player member : leader.getParty().getMembers()) {
            if (member.getIdPlayer() == leader.getIdPlayer()) {
                continue;
            }
            member.setParty(new Party(member));
        }
        leader.getParty().dispose();
        if (!isLogout) {
            leader.setParty(new Party(leader));
        }
    }

    public void leaverParty(@NonNull Player player) throws IOException {
        if (player.getParty().isEmpty()) {
            return;
        }
        if (player.getParty().getIdLeader() == player.getIdPlayer()) {
            return;
        }
        sendPlayerLeave(player);
        player.getParty().removeMember(player);
        player.setParty(new Party(player));
    }

    public void kickMember(@NonNull Player leader, short idPlayerKick) throws IOException {
        if (leader.getParty().isEmpty()) {
            return;
        }
        if (leader.getIdPlayer() == idPlayerKick) {
            return;
        }
        Player playerKicked = leader.getParty().getMember(idPlayerKick);
        if (playerKicked == null || playerKicked.getParty().isEmpty()) {
            return;
        }
        sendPlayerKicked(leader, idPlayerKick);
        leader.getParty().removeMember(leader);
        playerKicked.setParty(new Party(playerKicked));
    }

    private void refuseToParty(@NonNull Player invited, short idInviter) throws IOException {
        Player plInviter = invited.getLocation().getZone().findPlayer(idInviter);
        if (plInviter == null) {
            return;
        }
        sendPlayerRefuseInvitation(plInviter, invited);
    }

    private void acceptToParty(@NonNull Player invited, short idInviter) throws IOException {
        if (!invited.getParty().isEmpty()) {
            Service.instance.sendLogOut(invited.getSession(), "Bạn đã có nhóm");
            return;
        }
        Player plInviter = invited.getLocation().getZone().findPlayer(idInviter);
        if (plInviter == null) {
            Service.instance.sendLogOut(invited.getSession(), "Không tìm thấy người mời trong khu vực");
            return;
        }
        if (plInviter.getParty().hasMember(invited.getIdPlayer())) {
            return;
        }
        invited.setParty(plInviter.getParty());
        plInviter.getParty().addMember(invited);
        sendPlayerAcceptInvitation(plInviter, invited);
    }

    private void createParty(@NonNull Player player) throws IOException {
        if (player.getParty().isEmpty()) {
            sendSuccessCreateParty(player);
        }
    }

    private void invitePlayerToParty(@NonNull Player player, short idPlayerInvited) throws IOException {
        if (player.getParty() == null) {
            return;
        }
        Player plInvited = player.getLocation().getZone().findPlayer(idPlayerInvited);
        if (plInvited == null || plInvited.getSundry().getPk() > 0 || plInvited.getSundry().isKiller()) {
            return;
        }
        if (plInvited.getParty().getMembers().size() >= Settings.MAX_PLAYER) {
            Service.instance.sendLogOut(player.getSession(), "Đạt số lượng thành viên tối đa");
            return;
        }
        if (!plInvited.getParty().isEmpty()) {
            Service.instance.sendLogOut(player.getSession(), String.format("%s đã có nhóm", plInvited.getName()));
            return;
        }
        sendInviteToParty(player, plInvited);
    }

    private void sendDisbandParty(@NonNull Player leader) throws IOException {
        Message msg = new Message(CommandMessage.KICK_PARTY);
        msg.writer().writeByte(DISBAND_PARTY);
        sendAllPlayerInParty(leader.getParty(), msg);
    }

    private void sendPlayerLeave(@NonNull Player leaver) throws IOException {
        Message msg = new Message(CommandMessage.KICK_PARTY);
        msg.writer().writeByte(LEAVE_PARTY);
        msg.writer().writeShort(leaver.getIdPlayer());
        sendAllPlayerInParty(leaver.getParty(), msg);
    }

    private void sendPlayerKicked(@NonNull Player leader, short idKick) throws IOException {
        Message msg = new Message(CommandMessage.KICK_PARTY);
        msg.writer().writeByte(KICK_MEMBER);
        msg.writer().writeShort(idKick);
        sendAllPlayerInParty(leader.getParty(), msg);
    }

    private void sendPlayerRefuseInvitation(@NonNull Player inviter, @NonNull Player invited) throws IOException {
        Message msg = new Message(CommandMessage.ADD_TO_PARTY);
        msg.writer().writeByte(REFUSE_PARTY);
        msg.writer().writeUTF(invited.getName());
        inviter.getSession().sendMessage(msg);
    }

    private void sendPlayerAcceptInvitation(@NonNull Player inviter, @NonNull Player invited) throws IOException {
        Message msg = new Message(CommandMessage.ADD_TO_PARTY);
        msg.writer().writeByte(ACCEPT_PARTY);
        msg.writer().writeShort(invited.getIdPlayer());
        msg.writer().writeUTF(invited.getName());
        msg.writer().writeByte(invited.getInfo().getLevel());
        msg.writer().writeByte(invited.getInfo().getClassPlayer());
        inviter.getSession().sendMessage(msg);
    }

    private void sendSuccessCreateParty(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CREATE_PARTY);
        msg.writer().writeShort(player.getParty().getIdParty());
        player.getSession().sendMessage(msg);
    }

    private void sendInviteToParty(@NonNull Player inviter, @NonNull Player plInvived) throws IOException {
        Message msg = new Message(CommandMessage.ADD_TO_PARTY);
        msg.writer().writeByte(INVITE_PARTY);
        msg.writer().writeShort(inviter.getParty().getIdParty());
        msg.writer().writeShort(inviter.getIdPlayer());
        msg.writer().writeUTF(inviter.getName());
        msg.writer().writeByte(inviter.getInfo().getLevel());
        msg.writer().writeShort(-1);
        msg.writer().writeByte(inviter.getInfo().getClassPlayer());
        for (Player member : inviter.getParty().getMembers()) {
            if (member.getIdPlayer() == inviter.getIdPlayer()) {
                continue;
            }
            msg.writer().writeShort(member.getIdPlayer());
            msg.writer().writeUTF(member.getName());
            msg.writer().writeByte(member.getInfo().getLevel());
            msg.writer().writeByte(member.getInfo().getClassPlayer());
        }
        plInvived.getSession().sendMessage(msg);
    }

    private void sendAllPlayerInParty(@NonNull Party party, @NonNull Message msg) {
        for (int i = 0; i < party.getMembers().size(); i++) {
            Player member = party.getMembers().get(i);
            if (member != null) {
                member.getSession().sendMessage(msg);
            }
        }
    }
}
