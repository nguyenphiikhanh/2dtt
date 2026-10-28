package map;

import daos.PlayerDAO;
import interfaces.IMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import manager.ExecutorVirtualThread;
import manager.Manager;
import player.Player;
import skill.BuffInfluenceMonster;
import utils.Logger;

@Data
@Builder
public class Map implements IMap {

    private short id;
    private String name;
    private byte country;
    private List<Zone> zones;
    private List<ChildMap> childMaps;
    @NonNull
    private MapData mapData;

    public void initZone() {
        zones = new ArrayList<>();
        for (byte i = 0; i < (mapData.isOfflineMap() ? 1 : mapData.getMaxZone()); i++) {
            List<Monster> mobsNew = mapData.getMobsOrigin().stream().map(mob -> {
                Monster newMob = Monster.builder().id(mob.getId()).x(mob.getX()).hp(mob.getHp()).y(mob.getY()).template(mob.getTemplate()).playerAttack(new ArrayList<>()).build();
                newMob.setBuffInfluence(BuffInfluenceMonster.builder().build());
                newMob.getBuffInfluence().setMob(newMob);
                return newMob;
            }).collect(Collectors.toList());
            List<Player> playerDefault = mapData.getNpcsActor().stream().map(npc -> {
                Player player = PlayerDAO.buildNpcActor(Manager.getNpcTemplate((short) npc.getIdDatabase()), npc.getLocation().getX(), npc.getLocation().getY());
                player.setIdPlayer(npc.getIdPlayer());
                return player;
            }).collect(Collectors.toList());
            Zone z = Zone.builder().id(i).map(this).mobs(mobsNew).players(playerDefault).items(new ArrayList<>()).build();
            z.initMobZone();
            zones.add(z);
        }
    }

    public void initChildMap() {
        if (childMaps == null) {
            return;
        }
        for (int i = 0; i < childMaps.size(); i++) {
            ChildMap childMap = childMaps.get(i);
            if (childMap != null) {
                List<Zone> zonesNew = zones.stream().map(zone -> {
                    List<Monster> mobsNew = mapData.getMobsOrigin().stream().map(mob -> {
                        Monster newMob = Monster.builder().id(mob.getId()).x(mob.getX()).hp(mob.getHp()).y(mob.getY()).template(mob.getTemplate()).playerAttack(new ArrayList<>()).build();
                        newMob.setBuffInfluence(BuffInfluenceMonster.builder().build());
                        newMob.getBuffInfluence().setMob(newMob);
                        return newMob;
                    }).collect(Collectors.toList());
                    List<Player> playerDefault = mapData.getNpcsActor().stream().map(npc -> {
                        Player player = PlayerDAO.buildNpcActor(Manager.getNpcTemplate((short) npc.getIdDatabase()), npc.getLocation().getX(), npc.getLocation().getY());
                        player.setIdPlayer(npc.getIdPlayer());
                        return player;
                    }).collect(Collectors.toList());
                    Zone newZone = Zone.builder().id(zone.getId()).map(childMap).mobs(mobsNew).players(playerDefault).items(new ArrayList<>()).build();
                    newZone.initMobZone();
                    return newZone;
                }).collect(Collectors.toList());
                childMap.setZones(zonesNew);
                childMap.setMapParent(this);
                childMap.startUpdateMap();
            }
        }
    }

    @Override
    public boolean isChildMap() {
        return false;
    }

    @Override
    public short getMapId() {
        return id;
    }

    @Override
    public void startUpdateMap() {
        if (!mapData.isOfflineMap()) {
            ExecutorVirtualThread.submitThreadMap(update());
        }
    }

    @Override
    public Runnable update() {
        return () -> {
            try {
                while (true) {
                    if (zones != null && !zones.isEmpty()) {
                        for (Zone zone : Map.this.zones) {
                            if (zone != null) {
                                zone.update();
                            }
                        }
                    }
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (Exception e) {
                Logger.logError("Lỗi Update Map", e);
            }
        };
    }

    @Override
    public boolean isOfflineMap() {
        return mapData.isOfflineMap();
    }

    @Override
    public void setCountry(byte country) {
        this.country = country;
    }

    @Override
    public byte getCountry() {
        return country;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MapData getMapData() {
        return mapData;
    }

    @Override
    public IMap getMapParent() {
        return null;
    }

    @Override
    public List<ChildMap> getChildMaps() {
        return childMaps;
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
        return this.id == 0 || this.id == 70;
    }
}
