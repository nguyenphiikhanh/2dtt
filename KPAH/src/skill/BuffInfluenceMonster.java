package skill;

import java.io.IOException;
import lombok.Builder;
import lombok.Data;
import lombok.Synchronized;
import map.Monster;
import player.Player;
import services.BuffService;
import consts.BuffConst;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class BuffInfluenceMonster {

    private Monster mob;
    private Player playerUser;

    private boolean isPoisoned;
    private short docTo;
    private short secondOfPoisoned;
    private long lastTimePoisoned;
    private long lastTimeMinusHp;

    private boolean isStunned;
    private short secondOfStunned;
    private long lastTimeStunned;

    @Synchronized
    public void addBuffPoisoned(Player player, short time, short docto) throws IOException {
        if (isPoisoned) {
            return;
        }
        isPoisoned = true;
        secondOfPoisoned = time;
        docTo = docto;
        lastTimePoisoned = System.currentTimeMillis();
        playerUser = player;
        lastTimeMinusHp = System.currentTimeMillis();
        BuffService.instance.sendAddBuffInfluence(mob, BuffConst.BUFF_DOC_TO);
    }

    @Synchronized
    public void removeBuffPoisoned() throws IOException {
        if (!isPoisoned) {
            return;
        }
        isPoisoned = false;
        secondOfPoisoned = 0;
        lastTimePoisoned = 0;
        docTo = 0;
        playerUser = null;
        BuffService.instance.sendRemoveBuffInfluence(mob, BuffConst.BUFF_DOC_TO);
    }

    @Synchronized
    public byte getSecondPosonedLeft() {
        return (byte) (secondOfPoisoned - Util.getSecondDifference(System.currentTimeMillis(), lastTimePoisoned));
    }

    @Synchronized
    public void addBuffStunned(short time) throws IOException {
        if (isStunned) {
            return;
        }
        isStunned = true;
        secondOfStunned = time;
        lastTimeStunned = System.currentTimeMillis();
    }

    @Synchronized
    public void removeBuffStunned() throws IOException {
        if (!isStunned) {
            return;
        }
        isStunned = false;
        secondOfStunned = 0;
        lastTimeStunned = 0;
        playerUser = null;
    }

    public void clearBuff() throws IOException {
        if (isPoisoned) {
            removeBuffPoisoned();
            BuffService.instance.sendRemoveBuffInfluence(mob, BuffConst.BUFF_DOC_TO);
        }
        if (isStunned) {
            removeBuffStunned();
            BuffService.instance.sendRemoveBuffInfluence(mob, BuffConst.BUFF_STUN);
        }
    }

    public void update() throws IOException {
        if (isStunned && (Util.canDoWithTime(lastTimeStunned, secondOfStunned * 1000) || mob.isDie())) {
            removeBuffStunned();
        }
        if (isPoisoned && (Util.canDoWithTime(lastTimePoisoned, secondOfPoisoned * 1000) || mob.isDie())) {
            removeBuffPoisoned();
        }
        if (isPoisoned && !mob.isDie() && Util.canDoWithTime(lastTimeMinusHp, BuffConst.SECOND_SUB_HP_DOC_TO * 1000)) {
            lastTimeMinusHp = System.currentTimeMillis();
            short dame = (short) mob.injured(playerUser, docTo, false, true, false);
            if (dame > 0) {
                BuffService.instance.sendSubHpByBuffInfluence(mob, dame);
            }
        }
    }
}
