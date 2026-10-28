package manager;

import clan.Clan;
import daos.PlayerDAO;
import database.HikariCP;
import database.ResultSetImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static manager.ClanManager.CLANS;
import static manager.ClanManager.saveDataClan;
import org.json.JSONArray;
import org.json.JSONException;
import top.ClanInfo;
import top.TopPlayer;
import utils.Logger;
import utils.Printer;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class TopManager {

    public static List<ClanInfo> TOP_CLANS = new ArrayList<>();
    public static List<TopPlayer> TOP_STRONGER = new ArrayList<>();
    public static List<TopPlayer> TOP_RIGHER = new ArrayList<>();

    public static Runnable updateTopClan() {
        return () -> {
            try {
                while (true) {
                    dispose();
                    loadTop();
                    Printer.printRed("Load Top Data");
                    saveDataClan();
                    TimeUnit.MINUTES.sleep(5);
                }
            } catch (Exception e) {
                Logger.logError("Lỗi Update Top", e);
            }
        };
    }

    private static void dispose() {
        for (TopPlayer p : TOP_STRONGER) {
            p.dispose();
        }
        TOP_STRONGER.clear();
        for (TopPlayer p : TOP_RIGHER) {
            p.dispose();
        }
        TOP_RIGHER.clear();
        TOP_CLANS.clear();
    }

    private static void loadTop() throws SQLException, JSONException {
        ResultSetImpl rs = HikariCP.executeQuery("SELECT `id`,`name`,`itemBody`,`level`,`idNation`,`head`,`idClan`,`xu`, `luong` FROM (SELECT `id`,`name`,`itemBody`,JSON_EXTRACT(`info`, '$[5]') AS `level`,JSON_EXTRACT(`info`, '$[3]') AS `idNation`,JSON_EXTRACT(`info`, '$[1]') AS `head`,JSON_EXTRACT(`info`, '$[6]') AS `idClan`,JSON_EXTRACT(`inventory`, '$[2]') AS `xu`,JSON_EXTRACT(`inventory`, '$[0]') AS `luong` FROM `players`) AS `inventory_sort` ORDER BY `xu` DESC LIMIT 10");
        while (rs.next()) {
            int id = Integer.parseInt(rs.getString("id"));
            String name = rs.getString("name");
            String itemBody = rs.getString("itemBody");
            byte level = Byte.parseByte(rs.getString("level"));
            byte idNation = Byte.parseByte(rs.getString("idNation"));
            byte head = Byte.parseByte(rs.getString("head"));
            short idClan = Short.parseShort(rs.getString("idClan"));
            long xu = Long.parseLong(rs.getString("xu"));
            int luong = Integer.parseInt(rs.getString("luong"));
            byte isMaster = -1;
            Clan clan = Manager.getClan(idClan);
            if (clan == null || !clan.hasMember(id)) {
                idClan = -1;
            } else {
                isMaster = clan.getMaster(id);
            }
            TopPlayer top = TopPlayer.builder().name(name).head(head).level(level).idClan(idClan).xu(xu).luong(luong).nationId(idNation).isMaster(isMaster).items(PlayerDAO.loadDataItemFriend(new JSONArray(itemBody))).build();
            TOP_RIGHER.add(top);
        }
        rs.close();
        rs = HikariCP.executeQuery("SELECT `id`,`name`,`itemBody`,`level`,`idNation`,`head`,`idClan`,`xu`, `luong` FROM (SELECT `id`,`name`,`itemBody`,JSON_EXTRACT(`info`, '$[5]') AS `level`,JSON_EXTRACT(`info`, '$[3]') AS `idNation`,JSON_EXTRACT(`info`, '$[1]') AS `head`,JSON_EXTRACT(`info`, '$[6]') AS `idClan`,JSON_EXTRACT(`inventory`, '$[2]') AS `xu`,JSON_EXTRACT(`inventory`, '$[0]') AS `luong` FROM `players`) AS `inventory_sort` ORDER BY `level` DESC LIMIT 10");
        while (rs.next()) {
            int id = Integer.parseInt(rs.getString("id"));
            String name = rs.getString("name");
            String itemBody = rs.getString("itemBody");
            byte level = Byte.parseByte(rs.getString("level"));
            byte idNation = Byte.parseByte(rs.getString("idNation"));
            byte head = Byte.parseByte(rs.getString("head"));
            short idClan = Short.parseShort(rs.getString("idClan"));
            long xu = Long.parseLong(rs.getString("xu"));
            int luong = Integer.parseInt(rs.getString("luong"));
            byte isMaster = -1;
            Clan clan = Manager.getClan(idClan);
            if (clan == null || !clan.hasMember(id)) {
                idClan = -1;
            } else {
                isMaster = clan.getMaster(id);
            }
            TopPlayer top = TopPlayer.builder().name(name).head(head).level(level).idClan(idClan).xu(xu).luong(luong).nationId(idNation).isMaster(isMaster).items(PlayerDAO.loadDataItemFriend(new JSONArray(itemBody))).build();
            TOP_STRONGER.add(top);
        }
        rs.close();
        CLANS.values().stream()
                .sorted(Comparator.comparing(Clan::getLevel).thenComparing(Clan::getXu))
                .limit(10).forEach(clan -> {
            ClanInfo clanInfo = ClanInfo.builder().indexIcon(clan.getIndexIcon()).level(clan.getLevel()).members((short) clan.getMembers().size()).nationId(clan.getNationID()).name(clan.getName()).xu(clan.getXu()).nameLeader(clan.getNameLeader()).build();
            TOP_CLANS.add(clanInfo);
        });
    }
}
