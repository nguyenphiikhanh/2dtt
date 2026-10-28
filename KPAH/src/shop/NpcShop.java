package shop;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class NpcShop {

    private byte id;
    private String nameShop;
    private byte typeShop;
    private short[] idItems;
}
