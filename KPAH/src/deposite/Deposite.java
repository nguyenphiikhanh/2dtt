package deposite;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Synchronized;
import manager.ClientManager;
import player.Player;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class Deposite {

    private String nameDeposite;
    private List<Player> playerSell;

    public boolean isMaxSeller() {
        return playerSell.size() >= 15;
    }

    @Synchronized
    public void addPlayer(@NonNull Player player) {
        if (!playerSell.contains(player)) {
            playerSell.add(player);
        }
    }

    @Synchronized
    public void removePlayerOffline() {
        playerSell.removeIf(p -> (!ClientManager.containsPlayers(p) || (p.getSundry().getDepositeItemEquips().size() <= 0 && p.getSundry().getDepositeItemGems().size() <= 0)));
    }

    public Player getPlayer(short idPlayer) {
        return playerSell.stream().filter(p -> p != null && p.getIdPlayer() == idPlayer).findFirst().orElse(null);
    }
}
