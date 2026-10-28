package network;

import interfaces.IMessageSendCollect;
import interfaces.ISession;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;

import utils.Printer;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public final class Sender implements Runnable {

    @NonNull
    private ISession session;
    @NonNull
    private BlockingQueue<Message> messages;
    @NonNull
    private DataOutputStream dos;
    @NonNull
    private IMessageSendCollect sendCollect;
    public long lastTimeCollectMessage = System.currentTimeMillis();

    public Sender(@NonNull ISession session, @NonNull Socket socket) {
        try {
            this.session = session;
            this.messages = new LinkedBlockingQueue<>();
            setSocket(socket);
        } catch (Exception ex) {
        }
    }

    public Sender setSocket(@NonNull Socket socket) {
        try {
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
        }
        return this;
    }

    @Override
    public void run() {
        try {
            while (this.session.isConnected()) {
                while (!this.messages.isEmpty()) {
                    Message message = messages.poll(5, TimeUnit.SECONDS);
                    if (message != null) {
                        this.doSendMessage(message);
                        lastTimeCollectMessage = System.currentTimeMillis();
                        message.cleanup();
                    }
                }
                TimeUnit.MILLISECONDS.sleep(5);
            }
        } catch (Exception e) {
        }
    }

    public synchronized void doSendMessage(@NonNull Message message) throws Exception {
        this.sendCollect.doSendMessage(this.session, this.dos, message);
    }

    public synchronized void sendMessage(@NonNull Message msg) {
        if (this.session.isConnected()) {
            this.messages.add(msg);
        }
    }

    public void setSend(@NonNull IMessageSendCollect sendCollect) {
        this.sendCollect = sendCollect;
    }

    public int getNumMessages() {
        return this.messages.size();
    }

    public void close() {
        this.messages.clear();
        try {
            this.dos.close();
        } catch (IOException ex) {
        }
    }

    public void dispose() {
        this.session = null;
        this.messages = null;
        this.sendCollect = null;
        this.dos = null;
    }
}
