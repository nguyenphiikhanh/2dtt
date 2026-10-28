package item;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@AllArgsConstructor
@Data
public class ItemBuyNpc {

    private byte category;
    private short idItem;
    private short quantity;
    private byte classChar;
}
