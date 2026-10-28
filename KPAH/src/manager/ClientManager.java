package manager;

import player.Player;
import interfaces.ISession;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.Synchronized;

public class ClientManager {

    private static final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Player> playersName = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, ISession> clients = new ConcurrentHashMap<>();

    @Synchronized
    public static ConcurrentHashMap<Integer, Player> getPlayers() {
        return players;
    }

    @Synchronized
    public static ConcurrentHashMap<Integer, ISession> getClients() {
        return clients;
    }

    public static boolean containsPlayers(@NonNull Player pl) {
        return players.contains(pl);
    }

    public static Player getPlayerByUserID(int userID) {
        for (Player player : players.values()) {
            if (player != null && player.getSession().getUserID() == userID) {
                return player;
            }
        }
        return null;
    }

    public static Player getPlayer(int id) {
        return players.getOrDefault(id, null);
    }

    public static Player getPlayer(String name) {
        return playersName.getOrDefault(name, null);
    }

    @Synchronized
    public static void close() {
        for (Integer key : ClientManager.clients.keySet()) {
            ClientManager.kickClient(ClientManager.clients.remove(key));
        }
    }

    public static void kickClient(@NonNull ISession cl) {
        removeClient(cl);
        if (cl.getPlayer() != null) {
            removePlayer(cl.getPlayer());
        }
    }

    @Synchronized
    public static void joinClient(@NonNull ISession cl) {
        if (!clients.contains(cl)) {
            clients.put(cl.getID(), cl);
        }
    }

    @Synchronized
    public static void removeClient(@NonNull ISession cl) {
        if (clients.contains(cl)) {
            clients.remove(cl.getID());
        }
    }

    @Synchronized
    public static void joinPlayer(@NonNull Player pl) {
        if (!players.contains(pl)) {
            players.put(pl.getIdDatabase(), pl);
        }
        if (!playersName.contains(pl)) {
            playersName.put(pl.getName(), pl);
        }
    }

    @Synchronized
    public static void removePlayer(@NonNull Player pl) {
        if (players.contains(pl)) {
            players.remove(pl.getIdDatabase());
        }
        if (playersName.contains(pl)) {
            playersName.remove(pl.getName());
        }
    }
}
