package services;

import interfaces.IMap;
import java.io.IOException;
import lombok.NonNull;
import manager.Manager;
import map.MapData;
import map.NpcServer;
import map.Zone;
import player.Player;
import template.XaPhuTemplate;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ChangeMapService {

    public static final ChangeMapService instance = new ChangeMapService();

    public void changeMap(@NonNull Player pl, Zone zone, short x, short y) throws IOException {
        changeMap(pl, zone.getMap().getMapId(), zone.getId(), x, y);
    }

    public void changeMap(@NonNull Player pl, short mapId, short x, short y) throws IOException {
        changeMap(pl, mapId, (byte) -1, x, y);
    }

    public void changeMap(@NonNull Player pl, short mapId) throws IOException {
        changeMap(pl, mapId, (byte) -1, pl.getLocation().getX(), pl.getLocation().getY());
    }

    public void changeMap(@NonNull Player pl, short mapId, byte zoneId, short x, short y) throws IOException {
        IMap mapCheck = Manager.getMap(pl.getLocation().getInCountry(), mapId);
        if (mapCheck == null) {
            Service.instance.sendLogOut(pl.getSession(), "Không thể chuyển map");
            return;
        }
        if (pl.getSundry().getTrader() != null) {
            TradeService.instance.cancelTrade(pl);
        }
        pl.getLocation().setLastZone(pl.getLocation().getZone());
        IMap mapLoadData = mapCheck.isChildMap() ? mapCheck.getMapParent() : mapCheck;
        Zone zone = MapService.instance.getValidZone(mapCheck, MapService.instance.getValidZoneId(zoneId, mapCheck));
        MapService.instance.playerJoinMap(pl, zone);
        short w = (short) (mapLoadData.getMapData().getW() * 30 / 100);
        short h = (short) (mapLoadData.getMapData().getH() * 30 / 100);
        pl.getLocation().setX((short) (x <= 0 ? Util.nextInt(w, mapLoadData.getMapData().getW() - w) * 16 + 8 : x));
        pl.getLocation().setY((short) (y <= 0 ? Util.nextInt(h, mapLoadData.getMapData().getH() - h) * 16 + 8 : y));
        short mapX = (short) ((pl.getLocation().getX() - 8) / 16);
        short mapY = (short) ((pl.getLocation().getY() - 8) / 16);
        MapService.instance.sendMove(pl);
        MapService.instance.sendDataMap(pl, mapCheck, mapX, mapY);
        MapData mapData = mapCheck.getMapData();
        for (int i = 0; i < mapData.getNpcServer().size(); i++) {
            NpcServer npcServer = mapData.getNpcServer().get(i);
            if (npcServer != null) {
                MapService.instance.sendNpcServer(pl, npcServer);
            }
        }
    }

    public void changeMapByXaPhu(@NonNull Player pl, byte index, short mapId) throws IOException {
        byte idXaphu = pl.getLocation().getZone().getMap().getMapData().getIdXaPhu();
        if (idXaphu == -1) {
            return;
        }
        XaPhuTemplate template = Manager.XA_PHU_TEMPLATE.getOrDefault(idXaphu, null);
        if (template == null) {
            return;
        }
        short price = template.getPrice()[index];
        short x = template.getX()[index];
        short y = template.getY()[index];
        if (!pl.getInventory().minusXu(price)) {
            Service.instance.sendLogOut(pl.getSession(), String.format("Không đủ %s xu", Util.formatNumber(price)));
            return;
        }
        changeMap(pl, mapId, (short) (x * 16 + 8), (short) (y * 16 + 8));
    }
}
