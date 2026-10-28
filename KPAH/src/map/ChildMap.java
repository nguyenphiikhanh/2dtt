package map;

import interfaces.IMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Data;
import manager.ExecutorVirtualThread;
import utils.Logger;
import utils.Printer;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ChildMap implements IMap {

    private short id;
    private String name;
    private List<Zone> zones;
    private Map mapParent;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isChildMap() {
        return true;
    }

    @Override
    public MapData getMapData() {
        return mapParent.getMapData();
    }

    @Override
    public short getMapId() {
        return id;
    }

    @Override
    public boolean isOfflineMap() {
        return mapParent.getMapData().isOfflineMap();
    }

    @Override
    public void setCountry(byte country) {
    }

    @Override
    public byte getCountry() {
        return mapParent.getCountry();
    }

    @Override
    public void startUpdateMap() {
        if (!isOfflineMap()) {
            ExecutorVirtualThread.submitThreadMap(update());
        }
    }

    @Override
    public Runnable update() {
        return () -> {
            try {
                while (true) {
                    if (zones != null && !zones.isEmpty()) {
                        for (Zone zone : ChildMap.this.zones) {
                            if (zone != null) {
                                zone.update();
                            }
                        }
                    }
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (Exception e) {
                Logger.logError("Lỗi Update Child Map", e);
            }
        };
    }

    @Override
    public List<ChildMap> getChildMaps() {
        return null;
    }

    @Override
    public IMap getMapParent() {
        return mapParent;
    }

    @Override
    public Zone getZone(int index) {
        return zones.get(index);
    }

    @Override
    public List<Zone> getZones() {
        return zones;
    }

    @Override
    public boolean isMapVillage() {
        return (this.id >= 301 && this.id <= 304) || (this.id >= 1701 && this.id <= 1704);
    }
}
