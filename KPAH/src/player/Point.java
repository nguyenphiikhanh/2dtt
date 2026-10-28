package player;

import java.io.IOException;
import lombok.Builder;
import lombok.Data;
import lombok.Synchronized;
import manager.Manager;
import org.json.JSONArray;
import services.InventoryService;
import services.MapService;
import services.Service;
import consts.BuffConst;
import consts.Const;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class Point {

    private Player player;

    private int hpMax;
    @Builder.Default
    private int hp = -1;
    private int mpMax;
    @Builder.Default
    private int mp = -1;

    private int attack;
    private int defend, defendMagic;

    private short percentPlusHp;
    private short percentPlusMp;

    private short accurate;
    private short dodge;
    private short critical;
    private short x2;
    private short hapThu;

    private short baoKich;
    private short xuyenGiap;
    private short expDonate;
    private short docTinh;
    private short giamStVat;
    private short giamStMa;

    private short strength, agility, spirit, health, luck;
    private short strengthAdd, agilityAdd, spiritAdd, healthAdd;

    private long exp;

    private short basePoint, skillPoint;

    private int dedicationPoint;

    private byte speed;
    private long xuRevive;

    public void increaseSkillPoint(byte type) throws IOException {
        byte levelSkill = player.getSkill().getLevelSkill()[type];
        if (levelSkill == -1 || skillPoint <= 0 || player.getInfo().getLevel() < Manager.getLevelAddSkill(type, levelSkill)) {
            return;
        }
        if (levelSkill >= 9) {
            Service.instance.sendLogOut(player.getSession(), "Kĩ năng đạt cấp tối đa");
            return;
        }
        skillPoint -= 1;
        player.getSkill().getLevelSkill()[type] += 1;
        Service.instance.sendEndDialog(player);
        initPoint();
        Service.instance.sendMainCharInfo(player);
    }

    public void increaseBasePoint(byte type, short numIncrease) throws IOException {
        if (basePoint == 0 || basePoint - numIncrease < 0 || numIncrease <= 0) {
            return;
        }
        basePoint -= numIncrease;
        switch (type) {
            case 0 ->
                strength += numIncrease;
            case 1 ->
                agility += numIncrease;
            case 2 ->
                spirit += numIncrease;
            case 3 ->
                health += numIncrease;
            case 4 ->
                luck += numIncrease;
        }
        Service.instance.sendEndDialog(player);
        initPoint();
        Service.instance.sendMainCharInfo(player);
        MapService.instance.onNewHpMp(player);
    }

    @Synchronized
    public int getDameAttack(boolean miss, boolean isCrit, boolean isBaoKich, boolean isAttackMob) {
        if (miss) {
            return 0;
        }
        int dameAttack = (int) (this.attack * (isAttackMob ? 2.5 : 2));
        dameAttack += dameAttack * (Manager.getSkillDamPercent(player.getInfo().getClassPlayer(), player.getSkill().getTypeSkill(), player.getSkill().getLevelSkill()[player.getSkill().getTypeSkill()]) / 100);
        if (isCrit || isBaoKich) {
            dameAttack *= 2;
        }
        dameAttack = Util.nextInt((int) ((float) dameAttack * 0.7), (int) ((float) dameAttack * 0.9));
        if (dameAttack < 0) {
            dameAttack = 1;
        }
        return dameAttack;
    }

    private void setStrength() {
        strengthAdd += InventoryService.instance.sumAttributeValueForId(player, (byte) 10);
        if (player.getHorse().getImageHorse() == 1) {
            strengthAdd += 3;
        }
        if (player.getHorse().getAnimalUse() != null) {
            strengthAdd += player.getHorse().getAnimalUse().getValue((byte) 10);
        }
    }

    private void setAgility() {
        agilityAdd += InventoryService.instance.sumAttributeValueForId(player, (byte) 11);
        if (player.getHorse().getImageHorse() == 1) {
            agilityAdd += 3;
        }
        if (player.getHorse().getAnimalUse() != null) {
            agilityAdd += player.getHorse().getAnimalUse().getValue((byte) 36);
        }
    }

    private void setSpirit() {
        spiritAdd += InventoryService.instance.sumAttributeValueForId(player, (byte) 12);
        if (player.getInfo().getClassPlayer() == Const.PHAP_SU) {
            spiritAdd += spiritAdd * (Manager.getSkillDamPercent(player.getInfo().getClassPlayer(), (byte) 5, player.getSkill().getLevelSkill()[5]) / 100);
        }
        if (player.getHorse().getImageHorse() == 1) {
            spiritAdd += 3;
        }
        if (player.getHorse().getAnimalUse() != null) {
            spiritAdd += player.getHorse().getAnimalUse().getValue((byte) 12);
        }
    }

    private void setHealth() {
        healthAdd += InventoryService.instance.sumAttributeValueForId(player, (byte) 13);
        healthAdd += InventoryService.instance.sumAttributeValueForId(player, (byte) 5);
        if (player.getHorse().getImageHorse() == 1) {
            healthAdd += 3;
        }
        if (player.getHorse().getAnimalUse() != null) {
            healthAdd += player.getHorse().getAnimalUse().getValue((byte) 13);
        }
    }

    private void setAttack() {
        switch (player.getInfo().getClassPlayer()) {
            case Const.KIEM_KHACH, Const.CHIEN_BINH, Const.DAU_SI ->
                attack += strength + strengthAdd;
            case Const.PHAP_SU ->
                attack += (spirit + spiritAdd) * 2;
            case Const.CUNG_THU ->
                attack += (agility + agilityAdd) * 1.8;
        }
        attack += InventoryService.instance.sumAttributeValueForId(player, (byte) 0);
        if (player.getInfo().getClassPlayer() == Const.CHIEN_BINH) {
            attack += attack * (Manager.getSkillDamPercent(player.getInfo().getClassPlayer(), (byte) 5, player.getSkill().getLevelSkill()[5]) / 100);
        }
        if (player.getHorse().getAnimalUse() != null) {
            attack += attack * player.getHorse().getAnimalUse().getValue((byte) 30) / 100;
        }
    }

    private void setDefend() {
        defend += agility + agilityAdd;
        defend += InventoryService.instance.sumAttributeValueForId(player, (byte) 1);
        if (player.getInfo().getClassPlayer() == Const.DAU_SI) {
            defend += defend * (Manager.getSkillDamPercent(player.getInfo().getClassPlayer(), (byte) 5, player.getSkill().getLevelSkill()[5]) / 100);
        }
        if (player.getHorse().getAnimalUse() != null) {
            defend += defend * player.getHorse().getAnimalUse().getValue((byte) 56) / 100;
        }
    }

    private void setDefendMagic() {
        defendMagic += agility + agilityAdd;
        defendMagic += InventoryService.instance.sumAttributeValueForId(player, (byte) 6);
        if (player.getInfo().getClassPlayer() == Const.DAU_SI) {
            defendMagic += defendMagic * (Manager.getSkillDamPercent(player.getInfo().getClassPlayer(), (byte) 5, player.getSkill().getLevelSkill()[5]) / 100);
        }
        if (player.getHorse().getAnimalUse() != null) {
            defendMagic += defendMagic * player.getHorse().getAnimalUse().getValue((byte) 60) / 100;
        }
    }

    private void setAccurate() {
        switch (player.getInfo().getClassPlayer()) {
            case Const.CUNG_THU ->
                accurate += agility + agilityAdd;
        }
        accurate += InventoryService.instance.sumAttributeValueForId(player, (byte) 3);
    }

    private void setDodge() {
        switch (player.getInfo().getClassPlayer()) {
            case Const.CUNG_THU ->
                dodge += (agility + agilityAdd) * 0.5;
        }
        dodge += InventoryService.instance.sumAttributeValueForId(player, (byte) 2);
        if (player.getHorse().getAnimalUse() != null) {
            dodge += player.getHorse().getAnimalUse().getValue((byte) 2);
        }
    }

    private void setCrit() {
        critical += luck / 20;
        critical += InventoryService.instance.sumAttributeValueForId(player, (byte) 4) / 2;
        if (player.getHorse().getAnimalUse() != null) {
            critical += player.getHorse().getAnimalUse().getValue((byte) 40);
        }
    }

    private void setBaoKich() {
        baoKich += InventoryService.instance.sumAttributeValueForId(player, (byte) 27);
    }

    private void setDocTinh() {
        docTinh += InventoryService.instance.sumAttributeValueForId(player, (byte) 16);
        if (player.getInfo().getClassPlayer() == Const.CUNG_THU) {
            docTinh += Manager.getSkillDamPercent(player.getInfo().getClassPlayer(), (byte) 5, player.getSkill().getLevelSkill()[5]);
        }
    }

    private void setXuyenGiap() {
        xuyenGiap += InventoryService.instance.sumAttributeValueForId(player, (byte) 31);
        if (player.getInfo().getClassPlayer() == Const.KIEM_KHACH) {
            xuyenGiap += Manager.getSkillDamPercent(player.getInfo().getClassPlayer(), (byte) 4, player.getSkill().getLevelSkill()[4]);
        }
    }

    private void setExpDonate() {
        expDonate += InventoryService.instance.sumAttributeValueForId(player, (byte) 111);
    }

    private void setPercentPlusHp() {
        if (player.getInfo().getClassPlayer() == Const.PHAP_SU) {
            if (player.getSkillBuff().isExistBuff(BuffConst.HOI_CONG_LUC_DAN)) {
                percentPlusHp = player.getSkillBuff().getPercentDame(BuffConst.HOI_CONG_LUC_DAN);
            }
        }
    }

    private void setPercentPlusMp() {
        if (player.getInfo().getClassPlayer() == Const.PHAP_SU) {
            if (player.getSkillBuff().isExistBuff(BuffConst.HOI_CONG_LUC_DAN)) {
                percentPlusMp = player.getSkillBuff().getPercentDame(BuffConst.HOI_CONG_LUC_DAN);
            }
        }
    }

    private void setHpMax() {
        switch (player.getInfo().getClassPlayer()) {
            case Const.KIEM_KHACH ->
                hpMax += (health + healthAdd) * 80;
            case Const.DAU_SI, Const.CHIEN_BINH ->
                hpMax += (health + healthAdd) * 70;
            case Const.PHAP_SU, Const.CUNG_THU ->
                hpMax += (health + healthAdd) * 60;
        }
        hpMax += hpMax * percentPlusHp / 100;
        hpMax += InventoryService.instance.sumAttributeValueForId(player, (byte) 33);
        switch (player.getHorse().getImageHorse()) {
            case 0 ->
                hpMax += 700;
            case 1 ->
                hpMax += 1000;
            default -> {
                if (player.getHorse().getAnimalUse() != null) {
                    hpMax += player.getHorse().getAnimalUse().getValue((byte) 33);
                }
            }
        }
    }

    private void setMpMax() {
        switch (player.getInfo().getClassPlayer()) {
            case Const.KIEM_KHACH, Const.CHIEN_BINH, Const.DAU_SI, Const.CUNG_THU ->
                mpMax += (spirit + spiritAdd) * 20;
            case Const.PHAP_SU ->
                mpMax += (spirit + spiritAdd) * 52;
        }
        mpMax += mpMax * percentPlusMp / 100;
        mpMax += InventoryService.instance.sumAttributeValueForId(player, (byte) 34);
        switch (player.getHorse().getImageHorse()) {
            case 0 ->
                mpMax += 700;
            case 1 ->
                mpMax += 1000;
            default -> {
                if (player.getHorse().getAnimalUse() != null) {
                    mpMax += player.getHorse().getAnimalUse().getValue((byte) 34);
                }
            }
        }
    }

    private void setHp() {
        if (hp == -1) {
            hp = hpMax;
        } else if (hp == 0) {
            hp = 1;
        }
    }

    private void setMp() {
        if (mp == -1) {
            mp = mpMax;
        } else if (mp == 0) {
            mp = 1;
        }
    }

    public void plusHp(int hp) {
        if (hp < 0) {
            return;
        }
        if (this.hp + hp >= hpMax) {
            this.hp = hpMax;
        } else {
            this.hp += hp;
        }
    }

    public void plusMp(int mp) {
        if (mp < 0) {
            return;
        }
        if (this.mp + mp >= mpMax) {
            this.mp = mpMax;
        } else {
            this.mp += mp;
        }
    }

    public void minusHp(int hp) {
        if (hp < 0) {
            return;
        }
        if (this.hp >= hp) {
            this.hp -= hp;
        } else {
            this.hp = 0;
        }
    }

    public void minusMp(int mp) {
        if (mp < 0) {
            return;
        }
        if (this.mp >= mp) {
            this.mp -= mp;
        } else {
            this.mp = 0;
        }
    }

    public void plusExp(int exp) {
        if (exp < 0) {
            return;
        }
        this.exp += exp;
    }

    public void plusStrength(int point) {
        if (point < 0) {
            return;
        }
        this.strength += point;
    }

    public void plusAgility(int point) {
        if (point < 0) {
            return;
        }
        this.agility += point;
    }

    public void plusSpirit(int point) {
        if (point < 0) {
            return;
        }
        this.spirit += point;
    }

    public void plusHealth(int point) {
        if (point < 0) {
            return;
        }
        this.health += point;
    }

    public void plusLuck(int point) {
        if (point < 0) {
            return;
        }
        this.luck += point;
    }

    public void plusBasePoint(int point) {
        if (point < 0) {
            return;
        }
        this.basePoint += point;
    }

    public void plusSkillPoint(int point) {
        if (point < 0) {
            return;
        }
        this.skillPoint += point;
    }

    public boolean isFullHp() {
        return hp >= hpMax;
    }

    public boolean isFullMp() {
        return mp >= mpMax;
    }

    private void setSpeed() {
        this.speed = Const.SPEED;
        this.speed += player.getHorse().getAnimalUse() == null ? 1 : 2;
    }

    private void setX2() {
        x2 += InventoryService.instance.sumAttributeValueForId(player, (byte) 26);
        if (player.getHorse().getAnimalUse() != null) {
            x2 += player.getHorse().getAnimalUse().getValue((byte) 26);
        }
    }

    private void setHapThu() {
        hapThu += InventoryService.instance.sumAttributeValueForId(player, (byte) 81);
        if (player.getHorse().getAnimalUse() != null) {
            hapThu += player.getHorse().getAnimalUse().getValue((byte) 81);
        }
    }

    private void setGiamStVat() {
        giamStVat += InventoryService.instance.sumAttributeValueForId(player, (byte) 28);
        if (player.getHorse().getAnimalUse() != null) {
            giamStVat += player.getHorse().getAnimalUse().getValue((byte) 28);
        }
    }

    private void setGiamStMa() {
        giamStMa += InventoryService.instance.sumAttributeValueForId(player, (byte) 29);
        if (player.getHorse().getAnimalUse() != null) {
            giamStMa += player.getHorse().getAnimalUse().getValue((byte) 28);
        }
    }

    private void setXuRevive() {
        xuRevive = Util.roundNumber((long) (Math.pow(player.getInfo().getLevel(), player.getInfo().getLevel() / 10)));
        xuRevive = xuRevive < 1000 ? 1000 : xuRevive;
    }

    public void initPoint() {
        resetPoint();
        setHapThu();
        setXuRevive();
        setSpeed();
        setGiamStMa();
        setGiamStVat();
        setX2();
        setPercentPlusHp();
        setPercentPlusMp();
        setXuyenGiap();
        setDocTinh();
        setExpDonate();
        setStrength();
        setHealth();
        setAgility();
        setSpirit();
        setHpMax();
        setMpMax();
        setAttack();
        setDefend();
        setDefendMagic();
        setAccurate();
        setDodge();
        setCrit();
        setBaoKich();
        setHp();
        setMp();
    }

    private void resetPoint() {
        hpMax = 0;
        mpMax = 0;
        strengthAdd = 0;
        healthAdd = 0;
        agilityAdd = 0;
        spiritAdd = 0;
        attack = 0;
        defend = 0;
        defendMagic = 0;
        accurate = 0;
        dodge = 0;
        critical = 0;
        baoKich = 0;
        xuyenGiap = 0;
        expDonate = 0;
        docTinh = 0;
        percentPlusHp = 0;
        percentPlusMp = 0;
        speed = 0;
        x2 = 0;
        hapThu = 0;
        giamStMa = 0;
        giamStVat = 0;
        xuRevive = 0;
    }

    @Override
    public String toString() {
        JSONArray point = new JSONArray();
        point.put(hp);
        point.put(mp);
        point.put(strength);
        point.put(agility);
        point.put(spirit);
        point.put(health);
        point.put(luck);
        point.put(basePoint);
        point.put(skillPoint);
        point.put(dedicationPoint);
        point.put(baoKich);
        point.put(exp);
        return point.toString();
    }
}
