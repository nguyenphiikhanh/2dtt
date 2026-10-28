package manager;

import clan.Clan;
import database.HikariCP;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.Synchronized;
import utils.Logger;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ClanManager {

    public static ConcurrentHashMap<Short, Clan> CLANS = new ConcurrentHashMap<>();

    public static Runnable update() {
        return () -> {
            try {
                while (true) {
                    for (Short key : CLANS.keySet()) {
                        Clan clan = CLANS.getOrDefault(key, null);
                        if (clan != null && clan.getLastTimeEndDelete() != 0) {
                            int second = Util.getSecondDifference(clan.getLastTimeEndDelete(), System.currentTimeMillis());
                            if ((clan.isDissolve() && second <= 0) || (clan.getMembers().size() < 10 && second <= 0)) {
                                CLANS.remove(key).dispose();
                            }
                        }
                    }
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (Exception e) {
                Logger.logError("Lỗi Update Clan", e);
            }
        };
    }

    @Synchronized
    public static void saveDataClan() throws SQLException {
        HikariCP.execute("DELETE FROM `clan`");
        for (Short key : ClanManager.CLANS.keySet()) {
            Clan clan = ClanManager.CLANS.remove(key);
            HikariCP.execute(String.format("INSERT INTO `clan`(`nameLeader`, `name`, `slogan`, `indexIcon`, `nationID`, `level`, `exp`, `dedicationPoint`, `xu`, `members`, `dissolve`, `lastTimeCreate`, `lastTimeEndDelete`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')", clan.getNameLeader(), clan.getName(), clan.getSlogan(), clan.getIndexIcon(), clan.getNationID(), clan.getLevel(), clan.getExp(), clan.getDedicationPoint(), clan.getXu(), clan.getMembers().toString(), clan.isDissolve() ? 1 : 0, clan.getLastTimeCreate(), clan.getLastTimeEndDelete()));
        }
    }

    @Synchronized
    public static void addClan(@NonNull Clan cl) {
        if (!ClanManager.CLANS.contains(cl)) {
            ClanManager.CLANS.put(cl.getIndexIcon(), cl);
        }
    }

    @Synchronized
    public static void removeClan(@NonNull Clan cl) {
        if (ClanManager.CLANS.contains(cl)) {
            ClanManager.CLANS.remove(cl.getIndexIcon());
        }
    }

}
