package player;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NonNull;
import lombok.Synchronized;
import manager.Settings;
import player.Player;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
public class Party {

    private short idParty;
    private final short idLeader;
    private final List<Player> members;

    public Party(Player leader) {
        this.idParty = (short) -leader.getIdPlayer();
        this.idLeader = leader.getIdPlayer();
        this.members = new ArrayList<>();
        members.add(leader);
    }

    public Player getLeader() {
        return members.stream().filter(m -> m != null && m.getIdPlayer() == idLeader).findFirst().orElse(null);
    }

    public boolean isEmpty() {
        return members.size() == 1;
    }

    public boolean hasMember(short id) {
        return members.stream().anyMatch(m -> m != null && m.getIdPlayer() == id);
    }

    public Player getMember(short id) {
        return members.stream().filter(m -> m != null && m.getIdPlayer() == id).findFirst().orElse(null);
    }

    @Synchronized
    public void addMember(@NonNull Player member) {
        if (!members.contains(member)) {
            members.add(member);
        }
    }

    @Synchronized
    public void removeMember(@NonNull Player member) {
        if (members.contains(member)) {
            members.remove(member);
        }
    }

    public void dispose() {
        this.idParty = 0;
        this.members.clear();
    }
}
