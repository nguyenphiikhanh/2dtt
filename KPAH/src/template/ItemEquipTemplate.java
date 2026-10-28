package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ItemEquipTemplate {

    private short id;
    private String name;
    private byte type;
    private byte style;
    private byte he;
    private byte gender;
    private byte level;
    private short durable;
    private short[] attribute;
    private int price;
    private byte classChar;
    private byte colorItem;
    private short idIcon;
    private short ndayLoan;
    private byte dxWear;
    private byte dyWear;
}
