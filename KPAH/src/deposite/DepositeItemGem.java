package deposite;

import item.ItemGem;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@AllArgsConstructor
public class DepositeItemGem {

    private short idReal;
    private ItemGem item;
    private String nameDeposite;
    private int priceDeposite;
    private String playerCanBuy;

    public void dispose() {
        priceDeposite = -1;
        playerCanBuy = "";
        nameDeposite = "";
        item = null;
    }
}
