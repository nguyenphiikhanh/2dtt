package item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import template.AttributeEquipTemplate;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
@AllArgsConstructor
public class Attribute {

    private AttributeEquipTemplate template;
    private short value;

    public void dispose() {
        template = null;
    }

    public void plusValue(short plus) {
        if (plus <= 0 || value + plus <= 0) {
            return;
        }
        value += plus;
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(template.getId());
        arr.put(value);
        return arr.toString();
    }
}
