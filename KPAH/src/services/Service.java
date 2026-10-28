package services;

import clan.Clan;
import utils.Util;
import player.Player;
import interfaces.ISession;
import java.io.IOException;
import lombok.NonNull;
import manager.ClientManager;
import manager.Manager;
import network.Message;
import template.ItemEquipTemplate;
import consts.ClanConst;
import utils.CommandMessage;
import consts.Const;
import effects.EffectData;
import effects.ImageInfo;
import effects.PartFrame;

public class Service {

    public static final Service instance = new Service();

    public void sendConfigGame(@NonNull Player pl, byte type) throws IOException {
        Message m = new Message(CommandMessage.CONFIG);
        m.writer().writeByte(type);
        m.writer().writeByte(-1);
        pl.getSession().sendMessage(m);
    }

    public void sendEffectData(@NonNull Player pl) throws IOException {
        Message m = new Message(CommandMessage.MONSTER_TEMPLATE);
        m.writer().write(Manager.DATA_EFFECT);
        pl.getSession().sendMessage(m);
    }

    public void sendEffectObject(@NonNull Player pl, byte id) throws IOException {
        EffectData data = Manager.getEffectData(Const.NORMAL_EFFECT, id);
        if (data == null) {
            return;
        }
        Message msg = new Message(CommandMessage.EFFECT_OBJ);
        msg.writer().writeByte(1);
        msg.writer().writeByte(id);
        msg.writer().writeShort(data.getImageData().length);
        msg.writer().write(data.getImageData());
        msg.writer().writeByte(data.getImageInfo().length);
        for (ImageInfo imageInfo : data.getImageInfo()) {
            msg.writer().writeByte(imageInfo.getID());
            msg.writer().writeByte(imageInfo.getX0());
            msg.writer().writeByte(imageInfo.getY0());
            msg.writer().writeByte(imageInfo.getW());
            msg.writer().writeByte(imageInfo.getH());
        }
        msg.writer().writeByte(data.getFrame().length);
        for (PartFrame[] parts : data.getFrame()) {
            msg.writer().writeByte(parts.length);
            for (PartFrame part : parts) {
                msg.writer().writeByte(part.getDx());
                msg.writer().writeByte(part.getDy());
                msg.writer().writeByte(part.getIdSmallImg());
            }
        }
        msg.writer().writeByte(data.getArrFrame().length);
        for (byte a : data.getArrFrame()) {
            msg.writer().writeByte(a);
        }
        pl.getSession().sendMessage(msg);
    }

    public void sendLogOut(@NonNull ISession session, String s) throws IOException {
        Message m = new Message(CommandMessage.LOGOUT);
        m.writer().writeUTF(s);
        session.sendMessage(m);
    }

    public void sendImageTree(@NonNull ISession session, byte id) throws IOException {
        byte[] treeInfo = Manager.getImageTree(id);
        if (treeInfo == null) {
            return;
        }
        Message m = new Message(CommandMessage.LOAD_IMAGE_TREE);
        m.writer().writeByte(id);
        m.writer().writeShort(treeInfo.length);
        m.writer().write(treeInfo);
        session.sendMessage(m);
    }

    public void sendImageCloth(@NonNull ISession session, byte type, byte id) throws IOException {
        byte[] data = Manager.getImageCloth(id, type);
        if (data == null) {
            return;
        }
        Message m = new Message(CommandMessage.LOAD_IMAGE_TREE);
        m.writer().writeByte(-1);
        m.writer().writeByte(type);
        m.writer().writeByte(id);
        m.writer().write(data);
        session.sendMessage(m);
    }

    public void sendImage(@NonNull Player pl, byte type) throws IOException {
        Message msg = new Message(CommandMessage.GET_IMAGE);
        msg.writer().writeByte(type);
        msg.writer().writeByte(68);
        switch (type) {
            case 1 -> {
                msg.writer().writeShort(Manager.ICON_SKILL[pl.getInfo().getClassPlayer()].length);
                msg.writer().write(Manager.ICON_SKILL[pl.getInfo().getClassPlayer()]);
            }
            case 2 -> {
                msg.writer().writeShort(Manager.ICON_POTION.length);
                msg.writer().write(Manager.ICON_POTION);
                msg.writer().writeShort(Manager.ICON_ITEM.length);
                msg.writer().write(Manager.ICON_ITEM);
                msg.writer().writeByte(Manager.DATA_HORSE.length);
                for (int i = 0; i < Manager.DATA_HORSE.length; i++) {
                    msg.writer().writeShort(Manager.DATA_HORSE[i].length);
                    msg.writer().write(Manager.DATA_HORSE[i]);
                    msg.writer().writeByte(Manager.HEAD_HORSE[i].length);
                    for (byte[] item : Manager.HEAD_HORSE[i]) {
                        msg.writer().writeShort(item.length);
                        msg.writer().write(item);
                    }
                }
            }
            case 4 -> {
                msg.writer().writeShort(Manager.ICON_ANIMAL.length);
                msg.writer().write(Manager.ICON_ANIMAL);
            }
        }
        pl.getSession().sendMessage(msg);
    }

    public void sendIcon(@NonNull ISession session, short id) throws IOException {
        byte[] data = Manager.getImageDefault(id);
        if (data == null) {
            return;
        }
        Message m = new Message(CommandMessage.IMAGE_SERVER);
        m.writer().writeShort(id);
        m.writer().write(data);
        session.sendMessage(m);
    }

    public void sendWeaponData(@NonNull ISession session, byte type, byte sbyte, byte index) throws IOException {
        Message m = new Message(CommandMessage.GET_WEAPONE);
        m.writer().writeByte(0);
        ItemEquipTemplate template = Manager.getItemEquipment(type, sbyte, index);
        if (template != null) {
            m.writer().writeByte(0);
            byte[][] image = Manager.getImageWeapon(template.getId());
            m.writer().writeShort(image[0].length);
            for (int i = 0; i < image[0].length; i++) {
                m.writer().writeByte(image[0][i]);
            }
            m.writer().writeShort(image[1].length);
            for (int i = 0; i < image[1].length; i++) {
                m.writer().writeByte(image[1][i]);
            }
            m.writer().writeByte(template.getDxWear());
            m.writer().writeByte(template.getDyWear());
        } else {
            m.writer().writeByte(-1);
        }
        session.sendMessage(m);
    }

    public void sendAllPlayer(@NonNull Message msg) {
        for (Player player : ClientManager.getPlayers().values()) {
            if (player != null) {
                player.getSession().sendMessage(msg);
            }
        }
    }

    public void sendEndDialog(@NonNull Player player) throws IOException {
        Message m = new Message(CommandMessage.ADD_BASE_POINT);
        player.getSession().sendMessage(m);
    }

    public void sendMainCharInfo(@NonNull Player player) throws IOException {
        Message m = new Message(CommandMessage.INFO_MAIN_CHAR);
        m.writer().writeShort(player.getIdPlayer());
        m.writer().writeUTF(player.getName());
        m.writer().writeInt(player.getPoint().getHp());
        m.writer().writeInt(player.getPoint().getHpMax());
        m.writer().writeInt(player.getPoint().getMp());
        m.writer().writeInt(player.getPoint().getMpMax());
        m.writer().writeByte(player.getHead());
        m.writer().writeByte(player.getInfo().getClassPlayer());
        m.writer().writeInt(player.getPoint().getAttack());
        m.writer().writeInt(player.getPoint().getDefend());
        m.writer().writeInt(player.getPoint().getDefendMagic());
        m.writer().writeShort(player.getPoint().getAccurate());
        m.writer().writeShort(player.getPoint().getDodge());
        m.writer().writeShort(player.getPoint().getCritical());
        m.writer().writeByte(player.getInfo().getHe());
        m.writer().writeByte(player.getInfo().getLevel());
        m.writer().writeShort(Util.getPercentExp(player.getInfo().getLevel(), player.getPoint().getExp()));

        m.writer().writeShort(player.getPoint().getStrength());
        m.writer().writeShort(player.getPoint().getAgility());
        m.writer().writeShort(player.getPoint().getSpirit());
        m.writer().writeShort(player.getPoint().getHealth());
        m.writer().writeShort(player.getPoint().getLuck());

        m.writer().writeShort(player.getPoint().getBasePoint());
        m.writer().writeShort(player.getPoint().getSkillPoint());
        m.writer().writeInt(player.getPoint().getDedicationPoint());
        m.writer().writeShort(player.getPoint().getBaoKich());

        m.writer().writeByte(player.getSkill().getLevelSkill().length);
        for (int i = 0; i < player.getSkill().getLevelSkill().length; i++) {
            m.writer().writeByte(player.getSkill().getLevelSkill()[i]);
        }
        m.writer().writeShort(player.getInfo().getKiller());
        m.writer().writeByte(player.getInfo().getGender());
        m.writer().writeByte(player.getHorse().getUseHorse());
        m.writer().writeByte(player.getHorse().isFly() ? 1 : 0);
        m.writer().writeByte(player.getHorse().getImageHorse());
        m.writer().writeByte(player.getPoint().getSpeed());
        Clan clan = player.getInfo().getClan();
        m.writer().writeShort(clan == null ? -1 : clan.getIndexIcon());
        if (clan != null) {
            byte role = player.getSundry().getClanMember().getIsMaster();
            m.writer().writeByte(role);
            if (role == ClanConst.BANG_CHU) {
                m.writer().writeBoolean(clan.isDissolve());
            }
        }
        m.writer().writeBoolean(false);
        m.writer().writeByte(player.getInventory().getLimItemBag());
        m.writer().writeByte(player.getInfo().getIdNation());
        m.writer().writeByte(player.getLocation().getInCountry());
        m.writer().writeShort(0);
        m.writer().writeInt(0);
        m.writer().writeShort(0);
        m.writer().writeInt(0);
        m.writer().writeUTF(player.getName());
        m.writer().writeBoolean(false);
        player.getSession().sendMessage(m);
    }
}
