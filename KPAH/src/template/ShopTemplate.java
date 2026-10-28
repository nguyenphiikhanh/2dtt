package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ShopTemplate {

    private byte shopType;
    private String name;
    private String decript;
    private short idImage;
    private short id;
    private byte typeMoney;
    private boolean isSell;
    private short value;
    private int price;
    private short idItemEquip;
    private short idItemPotion;
    private short idItemGem;
    private boolean isGemLock;
}
