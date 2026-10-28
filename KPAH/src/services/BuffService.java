package services;

import java.io.IOException;
import lombok.NonNull;
import manager.Manager;
import map.Monster;
import network.Message;
import player.Player;
import consts.BuffConst;
import utils.CommandMessage;
import consts.Const;
import consts.ItemEquipConst;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class BuffService {

    public static final BuffService instance = new BuffService();

    public void useSkillBuff(@NonNull Player pl, byte effSkill, short idPlayerTarget) throws IOException {
        if (pl.isDie() || idPlayerTarget >= 0) {
            return;
        }
        Player playerTarget = pl.getLocation().getZone().findPlayer(idPlayerTarget);
        if (playerTarget == null) {
            return;
        }
        byte typeSkill = Manager.getIndexBuff(pl.getInfo().getClassPlayer(), effSkill);
        byte typeBuff = Manager.getTypeBuff(pl.getInfo().getClassPlayer(), (byte) (typeSkill - 4));
        if (typeBuff == BuffConst.PASSIVE_BUFF) {
            return;
        }
        pl.getSkill().setTypeBuffSkill(typeSkill);
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
        short percentDamage = Manager.getSkillDamPercent(pl.getInfo().getClassPlayer(), typeSkill, levelSkill);
        pl.getPoint().minusMp(skillMP);
        UseItemService.instance.onPlusMp(pl, (short) -skillMP);
        if (typeBuff == BuffConst.ACTIVE_BUFF) {
            if (playerTarget.getIdPlayer() == pl.getIdPlayer() && !pl.getSkillBuff().isExistBuff(effSkill)) {
                short timeLive = Manager.getTimeLifeBuffSkill(typeSkill, levelSkill);
                pl.getSkillBuff().addBuff(effSkill, timeLive, percentDamage);
                sendPlayerUseBuff(pl, effSkill, levelSkill, timeLive);
            }
        } else if (typeBuff == BuffConst.REVIVE_BUFF && pl.getInfo().getClassPlayer() == Const.PHAP_SU) {
            if (playerTarget.isDie() && playerTarget.isPlayer() && playerTarget.getIdPlayer() != pl.getIdPlayer()) {
                MapService.instance.revivePlayer(playerTarget, (byte) percentDamage);
                ChatService.instance.sendChat(playerTarget, String.format("Cảm ơn %s đã hồi sinh", pl.getName()));
            }
        }
        pl.getSkill().getTimeLastUseSkills()[typeSkill] = System.currentTimeMillis();
    }

    public void onMobInjured(@NonNull Player playerAttack, @NonNull Monster mob) throws IOException {
        if (playerAttack.isDie() || mob.isDie()) {
            return;
        }
        switch (playerAttack.getInfo().getClassPlayer()) {
            case Const.CUNG_THU -> {
                if (playerAttack.getSkillBuff().isExistBuff(BuffConst.DOC_LUU_TIEN)) {
                    short percentDamage = playerAttack.getSkillBuff().getPercentDame(BuffConst.DOC_LUU_TIEN);
                    mob.getBuffInfluence().addBuffPoisoned(playerAttack, percentDamage, percentDamage);
                }
            }
            case Const.DAU_SI -> {
                if (playerAttack.getSkillBuff().isExistBuff(BuffConst.BAT_DI_BIEN)) {
                    short percentDamage = playerAttack.getSkillBuff().getPercentDame(BuffConst.BAT_DI_BIEN);
                    if (Util.isTrue((double) percentDamage, 100.0)) {
                        mob.getBuffInfluence().addBuffStunned((short) 5);
                    }
                }
            }
        }
    }

    public void onPlayerInjured(@NonNull Player playerAttack, @NonNull Player player) throws IOException {
        if (playerAttack.isDie() || player.isDie()) {
            return;
        }
        switch (playerAttack.getInfo().getClassPlayer()) {
            case Const.CUNG_THU -> {
                if (playerAttack.getSkillBuff().isExistBuff(BuffConst.DOC_LUU_TIEN)) {
                    short percentDamage = playerAttack.getSkillBuff().getPercentDame(BuffConst.DOC_LUU_TIEN);
                    player.getBuffInfluence().addBuffPoisoned(percentDamage, percentDamage);
                }
            }
            case Const.DAU_SI -> {
                if (playerAttack.getSkillBuff().isExistBuff(BuffConst.BAT_DI_BIEN)) {
                    short percentDamage = playerAttack.getSkillBuff().getPercentDame(BuffConst.BAT_DI_BIEN);
                    if (Util.isTrue((double) percentDamage, 100.0)) {
                        player.getBuffInfluence().addBuffStunned((short) 5);
                    }
                }
            }
        }
    }

    public int onAttackPlayerHasBuff(@NonNull Monster mobAttack, @NonNull Player playerTarget, int damage) throws IOException {
        if (playerTarget.isDie() || mobAttack.isDie()) {
            return damage;
        }
        switch (playerTarget.getInfo().getClassPlayer()) {
            case Const.KIEM_KHACH -> {
                if (playerTarget.getSkillBuff().isExistBuff(BuffConst.DI_LUC_DAO_CONG)) {
                    short percentDamage = playerTarget.getSkillBuff().getPercentDame(BuffConst.DI_LUC_DAO_CONG);
                    if (Util.isTrue((double) percentDamage, 100.0)) {
                        short hp = (short) mobAttack.injured(playerTarget, (int) (damage * percentDamage / 100), false, true, false);
                        if (hp > 0) {
                            sendSubHpByBuffInfluence(mobAttack, hp);
                        }
                    }
                }
            }
            case Const.PHAP_SU -> {
                if (playerTarget.getSkillBuff().isExistBuff(BuffConst.SONG_HO_CONG_THU)) {
                    short percentDamage = playerTarget.getSkillBuff().getPercentDame(BuffConst.SONG_HO_CONG_THU);
                    short mpPlus = (short) (damage * percentDamage / 100);
                    damage -= mpPlus;
                    if (mpPlus > 0) {
                        playerTarget.getPoint().plusMp(mpPlus);
                        UseItemService.instance.onPlusMp(playerTarget, mpPlus);
                    }
                }
            }
        }
        return damage;
    }

    public int onAttackPlayerHasBuff(@NonNull Player playerAttack, @NonNull Player playerTarget, int damage) throws IOException {
        if (playerTarget.isDie() || playerAttack.isDie()) {
            return damage;
        }
        switch (playerTarget.getInfo().getClassPlayer()) {
            case Const.KIEM_KHACH -> {
                if (playerTarget.getSkillBuff().isExistBuff(BuffConst.DI_LUC_DAO_CONG)) {
                    short percentDamage = playerTarget.getSkillBuff().getPercentDame(BuffConst.DI_LUC_DAO_CONG);
                    if (Util.isTrue((double) percentDamage, 100.0)) {
                        short hp = (short) playerAttack.injured((int) (damage * percentDamage / 100), true, ItemEquipConst.DAMAGE_NONE, false);
                        if (hp > 0) {
                            sendSubHpByBuffInfluence(playerAttack, hp);
                        }
                    }
                }
            }
            case Const.PHAP_SU -> {
                if (playerTarget.getSkillBuff().isExistBuff(BuffConst.SONG_HO_CONG_THU)) {
                    short percentDamage = playerTarget.getSkillBuff().getPercentDame(BuffConst.SONG_HO_CONG_THU);
                    short mpPlus = (short) (damage * percentDamage / 100);
                    damage -= mpPlus;
                    if (mpPlus > 0) {
                        playerTarget.getPoint().plusMp(mpPlus);
                        UseItemService.instance.onPlusMp(playerTarget, mpPlus);
                    }
                }
            }
        }
        return damage;
    }

    public void sendSubHpByBuffInfluence(@NonNull Player player, short hpSub) throws IOException {
        Message msg = new Message(CommandMessage.BUFF_ATTACK);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        msg.writer().writeByte(-1);
        msg.writer().writeShort(hpSub);
        msg.writer().writeByte(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeByte(-1);
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    public void sendSubHpByBuffInfluence(@NonNull Monster mob, short hpSub) throws IOException {
        Message msg = new Message(CommandMessage.BUFF_ATTACK);
        msg.writer().writeShort(mob.getId());
        msg.writer().writeByte(Const.CATEGORY_MONSTER);
        msg.writer().writeByte(-1);
        msg.writer().writeShort(hpSub);
        msg.writer().writeByte(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeByte(-1);
        msg.writer().writeByte(-1);
        MapService.instance.sendAllPlayerInMap(mob, msg);
    }

    public void sendRemoveBuffInfluence(@NonNull Player player, byte idBuff) throws IOException {
        Message msg = new Message(CommandMessage.BUFF_ATTACK);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        msg.writer().writeByte(BuffConst.SECOND_SUB_HP_DOC_TO);
        msg.writer().writeShort(0);
        msg.writer().writeByte(4);
        msg.writer().writeByte(idBuff);
        msg.writer().writeByte(1);
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    public void sendRemoveBuffInfluence(@NonNull Monster mob, byte idBuff) throws IOException {
        Message msg = new Message(CommandMessage.BUFF_ATTACK);
        msg.writer().writeShort(mob.getId());
        msg.writer().writeByte(Const.CATEGORY_MONSTER);
        msg.writer().writeByte(BuffConst.SECOND_SUB_HP_DOC_TO);
        msg.writer().writeShort(0);
        msg.writer().writeByte(4);
        msg.writer().writeByte(idBuff);
        msg.writer().writeByte(1);
        MapService.instance.sendAllPlayerInMap(mob, msg);
    }

    public void sendAddBuffInfluence(@NonNull Player player, byte idBuff) throws IOException {
        Message msg = new Message(CommandMessage.BUFF_ATTACK);
        msg.writer().writeShort(player.getIdPlayer());
        msg.writer().writeByte(Const.CATEGORY_PLAYER);
        msg.writer().writeByte(BuffConst.SECOND_SUB_HP_DOC_TO);
        msg.writer().writeShort(0);
        msg.writer().writeByte(4);
        msg.writer().writeByte(idBuff);
        msg.writer().writeByte(idBuff == BuffConst.BUFF_DOC_TO ? player.getBuffInfluence().getSecondPosonedLeft() : player.getBuffInfluence().getSecondOfStunned());
        MapService.instance.sendAllPlayerInMap(player, msg);
    }

    public void sendAddBuffInfluence(@NonNull Monster mob, byte idBuff) throws IOException {
        Message msg = new Message(CommandMessage.BUFF_ATTACK);
        msg.writer().writeShort(mob.getId());
        msg.writer().writeByte(Const.CATEGORY_MONSTER);
        msg.writer().writeByte(BuffConst.SECOND_SUB_HP_DOC_TO);
        msg.writer().writeShort(0);
        msg.writer().writeByte(4);
        msg.writer().writeByte(idBuff);
        msg.writer().writeByte(idBuff == BuffConst.BUFF_DOC_TO ? mob.getBuffInfluence().getSecondPosonedLeft() : mob.getBuffInfluence().getSecondOfStunned());
        MapService.instance.sendAllPlayerInMap(mob, msg);
    }

    public void sendPlayerUseBuff(@NonNull Player pl, byte eff, byte level, short secondLive) throws IOException {
        Message msg = new Message(CommandMessage.USE_BUFF);
        msg.writer().writeByte(BuffConst.ADD_BUFF);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeByte(eff);
        msg.writer().writeShort(secondLive);
        msg.writer().writeByte(level);
        MapService.instance.sendAllPlayerInMap(pl, msg);
    }

    public void sendRemoveUseBuff(@NonNull Player pl, byte eff) throws IOException {
        Message msg = new Message(CommandMessage.USE_BUFF);
        msg.writer().writeByte(BuffConst.REMOVE_BUFF);
        msg.writer().writeShort(pl.getIdPlayer());
        msg.writer().writeByte(eff);
        msg.writer().writeShort(0);
        MapService.instance.sendAllPlayerInMap(pl, msg);
    }

    public void sendEffectBuffToPlayer(@NonNull Player pl, @NonNull Player plReceive) throws IOException {
        Message msg = new Message(CommandMessage.USE_BUFF);
        msg.writer().writeByte(BuffConst.VIEW_BUFF);
        msg.writer().writeShort(pl.getIdPlayer());
        for (int j = 0; j < pl.getSkillBuff().getIdBuff().length; j++) {
            msg.writer().writeByte(pl.getSkillBuff().getIdBuff()[j]);
        }
        for (int j = 0; j < pl.getSkillBuff().getCoolDown().length; j++) {
            msg.writer().writeShort(pl.getSkillBuff().getCoolDown()[j]);
        }
        for (int j = 0; j < 7; j++) {
            msg.writer().writeByte(-1);
        }
        plReceive.getSession().sendMessage(msg);
    }
}
