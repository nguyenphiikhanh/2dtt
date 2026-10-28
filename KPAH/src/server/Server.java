package server;

import manager.Manager;
import network.Session;
import manager.ClientManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Scanner;
import manager.ClanManager;
import manager.ExecutorVirtualThread;
import manager.Settings;
import manager.TopManager;
import network.MessageHandler;
import network.MessageSendCollect;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import utils.Logger;
import utils.Printer;

public class Server implements Runnable {

    private ServerSocketChannel serverChannel;
    private boolean isBaoTri;

    public void init() {
        new Thread(this, "Server Socket Thread").start();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.init();
    }

    @Override
    public void run() {
        try {
            AnsiConsole.systemInstall();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(Settings.PORT_SERVER));
            serverChannel.configureBlocking(false);
            Printer.printAscii(Settings.LOGO_GAME, 0, 255, 255);
            Printer.printAscii(Settings.ICON, Ansi.Color.RED);
            Manager.init();
            activeCommandLine();
            Printer.printGreen("Listen Port " + Settings.PORT_SERVER);
            ExecutorVirtualThread.submitServer(ClanManager.update());
            ExecutorVirtualThread.submitServer(TopManager.updateTopClan());
            while (!isBaoTri) {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    if (ClientManager.getPlayers().size() < Settings.MAX_PLAYER) {
                        ExecutorVirtualThread.submitServer(() -> handleClient(clientChannel));
                    } else {
                        clientChannel.close();
                    }
                }
            }
        } catch (IOException e) {
            Printer.printRed("Port Is Already Open");
        } finally {
            try {
                serverChannel.close();
            } catch (IOException e) {
                Printer.printRed("Error closing server channel.");
            }
            ExecutorVirtualThread.shutdownServer();
        }
    }

    private void handleClient(SocketChannel clientChannel) {
        try {
            Session session = new Session(clientChannel.socket());
            session.initThreadSession();
            session.setMessageHandler(new MessageHandler()).setSendCollect(new MessageSendCollect()).startCollect();
            ExecutorVirtualThread.submitThreadSession(session.update());
            Printer.printGreen("Accept IpAddress " + session.getIP());
            ClientManager.joinClient(session);
        } catch (IOException e) {
            Printer.printRed("Error Handling Client");
            Logger.logError("Lỗi Handle Client", e);
        }
    }

    private void activeCommandLine() {
        Thread.ofVirtual().start(() -> {
            try {
                Scanner sc = new Scanner(System.in);
                while (true) {
                    String line = sc.nextLine();
                    switch (line) {
                        case "baotri" -> {
                            isBaoTri = true;
                            closeServer();
                        }
                        case "thread" ->
                            Printer.printRed("Thread count: " + Thread.activeCount());
                        case "player" ->
                            Printer.printRed("Player in game: " + ClientManager.getPlayers().size());
                        case "session" ->
                            Printer.printRed("Session connect: " + ClientManager.getClients().size());
                    }
                }
            } catch (Exception e) {
                Logger.logError("Lỗi command line", e);
            }
        });
    }

    private void closeServer() throws SQLException {
        ClanManager.saveDataClan();
        ClientManager.close();
        System.exit(0);
    }
}
