package skill;

import java.io.IOException;
import lombok.Builder;
import lombok.Data;
import lombok.Synchronized;
import player.Player;
import services.BuffService;
import consts.BuffConst;
import consts.ItemEquipConst;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class BuffInfluencePlayer {

    private Player player;

    private boolean isPoisoned;
    private short docTo;
    private short secondOfPoisoned;
    private long lastTimePoisoned;
    private long lastTimeMinusHp;

    private boolean isStunned;
    private short secondOfStunned;
    private long lastTimeStunned;

    @Synchronized
    public void addBuffPoisoned(short time, short docto) throws IOException {
        if (isPoisoned) {
            return;
        }
        isPoisoned = true;
        secondOfPoisoned = time;
        docTo = docto;
        lastTimePoisoned = System.currentTimeMillis();
        lastTimeMinusHp = System.currentTimeMillis();
        BuffService.instance.sendAddBuffInfluence(this.player, BuffConst.BUFF_DOC_TO);
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
        BuffService.instance.sendRemoveBuffInfluence(this.player, BuffConst.BUFF_DOC_TO);
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
        BuffService.instance.sendAddBuffInfluence(this.player, BuffConst.BUFF_STUN);
    }

    @Synchronized
    public void removeBuffStunned() throws IOException {
        if (!isStunned) {
            return;
        }
        isStunned = false;
        secondOfStunned = 0;
        lastTimeStunned = 0;
        BuffService.instance.sendRemoveBuffInfluence(this.player, BuffConst.BUFF_STUN);
    }

    public void dispose() {
        this.player = null;
    }

    public void update() throws IOException {
        if (isStunned && (Util.canDoWithTime(lastTimeStunned, secondOfStunned * 1000) || player.isDie())) {
            removeBuffStunned();
        }
        if (isPoisoned && (Util.canDoWithTime(lastTimePoisoned, secondOfPoisoned * 1000) || player.isDie())) {
            removeBuffPoisoned();
        }
        if (isPoisoned && !player.isDie() && Util.canDoWithTime(lastTimeMinusHp, BuffConst.SECOND_SUB_HP_DOC_TO * 1000)) {
            lastTimeMinusHp = System.currentTimeMillis();
            short dame = (short) player.injured(docTo, true, ItemEquipConst.DAMAGE_MAGIC, false);
            if (dame > 0) {
                BuffService.instance.sendSubHpByBuffInfluence(player, dame);
            }
        }
    }
}
