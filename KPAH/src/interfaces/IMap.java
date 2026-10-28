package interfaces;

import java.util.List;
import map.ChildMap;
import map.MapData;
import map.Zone;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public interface IMap {

    short getMapId();

    String getName();

    boolean isOfflineMap();

    boolean isChildMap();

    byte getCountry();

    void setCountry(byte country);

    void startUpdateMap();

    MapData getMapData();

    Runnable update();

    IMap getMapParent();

    List<ChildMap> getChildMaps();

    List<Zone> getZones();

    Zone getZone(int index);

    boolean isMapVillage();
}
