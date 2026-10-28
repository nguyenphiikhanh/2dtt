package services;

import item.ItemEquip;
import java.io.IOException;
import java.util.List;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.Synchronized;
import manager.Manager;
import manager.Settings;
import map.Monster;
import player.Player;
import network.Message;
import template.SkillNewTemplate;
import utils.CommandMessage;
import consts.Const;
import consts.ItemEquipConst;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class SkillService {

    public static final SkillService instance = new SkillService();

    public void useSkillToPlayer(@NonNull Player pl, byte typeSkill, short idPlayer) throws IOException {
        if (pl.isDie() || pl.getIdPlayer() == idPlayer) {
            return;
        }
        if (pl.getLocation().getZone().getMap().isMapVillage()) {
            ChatService.instance.sendChatOnlyMe(pl, "Không thể tấn công trong làng");
            return;
        }
        ItemEquip weapon = InventoryService.instance.findItemBodyByType(pl, (byte) (3 + pl.getInfo().getClassPlayer()));
        if (weapon == null || weapon.getDurable() <= 0) {
            return;
        }
        pl.getSkill().setTypeSkill(typeSkill);
        byte levelSkill = pl.getSkill().getLevelSkill()[typeSkill];
        if (levelSkill <= 0) {
            return;
        }
        if (!Util.canDoWithTime(pl.getSkill().getTimeLastUseSkills()[typeSkill], Manager.getSkillCooldown(pl.getInfo().getClassPlayer(), typeSkill, levelSkill))) {
            return;
        }
        int skillMP = Manager.getSkillMP(pl.getInfo().getClassPlayer(), typeSkill, levelSkill);
        if (skillMP > pl.getPoint().getMp()) {
            return;
        }
        Player playerTarget = pl.getLocation().getZone().findPlayer(idPlayer);
        if (playerTarget == null || playerTarget.isDie() || !playerTarget.isPlayer()) {
            return;
        }
        if (pl.getSundry().getPk() == 0 || playerTarget.getSundry().getPk() == 0 || playerTarget.getSundry().getPk() == pl.getSundry().getPk()) {
            if (!pl.getSundry().isKiller() && !playerTarget.getSundry().isKiller() && pl.getInfo().getIdNation() == playerTarget.getInfo().getIdNation()) {
                return;
            }
        }
        int range = Manager.getSkillRange(pl.getInfo().getClassPlayer(), typeSkill);
        if (Util.getDistance(pl, playerTarget) > range) {
            return;
        }
        weapon.minusDurable();
        pl.getPoint().minusMp(skillMP);
        if (skillMP > 0) {
            UseItemService.instance.onPlusMp(pl, (short) -skillMP);
        }
        onPlayerAttackPlayer(pl, playerTarget);
        pl.getSkill().getTimeLastUseSkills()[typeSkill] = System.currentTimeMillis();
    }

    public void useSkillToMob(@NonNull Player pl, byte typeSkill, short... idMobs) throws IOException {
        if (pl.isDie()) {
            return;
        }
        if (idMobs == null || idMobs.length <= 0 || idMobs.length > 11) {
            return;
        }
        ItemEquip weapon = InventoryService.instance.findItemBodyByType(pl, (byte) (3 + pl.getInfo().getClassPlayer()));
        ItemEquip cuoc = InventoryService.instance.findItemBodyByType(pl, (byte) 13);
        if (weapon == null || weapon.getDurable() <= 0) {
            return;
        }
        boolean isSkillAeo = Manager.isSkillAeo(pl.getInfo().getClassPlayer(), typeSkill);
        if (!isSkillAeo && idMobs.length > 1) {
            return;
        }
        pl.getSkill().setTypeSkill(typeSkill);
        byte levelSkill = pl.getSkill().getLevelSkill()[typeSkill];
        if (levelSkill <= 0) {
            return;
        }
        if (!Util.canDoWithTime(pl.getSkill().getTimeLastUseSkills()[typeSkill], Manager.getSkillCooldown(pl.getInfo().getClassPlayer(), typeSkill, levelSkill))) {
            return;
        }
        int skillMP = Manager.getSkillMP(pl.getInfo().getClassPlayer(), typeSkill, levelSkill);
        if (skillMP > pl.getPoint().getMp()) {
            return;
        }
        Monster mobTarget = pl.getLocation().getZone().findMob(idMobs[0]);
        if (mobTarget == null || mobTarget.isDie() || mobTarget.playerCanNotAttack()) {
            return;
        }
        int range = Manager.getSkillRange(pl.getInfo().getClassPlayer(), typeSkill);
        if (Util.getDistance(pl, mobTarget) > range + Settings.DISTANCE_MOB_CAN_ATTACK + 60) {
            return;
        }
        weapon.minusDurable();
        pl.getPoint().minusMp(skillMP);
        if (skillMP > 0) {
            UseItemService.instance.onPlusMp(pl, (short) -skillMP);
        }
        if (isSkillAeo && !mobTarget.isKhoangSan()) {
            @Cleanup("clear")
            List<Monster> mobsNear = pl.getLocation().getZone().findMobNear(pl, idMobs, range + Settings.DISTANCE_MOB_CAN_ATTACK + 60);
            onPlayerAttackMultiMob(pl, mobsNear);
        } else {
            if (mobTarget.isKhoangSan()) {
                if (cuoc == null || cuoc.getDurable() <= 0) {
                    return;
                }
                cuoc.minusDurable();
            }
            onPlayerAttackMob(pl, mobTarget, cuoc);
        }
        pl.getSkill().getTimeLastUseSkills()[typeSkill] = System.currentTimeMillis();
    }

    private void onPlayerAttackPlayer(@NonNull Player player, @NonNull Player playerTarget) throws IOException {
        boolean isCrit = Util.isTrue((double) player.getPoint().getCritical(), 100.0);
        boolean isBaoKich = Util.isTrue((double) player.getPoint().getBaoKich(), 100.0);
        boolean isMiss = Util.isTrue((double) playerTarget.getPoint().getDodge(), 100.0);
        boolean isXuyenGiap = Util.isTrue((double) player.getPoint().getXuyenGiap(), 100.0);
        boolean x2 = Util.isTrue((double) player.getPoint().getX2(), 100.0);
        byte effAttack = Const.NONE_EFFECT;
        byte typeSkill = player.getSkill().getTypeSkill();
        if (isMiss) {
            effAttack = Const.MISS_EFFECT;
        } else if (isCrit) {
            effAttack = Const.CRIT_EFFECT;
        } else if (isBaoKich) {
            effAttack = Const.BAO_KICK_EFFECT;
        }
        int damePlayer = player.getPoint().getDameAttack(isMiss, isCrit, isBaoKich, false);
        damePlayer = BuffService.instance.onAttackPlayerHasBuff(player, playerTarget, damePlayer);
        int dameHit = playerTarget.injured(damePlayer, false, ((player.getInfo().getClassPlayer() == Const.PHAP_SU || player.getInfo().getClassPlayer() == Const.CUNG_THU) ? ItemEquipConst.DAMAGE_MAGIC : ItemEquipConst.DAMAGE_PHYSIC), x2);
        BuffService.instance.onPlayerInjured(player, playerTarget);

        if (playerTarget.isDie()) {
            player.getInfo().plusKiller((byte) 1);
            MapService.instance.sendKiller(player);
        }

        if (dameHit == 0) {
            effAttack = Const.MISS_EFFECT;
            isXuyenGiap = false;
        }

        Message msg = new Message(CommandMessage.PLAYER_ATTACK_PLAYER);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeShort(playerTarget.getIdPlayer());
        msg.writer().writeByte(typeSkill);
        msg.writer().writeInt(dameHit);
        msg.writer().writeInt(playerTarget.getPoint().getHp());
        msg.writer().writeByte(effAttack);
        msg.writer().writeByte(x2 ? 2 : 1);
        msg.writer().writeByte(isXuyenGiap ? 0 : 1);
        msg.writer().writeByte(player.getSkill().getLevelSkill()[typeSkill]);
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    private void onPlayerAttackMob(@NonNull Player player, @NonNull Monster mob, ItemEquip cuoc) throws IOException {
        boolean isCrit = Util.isTrue((double) player.getPoint().getCritical(), 100.0);
        boolean isBaoKich = Util.isTrue((double) player.getPoint().getBaoKich(), 100.0);
        boolean isMiss = Util.isTrue(2.5, 96.7);
        boolean isXuyenGiap = Util.isTrue((double) player.getPoint().getXuyenGiap(), 100.0);
        boolean x2 = Util.isTrue((double) player.getPoint().getX2(), 100.0);

        byte effAttack = Const.NONE_EFFECT;
        byte typeSkill = player.getSkill().getTypeSkill();
        if (isMiss) {
            effAttack = Const.MISS_EFFECT;
        } else if (isCrit) {
            effAttack = Const.CRIT_EFFECT;
        } else if (isBaoKich) {
            effAttack = Const.BAO_KICK_EFFECT;
        }
        int dameAttack = player.getPoint().getDameAttack(isMiss, isCrit, isBaoKich, true);
        if (mob.isKhoangSan()) {
            dameAttack = cuoc.getTemplate().getId() == 466 ? 20 : 10;
        }
        int dameHit = mob.injured(player, dameAttack, isXuyenGiap, false, x2);
        if (dameHit == 0) {
            effAttack = Const.MISS_EFFECT;
            isXuyenGiap = false;
        }
        Message msg = new Message(CommandMessage.PLAYER_ATTACK_MONSTER);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeShort(mob.getId());
        msg.writer().writeByte(typeSkill);
        msg.writer().writeInt(dameHit);
        msg.writer().writeInt(mob.getHp());
        msg.writer().writeByte(effAttack);
        msg.writer().writeByte(x2 ? 2 : 1);
        msg.writer().writeByte(isXuyenGiap ? 0 : 1);
        msg.writer().writeByte(player.getSkill().getLevelSkill()[typeSkill]);
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    private void onPlayerAttackMultiMob(@NonNull Player player, @NonNull List<Monster> mobs) throws IOException {
        boolean isCrit = Util.isTrue((double) player.getPoint().getCritical(), 100.0);
        boolean isBaoKich = Util.isTrue((double) player.getPoint().getBaoKich(), 100.0);
        boolean isMiss = Util.isTrue(2.5, 96.7);
        boolean isXuyenGiap = Util.isTrue((double) player.getPoint().getXuyenGiap(), 100.0);

        byte effAttack = Const.NONE_EFFECT;
        byte typeSkill = player.getSkill().getTypeSkill();
        if (isMiss) {
            effAttack = Const.MISS_EFFECT;
        } else if (isCrit) {
            effAttack = Const.CRIT_EFFECT;
        } else if (isBaoKich) {
            effAttack = Const.BAO_KICK_EFFECT;
        }
        Monster mobTarget = mobs.get(0);
        int dameHit = mobTarget.injured(player, player.getPoint().getDameAttack(isMiss, isCrit, isBaoKich, true), isXuyenGiap, false, false);
        if (dameHit == 0) {
            effAttack = Const.MISS_EFFECT;
            isXuyenGiap = false;
        }
        Message msg = new Message(CommandMessage.ATTACK_MULTI_MONSTER);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(typeSkill);
        msg.writer().writeInt(dameHit);
        msg.writer().writeByte(effAttack);
        msg.writer().writeByte(player.getSkill().getLevelSkill()[typeSkill]);
        msg.writer().writeByte(isXuyenGiap ? 0 : 1);
        msg.writer().writeByte(mobs.size());
        msg.writer().writeShort(mobTarget.getId());
        msg.writer().writeInt(mobTarget.getHp());
        for (int j = 1; j < mobs.size(); j++) {
            Monster mob = mobs.get(j);
            mob.minusHp(player, dameHit);
            msg.writer().writeShort(mob.getId());
            msg.writer().writeInt(mob.getHp());
        }
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    @Synchronized
    public void learnNewSkill(@NonNull Player player, byte indexSkillNew) throws IOException {
        SkillNewTemplate templateSkill = Manager.getListSkillNew(player.getInfo().getClassPlayer()).get(indexSkillNew);
        if (templateSkill == null) {
            return;
        }
        if (!player.getInventory().minusXu(templateSkill.getPrice())) {
            Service.instance.sendLogOut(player.getSession(), "Không đủ xu");
            return;
        }
        short levelRequest = Manager.getLevelAddSkill(templateSkill.getIdSkill(), 1);
        if (player.getInfo().getLevel() < levelRequest) {
            Service.instance.sendLogOut(player.getSession(), String.format("Yêu cầu level %s để học kĩ năng %s", levelRequest, templateSkill.getName()));
            return;
        }
        if (player.getSkill().getLevelSkill()[templateSkill.getIdSkill()] == -1) {
            player.getSkill().getLevelSkill()[templateSkill.getIdSkill()] = 0;
            player.getPoint().initPoint();
            Service.instance.sendMainCharInfo(player);
            Service.instance.sendLogOut(player.getSession(), String.format("Học thành công kĩ năng %s", templateSkill.getName()));
        } else {
            Service.instance.sendLogOut(player.getSession(), "Bạn đã học skill này rồi");
        }
    }

    public void sendSkillInfo(@NonNull Player pl) throws IOException {
        Message msg = new Message(CommandMessage.SKILL_INFO);
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                msg.writer().writeShort(Manager.SKILL_DAM_PERCENT[pl.getInfo().getClassPlayer()][i][j]);
            }
        }
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                msg.writer().writeShort(Manager.SKILL_COOLDOWN[pl.getInfo().getClassPlayer()][i][j] / 100);
            }
        }
        for (int i = 0; i < 15; i++) {
            msg.writer().writeShort(Manager.SKILL_RANGE[pl.getInfo().getClassPlayer()][i]);
        }
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                msg.writer().writeByte(Manager.SKILL_MP[pl.getInfo().getClassPlayer()][i][j]);
            }
        }
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                msg.writer().writeShort(Manager.TIME_LIFE_BUFF_SKILL[i][j]);
            }
        }
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                msg.writer().writeByte(Manager.LEVEL_ADD_SKILL[i][j]);
            }
        }
        for (int i = 0; i < 5; i++) {
            msg.writer().writeByte(Manager.SKILL_AEO[i].length);
            for (int j = 0; j < Manager.SKILL_AEO[i].length; j++) {
                msg.writer().writeByte(Manager.SKILL_AEO[i][j]);
            }
        }
        pl.getSession().sendMessage(msg);
    }
}
