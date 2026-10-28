package network;

import interfaces.IMessageSendCollect;
import interfaces.ISession;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import lombok.NonNull;

import manager.Settings;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class MessageSendCollect implements IMessageSendCollect {

    @Override
    public Message readMessage(@NonNull ISession p0, DataInputStream dis) throws Exception {
        byte cmd = dis.readByte();
        if (p0.isSendKeyComplete()) {
            cmd = readKey(p0, cmd);
        }
        int size;
        if (p0.isSendKeyComplete()) {
            byte b1 = dis.readByte();
            byte b2 = dis.readByte();
            size = (readKey(p0, b1) & 255) << 8 | readKey(p0, b2) & 255;
        } else {
            size = dis.readShort();
        }
        byte data[] = new byte[size];
        int len = 0;
        int byteRead = 0;
        while (len != -1 && byteRead < size) {
            len = dis.read(data, byteRead, size - byteRead);
            if (len > 0) {
                byteRead += len;
            }
        }
        if (p0.isSendKeyComplete()) {
            for (int i = 0; i < data.length; i++) {
                data[i] = readKey(p0, data[i]);
            }
        }
        return new Message(cmd, data);
    }

    @Override
    public byte readKey(@NonNull ISession p0, byte p1) {
        final byte curR = p0.getCurR();
        p0.setCurR((byte) (curR + 1));
        final byte i = (byte) ((Settings.KEYS[curR] & 0xFF) ^ (p1 & 0xFF));
        if (p0.getCurR() >= Settings.KEYS.length) {
            p0.setCurR((byte) (p0.getCurR() % Settings.KEYS.length));
        }
        return i;
    }

    @Override
    public void doSendMessage(@NonNull ISession p0, DataOutputStream dos, Message msg) throws Exception {
        byte[] data = msg.getData();
        byte save_b = -1;
        if (msg.command == 25 || msg.command == 26 || msg.command == -8) {
            save_b = msg.command;
            msg.command = -128;
        }
        if (p0.isSendKeyComplete()) {
            byte b = writeKey(p0, msg.command);
            dos.writeByte(b);
        } else {
            dos.writeByte(msg.command);
        }
        if (data != null) {
            int size = data.length;
            if (msg.command == -128) {
                byte bspec = writeKey(p0, (byte) save_b);
                dos.writeByte(bspec);
                byte b4 = (byte) (size);
                byte b3 = (byte) ((byte) (size >> 8));
                byte b2 = (byte) ((byte) (size >> 16));
                byte b1 = (byte) ((byte) (size >> 24));
                final int byte4 = this.writeKey(p0, b4);
                final int byte3 = this.writeKey(p0, b3);
                final int byte2 = this.writeKey(p0, b2);
                final int byte1 = this.writeKey(p0, b1);
                dos.writeByte(byte1);
                dos.writeByte(byte2);
                dos.writeByte(byte3);
                dos.writeByte(byte4);
            } else if (p0.isSendKeyComplete()) {
                int byte1 = writeKey(p0, (byte) (size >> 8));
                dos.writeByte(byte1);
                int byte2 = writeKey(p0, (byte) (size));
                dos.writeByte(byte2);
            } else {
                final int byte1 = (byte) (size & 0xFF00);
                dos.writeByte(byte1);
                final int byte2 = (byte) (size & 0xFF);
                dos.writeByte(byte2);
            }
            if (p0.isSendKeyComplete()) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = writeKey(p0, data[i]);
                }
            }
            dos.write(data);
        } else {
            dos.writeByte(0);
            dos.writeByte(0);
        }
        dos.flush();
    }

    @Override
    public byte writeKey(@NonNull ISession p0, byte p1) {
        final byte curW = p0.getCurW();
        p0.setCurW((byte) (curW + 1));
        final byte i = (byte) ((Settings.KEYS[curW] & 0xFF) ^ (p1 & 0xFF));
        if (p0.getCurW() >= Settings.KEYS.length) {
            p0.setCurW((byte) (p0.getCurW() % Settings.KEYS.length));
        }
        return i;
    }
}
