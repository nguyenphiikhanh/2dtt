package services;

import item.ItemMap;
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
import template.MonsterTemplate;
import utils.CommandMessage;
import consts.Const;
import consts.ItemEquipConst;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class MonsterService {

    public static final MonsterService instance = new MonsterService();

    public void sendMonsterEndAttack(@NonNull Monster monster) throws IOException {
        Message msg = new Message(CommandMessage.MONSTER_ATTACK_PLAYER);
        msg.writer().writeShort(monster.getId());
        msg.writer().writeShort(32001);
        msg.writer().writeInt(0);
        msg.writer().writeInt(0);
        MapService.instance.sendAllPlayerInMap(monster, msg);
    }

    public void sendMonsterAttack(@NonNull Monster monster, @NonNull Player plTarget) throws IOException {
        int dameMob = monster.getDameAttack(plTarget);
        dameMob = BuffService.instance.onAttackPlayerHasBuff(monster, plTarget, dameMob);
        int damage = plTarget.injured(dameMob, false, ItemEquipConst.DAMAGE_PHYSIC, false);
        Message msg = new Message(CommandMessage.MONSTER_ATTACK_PLAYER);
        msg.writer().writeShort(monster.getId());
        msg.writer().writeShort(plTarget.getIdPlayer());
        msg.writer().writeInt(damage);
        msg.writer().writeInt(plTarget.getPoint().getHp());
        MapService.instance.sendAllPlayerInMap(monster, msg);
    }

    @Synchronized
    public void onMonsterDropItem(@NonNull Monster monster, @NonNull Player plAtt) throws IOException {
        if (!monster.isDie()) {
            return;
        }
        Message msg = new Message(CommandMessage.MONSTER_DIE);
        msg.writer().writeShort(plAtt.getIdPlayer());
        msg.writer().writeShort(monster.getId());
        msg.writer().writeByte(plAtt.getSkill().getTypeSkill());
        msg.writer().writeInt(-1);
        msg.writer().writeByte(Const.NONE_EFFECT);
        @Cleanup("clear")
        List<ItemMap> itemsDrop = monster.getItemDrop(plAtt);
        msg.writer().writeByte(itemsDrop.size());
        for (int i = 0; i < itemsDrop.size(); i++) {
            ItemMap itemMap = itemsDrop.get(i);
            plAtt.getOtherItemMapInside().add(itemMap.getItemMapId());
            monster.getZone().addItem(itemMap);
            msg.writer().writeByte(itemMap.getItemCatagory());
            msg.writer().writeShort(itemMap.getItemTemplateID());
            msg.writer().writeShort(itemMap.getItemMapId());
            msg.writer().writeShort(itemMap.getX());
            msg.writer().writeShort(itemMap.getY());
        }
        MapService.instance.sendAllPlayerInMap(monster, msg);
    }

    public void sendMonsterMove(@NonNull Monster monster) throws IOException {
        if (monster.isDie()) {
            return;
        }
        Message msg = new Message(CommandMessage.MOVE_CHAR);
        msg.writer().writeByte(Const.CATEGORY_MONSTER);
        msg.writer().writeByte(monster.getTemplate().getId());
        msg.writer().writeShort(monster.getId());
        msg.writer().writeShort(monster.getX());
        msg.writer().writeShort(monster.getY());
        msg.writer().writeByte(-1);
        msg.writer().writeInt(-1);
        msg.writer().writeByte(0);
        msg.writer().writeByte(-1);
        msg.writer().writeBoolean(true);
        MapService.instance.sendAllPlayerInMap(monster, msg);
    }

    public void sendMonsterMove(@NonNull Player pl, @NonNull Monster monster) throws IOException {
        if (monster.isDie()) {
            return;
        }
        Message msg = new Message(CommandMessage.MOVE_CHAR);
        msg.writer().writeByte(Const.CATEGORY_MONSTER);
        msg.writer().writeByte(monster.getTemplate().getId());
        msg.writer().writeShort(monster.getId());
        msg.writer().writeShort(monster.getX());
        msg.writer().writeShort(monster.getY());
        msg.writer().writeByte(-1);
        msg.writer().writeInt(-1);
        msg.writer().writeByte(0);
        msg.writer().writeByte(-1);
        msg.writer().writeBoolean(true);
        pl.getSession().sendMessage(msg);
    }

    public void sendMonsterInfo(@NonNull Player pl, short id) throws IOException {
        for (Monster monster : pl.getLocation().getZone().getMobs()) {
            if (monster != null && Util.getDistance(pl, monster) < pl.getSession().getDistanceLoad() && !monster.isDie() && monster.getId() == id) {
                Message m = new Message(CommandMessage.MONSTER_INFO);
                m.writer().writeShort(id);
                m.writer().writeByte(monster.getTemplate().getId());
                m.writer().writeShort(monster.getX());
                m.writer().writeShort(monster.getY());
                m.writer().writeInt(monster.getHp());
                m.writer().writeByte(monster.getTemplate().getLevel());
                m.writer().writeByte(monster.getTemplate().getType());
                m.writer().writeInt(monster.getTemplate().getMaxHp());
                m.writer().writeInt(Settings.TIME_LIVE_MOB);
                pl.getSession().sendMessage(m);
            }
        }
    }

    public void sendMonsterImage(@NonNull Player pl, short id) throws IOException {
        byte[] data = Manager.getImageMonster(id);
        if (data == null) {
            return;
        }
        Message m = new Message(CommandMessage.LOAD_IMAGE_MONSTER);
        m.writer().writeShort(id);
        m.writer().writeBoolean(false);
        m.writer().writeShort(data.length);
        m.writer().write(data);
        pl.getSession().sendMessage(m);
    }

    public void sendMonsterTemplate(@NonNull Player pl, byte type, short idMob) throws IOException {
        if (type != 0) {
            return;
        }
        MonsterTemplate monsterTemplate = Manager.getMobTemplate(idMob);
        if (monsterTemplate == null) {
            return;
        }
        Message m = new Message(CommandMessage.GET_INFO_TEMPLATE);
        m.writer().writeByte(type);
        m.writer().writeShort(monsterTemplate.getId());
        m.writer().writeByte(9);
        m.writer().writeByte(monsterTemplate.getMoveType());
        m.writer().writeByte(monsterTemplate.getSpeed());
        m.writer().writeByte(monsterTemplate.getHeight());
        m.writer().writeByte(monsterTemplate.getW());
        m.writer().writeByte(monsterTemplate.getH());
        m.writer().writeByte(monsterTemplate.getXCenter());
        m.writer().writeByte(monsterTemplate.getYCenter());
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeUTF(monsterTemplate.getName());
        m.writer().writeByte(monsterTemplate.getType());
        m.writer().writeInt(monsterTemplate.getMaxHp());
        m.writer().writeByte(monsterTemplate.getPalate());
        m.writer().writeByte(monsterTemplate.getSpalate());
        m.writer().writeByte(monsterTemplate.isNewMonster() ? 1 : 0);
        pl.getSession().sendMessage(m);
    }
}
