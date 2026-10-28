package services;

import clan.Clan;
import item.ItemFriend;
import java.io.IOException;
import lombok.NonNull;
import manager.TopManager;
import network.Message;
import player.Player;
import top.ClanInfo;
import top.TopPlayer;
import utils.CommandMessage;
import utils.Printer;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class TopService {

    public static final TopService instance = new TopService();

    private static final byte TOP_STRONGER = 4;
    private static final byte TOP_RIGHER = 3;
    private static final byte TOP_CLAN = 5;
    private static final byte TOP = 7;
    private static final byte TOP_CUC_BO = 8;

    public void doShowTop(@NonNull Player player, byte type, byte page) throws IOException {
        switch (type) {
            case TOP_CLAN ->
                showTopClan(player);
            case TOP_STRONGER ->
                showTopStronger(player);
            case TOP_RIGHER ->
                showTopRigher(player);
            case TOP ->
                MenuOptionService.instance.sendMenuBangTop(player);
            default ->
                Printer.printRed("Top Not Found: " + type);
        }
    }

    private void showTopStronger(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.TOP_STRONGER_RICHER);
        msg.writer().writeByte(TOP_RIGHER);
        msg.writer().writeUTF("");
        msg.writer().writeShort(TopManager.TOP_STRONGER.size());
        for (int i = 0; i < TopManager.TOP_STRONGER.size(); i++) {
            TopPlayer top = TopManager.TOP_STRONGER.get(i);
            msg.writer().writeUTF(top.getName());
            msg.writer().writeByte(top.getHead());
            msg.writer().writeByte(top.getLevel());
            msg.writer().writeByte(top.getItems().size());
            for (int k = 0; k < top.getItems().size(); k++) {
                ItemFriend item = top.getItems().get(k);
                msg.writer().writeByte(item.getClassChar());
                msg.writer().writeShort(item.getIdTemplate());
                msg.writer().writeByte(item.getLevel());
                msg.writer().writeByte(item.getPlusTemplate());
            }
            msg.writer().writeShort(top.getIdClan());
            msg.writer().writeByte(top.getIsMaster());
            msg.writer().writeLong(top.getXu());
            msg.writer().writeInt(top.getLuong());
            msg.writer().writeByte(top.getNationId());
        }
        player.getSession().sendMessage(msg);
    }

    private void showTopRigher(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.TOP_STRONGER_RICHER);
        msg.writer().writeByte(TOP_RIGHER);
        msg.writer().writeUTF("");
        msg.writer().writeShort(TopManager.TOP_RIGHER.size());
        for (int i = 0; i < TopManager.TOP_RIGHER.size(); i++) {
            TopPlayer top = TopManager.TOP_RIGHER.get(i);
            msg.writer().writeUTF(top.getName());
            msg.writer().writeByte(top.getHead());
            msg.writer().writeByte(top.getLevel());
            msg.writer().writeByte(top.getItems().size());
            for (int k = 0; k < top.getItems().size(); k++) {
                ItemFriend item = top.getItems().get(k);
                msg.writer().writeByte(item.getClassChar());
                msg.writer().writeShort(item.getIdTemplate());
                msg.writer().writeByte(item.getLevel());
                msg.writer().writeByte(item.getPlusTemplate());
            }
            msg.writer().writeShort(top.getIdClan());
            msg.writer().writeByte(top.getIsMaster());
            msg.writer().writeLong(top.getXu());
            msg.writer().writeInt(top.getLuong());
            msg.writer().writeByte(top.getNationId());
        }
        player.getSession().sendMessage(msg);
    }

    private void showTopClan(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.TOP_STRONGER_RICHER);
        msg.writer().writeByte(TOP_CLAN);
        msg.writer().writeUTF("");
        msg.writer().writeShort(TopManager.TOP_CLANS.size());
        for (int i = 0; i < TopManager.TOP_CLANS.size(); i++) {
            ClanInfo clan = TopManager.TOP_CLANS.get(i);
            msg.writer().writeShort(clan.getIndexIcon());
            msg.writer().writeUTF(clan.getName());
            msg.writer().writeUTF(clan.getNameLeader());
            msg.writer().writeByte(clan.getLevel());
            msg.writer().writeShort(clan.getMembers());
            msg.writer().writeLong(clan.getXu());
            msg.writer().writeByte(clan.getNationId());
        }
        player.getSession().sendMessage(msg);
    }
}
