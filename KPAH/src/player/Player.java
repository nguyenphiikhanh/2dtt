package player;

import skill.SkillBuff;
import daos.PlayerDAO;
import interfaces.ISession;
import item.ItemEquip;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Synchronized;
import manager.Manager;
import manager.Settings;
import services.ChatService;
import services.InventoryService;
import services.MapService;
import services.PartyService;
import services.TradeService;
import skill.BuffInfluencePlayer;
import consts.BuffConst;
import consts.Const;
import consts.ItemEquipConst;
import deposite.Deposite;
import services.ChangeMapService;
import utils.Logger;
import utils.Util;

@Data
@Builder
public class Player {

    private ISession session;
    private int idDatabase;
    private short idPlayer;
    private boolean isNpc;
    private String name;
    @NonNull
    private Info info;
    @NonNull
    private Location location;
    @NonNull
    private Point point;
    @NonNull
    private Inventory inventory;
    @NonNull
    private Skill skill;
    @NonNull
    private Sundry sundry;
    @NonNull
    private SkillBuff skillBuff;
    @NonNull
    private BuffInfluencePlayer buffInfluence;
    @NonNull
    private Horse horse;
    private List<Friend> friends;
    private Party party;
    private List<Short> otherMobInside;
    private List<Short> otherItemMapInside;
    private List<Short> otherPlayerInside;

    public void setUp() {
        sundry.setLastTimeUpdateDatabase(System.currentTimeMillis());
        this.sundry.setInGame(true);
        this.otherMobInside = new ArrayList<>();
        this.otherItemMapInside = new ArrayList<>();
        this.otherPlayerInside = new ArrayList<>();
        this.party = new Party(this);
        this.point.setPlayer(this);
        this.skillBuff.setPlayer(this);
        this.buffInfluence.setPlayer(this);
        this.point.initPoint();
        if (this.info.getClan() != null) {
            this.info.getClan().addMemberOnGame(this);
            Friend clanMember = this.info.getClan().findClanMember(idDatabase);
            this.sundry.setClanMember(clanMember);
        }
        if (info.getKiller() > 0) {
            sundry.setKiller(true);
        }
    }

    @Synchronized
    public int injured(int damage, boolean isInjuredByEffect, byte typeDame, boolean x2) throws IOException {
        if (!this.isDie()) {
            int def = (typeDame == ItemEquipConst.DAMAGE_MAGIC ? this.point.getDefendMagic() : this.point.getDefend());
            byte classPlayer = this.info.getClassPlayer();
            byte skillLevel = this.skill.getLevelSkill()[classPlayer == Const.DAU_SI ? 5 : 4];
            if (classPlayer == Const.DAU_SI) {
                def += def * (Manager.getSkillDamPercent(classPlayer, BuffConst.BUFF_PHONG_THU, skillLevel) / 100);
            }
            if (classPlayer == Const.CHIEN_BINH && skillBuff.isExistBuff(BuffConst.CUONG_THAN_GIAP)) {
                def += def * skillBuff.getPercentDame(BuffConst.CUONG_THAN_GIAP) / 100;
            }
            damage -= def;
            if (Util.isTrue((double) this.point.getHapThu(), 100.0)) {
                damage -= damage * this.point.getHapThu() / 100;
            }
            if (typeDame == ItemEquipConst.DAMAGE_MAGIC) {
                damage -= damage * this.point.getGiamStMa() / 100;
            } else {
                damage -= damage * this.point.getGiamStVat() / 100;
            }
            if (damage < 0) {
                damage = 1;
            }
            if (isInjuredByEffect && this.buffInfluence.isPoisoned()) {
                if (this.point.getHp() - damage < 1) {
                    damage = this.point.getHp() - 1;
                }
            }
            for (int i = 0; i < this.inventory.getItemBody().size(); i++) {
                ItemEquip item = this.inventory.getItemBody().get(i);
                if ((item.getTemplate().getType() == 0 || item.getTemplate().getType() == 1 || item.getTemplate().getType() == 2) && item.getMDurable() > 0) {
                    item.minusDurable();
                }
            }
            if (x2) {
                this.point.minusHp(damage);
            }
            this.point.minusHp(damage);
            if (this.isDie()) {
                skillBuff.clearBuff();
                sundry.plusCountDie();
                sundry.setMiliSecondRevive(Settings.MILISECOND_REVIVE_PLAYER * sundry.getCountDie());
                sundry.setLastTimeDie(System.currentTimeMillis());
                MapService.instance.onPlayerDie(this);
            }
            if (this.sundry.isComeHome()) {
                this.sundry.setComeHome(false);
            }
        }
        return damage;
    }

    public boolean isPlayer() {
        return !isNpc;
    }

    public boolean isDie() {
        return point.getHp() <= 0;
    }

    public short getHead() {
        return info.getHead();
    }

    public short getBody() {
        ItemEquip item = InventoryService.instance.findItemBodyByType(this, (byte) 0);
        if (item != null) {
            return item.getTemplate().getStyle();
        }
        if (info.getGender() == Const.MALE) {
            return 2;
        }
        return 3;
    }

    public short getLeg() {
        ItemEquip item = InventoryService.instance.findItemBodyByType(this, (byte) 1);
        if (item != null) {
            return item.getTemplate().getStyle();
        }
        if (info.getGender() == Const.MALE) {
            return 2;
        }
        return 3;
    }

    public short getHat() {
        ItemEquip item = InventoryService.instance.findItemBodyByType(this, (byte) 2);
        if (item != null) {
            return item.getTemplate().getStyle();
        }
        return -1;
    }

    public short getWeaponStyle() {
        ItemEquip item = InventoryService.instance.findItemBodyByType(this, (byte) (3 + info.getClassPlayer()));
        if (item != null) {
            return item.getTemplate().getStyle();
        }
        return -1;
    }

    public short getCoat() {
        ItemEquip item = InventoryService.instance.findItemBodyByType(this, (byte) 19);
        if (item != null) {
            return item.getTemplate().getStyle();
        }
        return -1;
    }

    public Runnable updatePlayer() {
        return () -> {
            try {
                while (Player.this.session != null && Player.this.session.isConnected() && Player.this.isPlayer()) {
                    long st = System.currentTimeMillis();
                    if (sundry.isInGame()) {
                        Player.this.update();
                    }
                    TimeUnit.MILLISECONDS.sleep(Math.abs(1000 - (System.currentTimeMillis() - st)));
                }
            } catch (Exception e) {
                Logger.logError("Lỗi Update Player", e);
            }
        };
    }

    public void update() throws IOException, SQLException {
        buffInfluence.update();
        skillBuff.update();
        if (isDie() && this.info.getLevel() < Settings.LEVEL_CAN_AUTO_REVIVE) {
            if (Util.canDoWithTime(sundry.getLastTimeDie(), sundry.getMiliSecondRevive())) {
                MapService.instance.revivePlayer(this, (byte) 100);
                sundry.setLastTimeRevived(System.currentTimeMillis());
            } else {
                ChatService.instance.sendChatOnlyMe(this, "Hồi sinh sau " + (sundry.getMiliSecondRevive() / 1000 - Util.getSecondDifference(System.currentTimeMillis(), sundry.getLastTimeDie())) + "s");
            }
        }
        if (sundry.isNewlyRevived() && Util.canDoWithTime(sundry.getLastTimeRevived(), 5000)) {
            sundry.setNewlyRevived(false);
        }
        if (Util.canDoWithTime(sundry.getLastTimeUpdateDatabase(), Settings.MILISECOND_UPDATE_DATABASE)) {
            sundry.setLastTimeUpdateDatabase(System.currentTimeMillis());
            PlayerDAO.updatePlayer(this);
        }
        if (sundry.isOnBoard() && Util.canDoWithTime(sundry.getLastTimeOnBoard(), 2000)) {
            sundry.setOnBoard(false);
            ChangeMapService.instance.changeMap(this, (short) 10, (short) 1015, (short) 2020);
        }
    }

    public void dispose() throws IOException, SQLException {
        if (otherMobInside != null) {
            otherMobInside.clear();
        }
        if (otherPlayerInside != null) {
            otherPlayerInside.clear();
        }
        if (otherItemMapInside != null) {
            otherItemMapInside.clear();
        }
        if (party != null && !party.isEmpty()) {
            PartyService.instance.leaverParty(this);
            if (this.party.getIdLeader() == idPlayer) {
                PartyService.instance.disbandParty(this, true);
            }
            party = null;
        }
        if (sundry.isInGame()) {
            if (this.sundry.getTrader() != null) {
                TradeService.instance.cancelTrade(this);
            }
            if (this.info.getClan() != null) {
                this.info.getClan().removeMemberOnGame(this);
                this.sundry.getClanMember().update(this);
            }
            sundry.dispose();
            MapService.instance.exitMap(this);
            PlayerDAO.updatePlayer(this);
        }
        if (friends != null) {
            for (Friend f : friends) {
                f.dispose();
            }
            friends.clear();
        }
        if (inventory != null) {
            inventory.dispose();
        }
        if (location != null) {
            location.dispose();
        }
        if (buffInfluence != null) {
            buffInfluence.dispose();
        }
        if (horse != null) {
            horse = null;
        }
    }
}
