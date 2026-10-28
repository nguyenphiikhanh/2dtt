package player;

import lombok.Builder;
import lombok.Data;
import map.Zone;
import org.json.JSONArray;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class Location {

    private short x;
    private short y;
    private byte mapVillage;
    private byte inCountry;
    private short lastX;
    private short lastY;
    private boolean stopCollectMessageMove;
    private Zone zone;
    private Zone lastZone;

    public void dispose() {
        zone = null;
        lastZone = null;
    }

    @Override
    public String toString() {
        JSONArray location = new JSONArray();
        if ((zone.getMap().isOfflineMap() || zone.getMap().getMapId() == 17) && zone.getMap().getMapId() != 105) {
            location.put(mapVillage);
            location.put(384);
            location.put(672);
            location.put(mapVillage == 70 ? 0 : 1);
        } else {
            location.put(zone.getMap().getMapId());
            location.put(x);
            location.put(y);
            location.put(inCountry);
        }
        return location.toString();
    }
}
