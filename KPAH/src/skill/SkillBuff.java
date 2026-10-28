package skill;

import java.io.IOException;
import lombok.Builder;
import lombok.Data;
import lombok.Synchronized;
import manager.Manager;
import player.Player;
import services.BuffService;
import services.MapService;
import consts.BuffConst;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class SkillBuff {

    private Player player;
    @Builder.Default
    private final byte[] idBuff = new byte[]{-1, -1, -1, -1, -1, -1, -1};
    @Builder.Default
    private final short[] coolDown = new short[]{-1, -1, -1, -1, -1, -1, -1};
    @Builder.Default
    private final long[] lastTimeStartBuff = new long[]{-1, -1, -1, -1, -1, -1, -1};
    @Builder.Default
    private final short[] percentDame = new short[]{-1, -1, -1, -1, -1, -1, -1};

    @Synchronized
    public boolean isExistBuff(byte id) {
        for (byte buff : idBuff) {
            if (buff == id) {
                return true;
            }
        }
        return false;
    }

    @Synchronized
    public short getPercentDame(byte id) {
        int index;
        for (index = 0; index < Manager.BUFF_TYPE.length && Manager.BUFF_TYPE[index] != id; index++) {
        }
        return percentDame[index];
    }

    @Synchronized
    public void addBuff(byte id, short cooldown, short percent) throws IOException {
        int index;
        for (index = 0; index < Manager.BUFF_TYPE.length && Manager.BUFF_TYPE[index] != id; index++) {
        }
        this.idBuff[index] = id;
        this.coolDown[index] = cooldown;
        this.percentDame[index] = percent;
        this.lastTimeStartBuff[index] = System.currentTimeMillis();
        if (id == BuffConst.HOI_CONG_LUC_DAN) {
            player.getPoint().initPoint();
            MapService.instance.onNewHpMp(player);
        }
    }

    @Synchronized
    public void clearBuff() throws IOException {
        for (int i = 0; i < idBuff.length; i++) {
            byte idBuffRemove = this.idBuff[i];
            this.idBuff[i] = -1;
            this.coolDown[i] = -1;
            this.lastTimeStartBuff[i] = -1;
            BuffService.instance.sendRemoveUseBuff(player, idBuffRemove);
        }
    }

    public void update() throws IOException {
        for (int i = 0; i < idBuff.length; i++) {
            short secondCooldown = coolDown[i];
            if (Util.canDoWithTime(lastTimeStartBuff[i], secondCooldown * 1000)) {
                byte idBuffRemove = this.idBuff[i];
                this.idBuff[i] = -1;
                this.coolDown[i] = -1;
                this.lastTimeStartBuff[i] = -1;
                if (idBuffRemove == BuffConst.HOI_CONG_LUC_DAN) {
                    player.getPoint().initPoint();
                    MapService.instance.onNewHpMp(player);
                }
                BuffService.instance.sendRemoveUseBuff(player, idBuffRemove);
            }
        }
    }
}
