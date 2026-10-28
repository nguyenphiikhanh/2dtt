package map;

import interfaces.IMap;
import item.ItemMap;
import java.io.IOException;
import java.util.ArrayList;
import player.Player;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Synchronized;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Builder
@Data
public class Zone {

    private byte id;
    private IMap map;
    private List<Monster> mobs;
    private List<Player> players;
    private List<ItemMap> items;
    @Builder.Default
    private short countItemAppeaerd = Short.MIN_VALUE;

    @Synchronized
    public void initMobZone() {
        for (int i = 0; i < mobs.size(); i++) {
            Monster mob = mobs.get(i);
            if (mob != null) {
                mob.setZone(this);
            }
        }
    }

    @Synchronized
    public void removeItem(ItemMap itemMap) {
        itemMap.dispose();
        countItemAppeaerd--;
        items.remove(itemMap);
    }

    @Synchronized
    public void addItem(ItemMap itemMap) {
        itemMap.setItemMapId(countItemAppeaerd);
        countItemAppeaerd++;
        if (countItemAppeaerd >= Short.MAX_VALUE) {
            countItemAppeaerd = Short.MIN_VALUE;
        }
        items.add(itemMap);
    }

    @Synchronized
    public void removePlayer(@NonNull Player player) {
        this.players.removeIf(p -> p != null && p.equals(player));
    }

    @Synchronized
    public void addPlayer(@NonNull Player player) {
        this.players.add(player);
    }

    @Synchronized
    public Player findPlayer(String name) {
        return this.players.stream().filter(m -> m != null && m.getName().equals(name)).findFirst().orElse(null);
    }

    @Synchronized
    public Player findPlayer(short id) {
        return this.players.stream().filter(m -> m != null && m.getIdPlayer() == id).findFirst().orElse(null);
    }

    @Synchronized
    public Monster findMob(short id) {
        return this.mobs.stream().filter(m -> m != null && m.getId() == id).findFirst().orElse(null);
    }

    @Synchronized
    public List<Monster> findMobNear(@NonNull Player pl, short[] id, int distance) {
        List<Monster> mobsNear = new ArrayList<>();
        for (short idMob : id) {
            Monster mob = findMob(idMob);
            if (mob != null && !mob.isDie() && Util.getDistance(pl, mob) <= distance) {
                mobsNear.add(mob);
            }
        }
        return mobsNear;
    }

    @Synchronized
    public ItemMap findItemMap(short id) {
        return this.items.stream().filter(m -> m != null && m.getItemMapId() == id).findFirst().orElse(null);
    }

    public void update() throws IOException {
        for (int i = 0; i < mobs.size(); i++) {
            Monster mob = mobs.get(i);
            if (mob != null) {
                mob.update();
            }
        }
        for (int i = 0; i < items.size(); i++) {
            ItemMap itemMap = items.get(i);
            if (itemMap != null) {
                itemMap.update();
            }
        }
    }
}
