package network;

import interfaces.IMessageSendCollect;
import interfaces.ISession;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;

import utils.CommandMessage;
import utils.Printer;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public final class Collector implements Runnable {

    @NonNull
    private ISession session;
    @NonNull
    private DataInputStream dis;
    @NonNull
    private IMessageSendCollect collect;
    @NonNull
    private MessageHandler messageHandler;

    public Collector(@NonNull ISession session, @NonNull Socket socket) {
        try {
            this.session = session;
            setSocket(socket);
        } catch (Exception e) {
        }
    }

    public Collector setSocket(@NonNull Socket socket) {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
        }
        return this;
    }

    @Override
    public void run() {
        try {
            while (this.session.isConnected()) {
                final Message msg = this.collect.readMessage(this.session, this.dis);
                if (msg.command == CommandMessage.REQUEST_KEY) {
                    this.session.sendKey();
                } else {
                    this.messageHandler.onMessage(this.session, msg);
                }
                msg.cleanup();
                TimeUnit.MILLISECONDS.sleep(5);
            }
        } catch (Exception ex) {
        }
        this.session.disconnect();
    }

    public void setCollect(@NonNull IMessageSendCollect collect) {
        this.collect = collect;
    }

    public void setMessageHandler(@NonNull MessageHandler handler) {
        this.messageHandler = handler;
    }

    public void close() {
        try {
            this.dis.close();
        } catch (IOException ex) {
        }
    }

    public void dispose() {
        this.session = null;
        this.dis = null;
        this.collect = null;
    }
}
