package item;

import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import map.Zone;
import services.ItemService;
import consts.Const;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@AllArgsConstructor
public class ItemMap {

    private byte itemCatagory;
    private short itemTemplateID;
    private short itemMapId;
    private short quantity;
    private short x;
    private short y;
    private Zone zone;
    private long lastTimeCreate;
    private short idPlayerDrop;

    public void dispose() {
        zone = null;
    }

    public void update() throws IOException {
        int timeWait = 0;
        if (itemCatagory == Const.CATEGORY_ITEM || itemCatagory == Const.CATEGORY_GEM_ITEM) {
            timeWait = 25000;
        } else if (itemCatagory == Const.CATEGORY_POTION) {
            timeWait = 15000;
            timeWait = ((itemTemplateID < 10) ? timeWait : 60000);
        }
        if (idPlayerDrop != -1 && Util.canDoWithTime(lastTimeCreate, timeWait / 2)) {
            idPlayerDrop = -1;
        }
        if (Util.canDoWithTime(lastTimeCreate, timeWait)) {
            ItemService.instance.onRemoveItemMap(this);
            zone.removeItem(this);
        }
    }
}
