package item;

import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ItemFriend {

    private byte classChar;
    private short idTemplate;
    private byte level;
    private byte plusTemplate;

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(idTemplate);
        arr.put(classChar);
        arr.put(level);
        arr.put(plusTemplate);
        return arr.toString();
    }
}
