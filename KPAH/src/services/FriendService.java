package services;

import item.ItemEquip;
import item.ItemFriend;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import network.Message;
import player.Friend;
import player.Player;
import utils.CommandMessage;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class FriendService {

    public static final FriendService instance = new FriendService();

    private static final byte ADD_FRIEND = 0;
    private static final byte ACCEPT_FRIEND = 1;

    public void doAction(@NonNull Player player, byte ok, short idPlayerAdd) throws IOException {
        switch (ok) {
            case ADD_FRIEND -> {
                addFriend(player, idPlayerAdd);
            }
            case ACCEPT_FRIEND -> {
                acceptFriend(player, idPlayerAdd);
            }
        }
    }

    public void removeFriend(@NonNull Player player, String name) {
        if (!player.getFriends().stream().anyMatch(f -> f != null && f.getName().equals(name))) {
            return;
        }
        player.getFriends().removeIf(f -> f != null && f.getName().equals(name));
    }

    private void acceptFriend(@NonNull Player playerReceive, short idPlayerSend) throws IOException {
        Player playerSend = playerReceive.getLocation().getZone().findPlayer(idPlayerSend);
        if (playerSend == null) {
            return;
        }
        if (playerReceive.getFriends().stream().anyMatch(f -> f != null && f.getId() == idPlayerSend)) {
            Service.instance.sendLogOut(playerReceive.getSession(), String.format("Đã kết bạn với %s rồi", playerSend.getName()));
            return;
        }
        List<ItemFriend> itemFriendSend = new ArrayList<>();
        for (ItemEquip item : playerSend.getInventory().getItemBody()) {
            itemFriendSend.add(ItemService.instance.createNewItemFriend(item));
        }
        Friend friendSend = Friend.builder().id(playerSend.getIdDatabase()).name(playerSend.getName()).head(playerSend.getInfo().getHead()).level(playerSend.getInfo().getLevel()).idClan(playerSend.getInfo().getClan() == null ? -1 : playerSend.getInfo().getClan().getIndexIcon()).isMaster((byte) -1).items(itemFriendSend).build();
        List<ItemFriend> itemFriendReceive = new ArrayList<>();
        for (ItemEquip item : playerReceive.getInventory().getItemBody()) {
            itemFriendReceive.add(ItemService.instance.createNewItemFriend(item));
        }
        Friend friendReceive = Friend.builder().id(playerReceive.getIdDatabase()).name(playerReceive.getName()).head(playerReceive.getInfo().getHead()).level(playerReceive.getInfo().getLevel()).idClan(playerReceive.getInfo().getClan() == null ? -1 : playerReceive.getInfo().getClan().getIndexIcon()).isMaster((byte) -1).items(itemFriendReceive).build();
        playerReceive.getFriends().add(friendSend);
        playerSend.getFriends().add(friendReceive);
        sendListFriend(playerSend);
        sendListFriend(playerReceive);
    }

    private void addFriend(@NonNull Player playerSend, short idPlayerReceive) throws IOException {
        Player playerReceive = playerSend.getLocation().getZone().findPlayer(idPlayerReceive);
        if (playerReceive == null) {
            return;
        }
        if (playerSend.getFriends().stream().anyMatch(f -> f != null && f.getId() == idPlayerReceive)) {
            Service.instance.sendLogOut(playerSend.getSession(), String.format("Đã kết bạn với %s rồi", playerReceive.getName()));
            return;
        }
        sendRequestAddFriend(playerSend, playerReceive);
    }

    private void sendRequestAddFriend(@NonNull Player playerSend, @NonNull Player playerReceive) throws IOException {
        Message msg = new Message(CommandMessage.ADD_FRIEND);
        msg.writer().writeByte(0);
        msg.writer().writeUTF(playerSend.getName());
        msg.writer().writeShort(playerSend.getIdPlayer());
        playerReceive.getSession().sendMessage(msg);
    }

    public void sendListFriend(@NonNull Player player) throws IOException {
        Message msg = new Message(CommandMessage.CMD_GET_FRIENDLIST);
        msg.writer().writeShort(player.getFriends().size());
        for (Friend friend : player.getFriends()) {
            msg.writer().writeUTF(friend.getName());
            msg.writer().writeByte(friend.getHead());
            msg.writer().writeByte(friend.getLevel());
            msg.writer().writeByte(friend.getItems().size());
            for (ItemFriend item : friend.getItems()) {
                msg.writer().writeByte(item.getClassChar());
                msg.writer().writeShort(item.getIdTemplate());
                msg.writer().writeByte(item.getLevel());
                msg.writer().writeByte(item.getPlusTemplate());
            }
            msg.writer().writeShort(friend.getIdClan());
            msg.writer().writeByte(friend.getIsMaster());
        }
        player.getSession().sendMessage(msg);
    }
}
