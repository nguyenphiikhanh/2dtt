package services;

import java.io.IOException;
import lombok.NonNull;
import manager.ClientManager;
import network.Message;
import player.Player;
import utils.CommandMessage;
import utils.Printer;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ChatService {

    public static final ChatService instance = new ChatService();

    public void sendChatPrivate(@NonNull Player playerSend, String nameChat, String chat) throws IOException {
        if (playerSend.getName().equals(nameChat)) {
            return;
        }
        Player playerReceive = ClientManager.getPlayer(nameChat);
        if (playerReceive == null) {
            return;
        }
        Message msg = new Message(CommandMessage.MESSAGE_PRIVATE);
        msg.writer().writeByte(1);
        msg.writer().writeUTF(playerSend.getName());
        msg.writer().writeUTF(chat);
        playerReceive.getSession().sendMessage(msg);
    }

    public void sendChatWorld(@NonNull Player pl, String chat) throws IOException {
        Message msg = new Message(CommandMessage.MESSAGE_WORLD);
        msg.writer().writeUTF(String.format("%s: %s", pl.getName(), chat));
        Service.instance.sendAllPlayer(msg);
    }

    public void sendChat(@NonNull Player pl, String chat) throws IOException {
        if (pl.getSession().isAdmin() && processChatAdmin(pl, chat)) {
            return;
        }
        Message msg = new Message(CommandMessage.CHAT);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeUTF(chat);
        MapService.instance.sendAnotherNotMeInMap(pl, msg);
    }

    public void sendChatDelay(@NonNull Player pl, String s) throws IOException {
        Message msg = new Message(CommandMessage.MESSAGE_DELAY);
        msg.writer().writeUTF(s);
        pl.getSession().sendMessage(msg);
    }

    public void sendChatOnlyMe(@NonNull Player pl, String chat) throws IOException {
        Message msg = new Message(CommandMessage.CHAT);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeUTF(chat);
        pl.getSession().sendMessage(msg);
    }

    private boolean processChatAdmin(@NonNull Player pl, String chat) throws IOException {
        if (chat.startsWith("m ")) {
            ChangeMapService.instance.changeMap(pl, Short.parseShort(chat.replace("m ", "")), (short) -1, (short) -1);
            return true;
        }
        return false;
    }
}
