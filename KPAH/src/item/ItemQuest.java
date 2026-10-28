package item;

import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import template.ItemQuestTemplate;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ItemQuest {

    private ItemQuestTemplate template;
    private short quantity;

    public void dispose() {
        template = null;
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(template.getId());
        arr.put(quantity);
        return arr.toString();
    }
}
