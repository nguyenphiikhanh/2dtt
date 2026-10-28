package interfaces;

import java.io.IOException;
import java.sql.SQLException;
import player.Player;
import java.util.List;
import network.Message;
import network.MessageHandler;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public interface ISession {

    ISession setSendCollect(final IMessageSendCollect collect);

    ISession setMessageHandler(final MessageHandler handler);

    ISession startCollect();

    ISession startSend();

    void loginAccount(Message msg);

    void updateChar() throws SQLException;

    void setPlayer(Player p);

    void setCurW(byte r);

    void setCurR(byte r);

    void setVersion(short s);

    void initThreadSession();

    void sendMessage(final Message msg);

    void sendKey() throws IOException;

    void doSendMessage(final Message msg);

    void disconnect();

    void setZoomLevel(final Message msg);

    void addChar(Player pl);

    String getIP();

    byte getCurW();

    byte getCurR();

    byte getZoomLevel();

    Player getPlayer();

    short getVersion();

    int getDistanceLoad();

    boolean isConnected();

    boolean isAdmin();

    boolean isSendKeyComplete();

    String getUsername();

    List<Player> getListChar();

    int getUserID();

    int getID();

    void emptyListChar(int idExcept) throws SQLException;

    void reloadChar(int id);

    Player findPlayer(int id) throws SQLException;

    Runnable update();
}
