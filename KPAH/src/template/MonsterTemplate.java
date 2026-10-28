package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class MonsterTemplate {

    private short id;
    private String name;
    private byte type;
    private int maxHp;
    private short level;
    private byte palate;
    private byte spalate;
    private byte moveType;
    private byte speed;
    private byte height;
    private byte w;
    private byte h;
    private byte xCenter;
    private byte yCenter;

    private boolean isNewMonster;

}
