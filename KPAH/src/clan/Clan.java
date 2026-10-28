package clan;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Synchronized;
import player.Friend;
import player.Player;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class Clan {

    private short indexIcon;
    private String nameLeader;
    private String name;
    private String slogan;
    private byte nationID;
    private byte level;
    private long xu;
    private long dedicationPoint;
    private long exp;
    @NonNull
    private List<Friend> members;
    @NonNull
    private List<Player> membersOnGame;
    @NonNull
    private List<ClanMessage> messages;
    private String date;
    private boolean dissolve;
    private long lastTimeEndDelete;
    private long lastTimeCreate;

    public boolean minusXu(int point) {
        if (point <= 0 || this.xu < point) {
            return false;
        }
        this.xu -= point;
        return true;
    }

    public boolean plusXu(int point) {
        if (point <= 0 || this.xu + point < 0) {
            return false;
        }
        this.xu += point;
        return true;
    }

    public boolean minusDedicationPoint(int point) {
        if (point <= 0 || this.dedicationPoint < point) {
            return false;
        }
        this.dedicationPoint -= point;
        return true;
    }

    public boolean plusDedicationPoint(int point) {
        if (point <= 0 || this.dedicationPoint + point < 0) {
            return false;
        }
        this.dedicationPoint += point;
        return true;
    }

    public Friend findClanMember(String name) {
        return members.stream().filter(f -> f != null && f.getName().equals(name)).findFirst().orElse(null);
    }

    public Friend findClanMember(int idDatabase) {
        return members.stream().filter(f -> f != null && f.getId() == idDatabase).findFirst().orElse(null);
    }

    public boolean hasMember(int idDatabase) {
        return members.stream().anyMatch(f -> f != null && f.getId() == idDatabase);
    }

    public byte getMaster(int idDatabase) {
        Friend mem = members.stream().filter(f -> f != null && f.getId() == idDatabase).findFirst().orElse(null);
        return mem != null ? mem.getIsMaster() : -1;
    }

    public Player findMemberOnGame(String name) {
        return membersOnGame.stream().filter(p -> p != null && p.getName().equals(name)).findFirst().orElse(null);
    }

    @Synchronized
    public void addMessage(@NonNull ClanMessage msg) {
        if (messages.size() > 50) {
            messages.remove(0);
        }
        messages.add(msg);
    }

    @Synchronized
    public void removeMessage(int index) {
        if (index >= messages.size() || index < 0) {
            return;
        }
        messages.remove(index);
    }

    @Synchronized
    public void addMemberOnGame(@NonNull Player member) {
        if (!membersOnGame.contains(member)) {
            membersOnGame.add(member);
        }
    }

    @Synchronized
    public void removeMemberOnGame(@NonNull Player member) {
        if (membersOnGame.contains(member)) {
            membersOnGame.remove(member);
        }
    }

    @Synchronized
    public void addMember(@NonNull Friend member) {
        if (!members.contains(member)) {
            members.add(member);
        }
    }

    @Synchronized
    public void removeMember(@NonNull Friend member) {
        if (members.contains(member)) {
            members.remove(member);
        }
    }

    public void dispose() {
        for (Player pl : membersOnGame) {
            pl.getInfo().setClan(null);
            pl.getSundry().setClanMember(null);
        }
        for (Friend f : members) {
            f.dispose();
        }
        messages.clear();
        members.clear();
        membersOnGame.clear();
        members = null;
        membersOnGame = null;
        messages = null;
    }
}
