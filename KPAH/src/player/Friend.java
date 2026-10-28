package player;

import item.ItemEquip;
import item.ItemFriend;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;
import services.ItemService;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Builder
@Data
public class Friend {

    private int id;
    private String name;
    private byte head;
    private byte level;
    private short idClan;
    private byte isMaster;
    private List<ItemFriend> items;

    public void update(Player player) {
        this.name = player.getName();
        this.head = player.getInfo().getHead();
        this.level = player.getInfo().getLevel();
        this.idClan = player.getInfo().getClan() == null ? - 1 : player.getInfo().getClan().getIndexIcon();
        this.isMaster = player.getSundry().getClanMember().isMaster;
        this.items.clear();
        for (ItemEquip item : player.getInventory().getItemBody()) {
            items.add(ItemService.instance.createNewItemFriend(item));
        }
    }

    public void dispose() {
        items.clear();
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(id);
        arr.put(name);
        arr.put(head);
        arr.put(level);
        arr.put(idClan);
        arr.put(isMaster);
        try {
            arr.put(new JSONArray(items.toString()));
        } catch (JSONException ex) {
        }
        return arr.toString();
    }
}
