package map;

import item.ItemMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Builder;
import template.MonsterTemplate;
import lombok.Data;
import lombok.NonNull;
import lombok.Synchronized;
import manager.ClientManager;
import manager.Manager;
import manager.Settings;
import player.Player;
import services.BuffService;
import services.ItemService;
import services.MapService;
import services.MonsterService;
import services.Service;
import skill.BuffInfluenceMonster;
import consts.Const;
import utils.Util;

@Builder
@Data
public class Monster implements Cloneable {

    private short id;
    private short x, y;
    private int hp;
    private Zone zone;
    private MonsterTemplate template;
    private List<Player> playerAttack;
    private BuffInfluenceMonster buffInfluence;
    private long lastTimeDie;
    private long lastTimeAttackPlayer;
    private Player playerTarget;

    public boolean isDie() {
        return this.hp <= (isKhoangSan() ? 10 : 0);
    }

    public boolean playerCanNotAttack() {
        return template.getId() >= 36 && template.getId() <= 46;
    }

    public boolean canNotAttackPlayer() {
        return (template.getId() >= 85 && template.getId() <= 89) || (template.getId() >= 36 && template.getId() <= 46);
    }

    public boolean isKhoangSan() {
        return template.getId() >= 85 && template.getId() <= 89;
    }

    private boolean isPlayerAttackable(@NonNull Player player) {
        return !player.getSundry().isNewlyRevived() && !player.isDie() && ClientManager.containsPlayers(player) && player.getLocation().getZone().equals(this.zone) && Util.getDistance(player, this) <= Settings.DISTANCE_MOB_CAN_ATTACK;
    }

    public int injured(@NonNull Player plAtt, int damage, boolean isXuyenGiap, boolean isInjuredByEffect, boolean x2) throws IOException {
        if (!this.isDie()) {
            if (!isKhoangSan()) {
                if (this.template.getLevel() - 2 > plAtt.getInfo().getLevel()) {
                    damage /= this.template.getLevel() - plAtt.getInfo().getLevel() + 2;
                }
                if (!isXuyenGiap) {
                    damage -= this.template.getMaxHp() * 0.02;
                }
            }
            if (damage < 0) {
                damage = 0;
            }
            if (isInjuredByEffect && this.buffInfluence.isPoisoned()) {
                if (hp - damage < 1) {
                    damage = hp - 1;
                }
            }
            if (!isKhoangSan()) {
                BuffService.instance.onMobInjured(plAtt, this);
            }
            if (x2) {
                this.minusHp(plAtt, damage);
            }
            this.minusHp(plAtt, damage);
        }
        return damage;
    }

    @Synchronized
    public void minusHp(Player plAtt, int damage) throws IOException {
        if (isDie()) {
            return;
        }
        this.hp -= damage;
        this.addPlayerAttack(plAtt);
        if (!isKhoangSan()) {
            calculatePowerPlus(plAtt, damage);
        }
        if (this.isDie()) {
            if (this.template.getLevel() - plAtt.getInfo().getLevel() >= 10) {
                plAtt.getInfo().minusKiller((byte) 1);
                if (plAtt.getInfo().getKiller() <= 0) {
                    plAtt.getSundry().setKiller(false);
                }
                MapService.instance.sendKiller(plAtt);
            }
            MonsterService.instance.onMonsterDropItem(this, plAtt);
            this.lastTimeDie = System.currentTimeMillis();
            this.playerTarget = null;
            this.playerAttack.clear();
            buffInfluence.clearBuff();
        }
    }

    @Synchronized
    private void getPlayerCanAttack() throws IOException {
        if (playerTarget != null) {
            if (!isPlayerAttackable(playerTarget)) {
                playerTarget = null;
                MonsterService.instance.sendMonsterEndAttack(this);
            }
        } else {
            Iterator<Player> iterator = playerAttack.iterator();
            while (iterator.hasNext()) {
                Player pl = iterator.next();
                if (pl == null || !isPlayerAttackable(pl)) {
                    iterator.remove();
                } else {
                    playerTarget = pl;
                }
            }
            if (this.template.getLevel() > 7) {
                for (Player pl : zone.getPlayers()) {
                    if (!pl.isDie() && isPlayerAttackable(pl)) {
                        playerTarget = pl;
                    }
                }
            }
        }
    }

    @Synchronized
    private void addPlayerAttack(@NonNull Player pl) {
        if (playerAttack.contains(pl)) {
            return;
        }
        if (playerAttack.size() > 10) {
            Iterator<Player> iterator = playerAttack.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        playerAttack.add(pl);
    }

    public int getDameAttack(@NonNull Player pl) {
        double d = this.template.getMaxHp() * 0.2;
        int dameAtt = Util.nextInt((int) (d * 0.9), (int) (d * 1.2));
        if (dameAtt <= 0) {
            dameAtt = 1;
        }
        if (this.template.getLevel() - 2 < pl.getInfo().getLevel()) {
            dameAtt /= pl.getInfo().getLevel() - this.template.getLevel() + 2;
        }
        return dameAtt;
    }

    @Synchronized
    public void calculatePowerPlus(@NonNull Player pl, int damage) throws IOException {
        int a = pl.getInfo().getLevel() / 20;
        a = a == 0 ? 1 : a;
        int b = damage * a;
        int c = (this.template.getLevel() - pl.getInfo().getLevel()) * a;
        int tnPl = (b / 2) + (b * c / 100);
        tnPl += tnPl * (pl.getPoint().getExpDonate() / 100);
        byte defferenceLevel = (byte) Math.abs(pl.getInfo().getLevel() - this.template.getLevel());
        if (defferenceLevel >= 2) {
            tnPl /= defferenceLevel + 3;
        }
        if (tnPl <= 0) {
            tnPl = 1;
        }
        tnPl *= Settings.EXP_DONATE;
        tnPl = (int) Math.max(1, Util.nextDouble(tnPl * 0.6, tnPl * 0.9));
        pl.getPoint().plusExp(tnPl);
        if (!pl.getParty().isEmpty()) {
            int expParty = Math.max(1, tnPl * Settings.PERCENT_EXP_PARTY / 100);
            for (Player member : pl.getParty().getMembers()) {
                if (member.getIdPlayer() == pl.getIdPlayer() || !member.getLocation().getZone().equals(pl.getLocation().getZone())) {
                    continue;
                }
                member.getPoint().plusExp(expParty);
                MapService.instance.onSetXP(member, expParty);
            }
        }
        boolean isLevelUp = false;
        while (pl.getPoint().getExp() >= Util.getExp(pl.getInfo().getLevel())) {
            pl.getPoint().setExp(pl.getPoint().getExp() - Util.getExp(pl.getInfo().getLevel()));
            pl.getInfo().plusLevel((byte) 1);
            pl.getPoint().plusStrength(1);
            pl.getPoint().plusHealth(1);
            pl.getPoint().plusAgility(1);
            pl.getPoint().plusLuck(1);
            pl.getPoint().plusSpirit(1);
            pl.getPoint().plusSkillPoint(1);
            pl.getPoint().plusBasePoint(5);
            isLevelUp = true;
        }
        if (isLevelUp) {
            pl.getPoint().initPoint();
            MapService.instance.onLevelUp(pl);
            Service.instance.sendMainCharInfo(pl);
        }
        MapService.instance.onSetXP(pl, tnPl);
    }

    @Synchronized
    public List<ItemMap> getItemDrop(@NonNull Player plAttack) {
        List<ItemMap> its = new ArrayList<>();
        if (Util.isTrue(0.8, 100.0)) {
            its.add(ItemService.instance.createNewItemMap((short) Util.nextInt(1, 6), (short) Util.nextInt(1, 3), Const.CATEGORY_POTION, x, y, plAttack.getIdPlayer(), zone));
        }
        if (Util.isTrue(1.3, 100.0)) {
            short quantity = (short) Util.nextInt(this.template.getLevel() * 15, (this.template.getLevel() + 10) * 15);
            its.add(ItemService.instance.createNewItemMap((short) 0, quantity, Const.CATEGORY_POTION, x, y, plAttack.getIdPlayer(), zone));
        }
        if (Util.isTrue(0.45, 100.0)) {
            short idItemEquipment = Manager.randomItemEquipment((byte) this.template.getLevel(), (byte) Util.getOne(plAttack.getInfo().getGender(), 0));
            if (idItemEquipment != -1) {
                its.add(ItemService.instance.createNewItemMap(idItemEquipment, (short) 1, Const.CATEGORY_ITEM, x, y, plAttack.getIdPlayer(), zone));
            }
        }
        if (isKhoangSan()) {
            switch (template.getId()) {
                case 85 ->
                    its.add(ItemService.instance.createNewItemMap((short) 81, (short) 1, Const.CATEGORY_GEM_ITEM, x, y, plAttack.getIdPlayer(), zone));
                case 86 ->
                    its.add(ItemService.instance.createNewItemMap((short) 67, (short) 1, Const.CATEGORY_GEM_ITEM, x, y, plAttack.getIdPlayer(), zone));
                case 87 ->
                    its.add(ItemService.instance.createNewItemMap((short) 88, (short) 1, Const.CATEGORY_GEM_ITEM, x, y, plAttack.getIdPlayer(), zone));
                case 88 ->
                    its.add(ItemService.instance.createNewItemMap((short) 95, (short) 1, Const.CATEGORY_GEM_ITEM, x, y, plAttack.getIdPlayer(), zone));
                case 89 ->
                    its.add(ItemService.instance.createNewItemMap((short) 74, (short) 1, Const.CATEGORY_GEM_ITEM, x, y, plAttack.getIdPlayer(), zone));
            }
        }
        return its;
    }

    private void attackPlayer() throws IOException {
        if (!isDie() && !this.buffInfluence.isStunned() && Util.canDoWithTime(lastTimeAttackPlayer, 2000)) {
            this.lastTimeAttackPlayer = System.currentTimeMillis();
            getPlayerCanAttack();
            if (playerTarget != null) {
                MonsterService.instance.sendMonsterAttack(this, playerTarget);
            }
        }
    }

    public void update() throws IOException {
        if (this.isDie() && Util.canDoWithTime(lastTimeDie, Settings.TIME_LIVE_MOB)) {
            this.hp = template.getMaxHp();
        }
        buffInfluence.update();
        if (!canNotAttackPlayer()) {
            attackPlayer();
        }
    }
}
