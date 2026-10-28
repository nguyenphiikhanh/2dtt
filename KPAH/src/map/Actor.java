package map;

import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;

@Data
@Builder
public class Actor {

    private short x;

    private short y;

    private int sizeX;

    private int sizeY;

    private int plusY;

    private int index;

    private short[] indexs;

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        if (plusY == -1) {
            arr.put(index);
            arr.put(x);
            arr.put(y);
        } else {
            try {
                arr.put(new JSONArray(indexs));
                arr.put(x);
                arr.put(sizeX);
                arr.put(sizeY);
                arr.put(plusY);
            } catch (JSONException ex) {
            }
        }
        return arr.toString();
    }
}
