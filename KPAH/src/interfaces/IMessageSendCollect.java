package interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import network.Message;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public interface IMessageSendCollect {

    Message readMessage(final ISession p0, final DataInputStream p1) throws Exception;

    byte readKey(final ISession p0, final byte p1);

    void doSendMessage(final ISession p0, final DataOutputStream p1, final Message p2) throws Exception;

    byte writeKey(final ISession p0, final byte p1);
}
