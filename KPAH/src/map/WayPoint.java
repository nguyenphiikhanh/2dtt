package map;

import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class WayPoint {

    private String nameWayPoint;
    private short toMap;
    private short toX;
    private short toY;

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(nameWayPoint);
        arr.put(toMap);
        arr.put(toX);
        arr.put(toY);
        return arr.toString();
    }
}
