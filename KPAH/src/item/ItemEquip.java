package item;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;
import template.ItemEquipTemplate;
import consts.ItemEquipConst;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ItemEquip {

    private short idItem;
    private ItemEquipTemplate template;
    private byte plusTemplate;
    private byte classChar;
    private boolean isLock;
    private short durable;
    private short mDurable;
    private byte level;
    private byte colorName;
    private byte viTriVe;
    private byte he;
    private byte rank;
    private byte damageType;
    private String nameCharSeal;
    private List<Attribute> itemAttributes;
    private int dayUse;
    private long timeCreateItem;

    public void minusDurable() {
        mDurable--;
        if (mDurable % 10 == 0) {
            durable--;
        }
    }

    public boolean isJewelry() {
        return template.getType() == 8 || template.getType() == 9 || template.getType() == 12;
    }

    public boolean isArmor() {
        return template.getType() == 0 || template.getType() == 1 || template.getType() == 2 || template.getType() == 10 || template.getType() == 11;
    }

    public boolean isWeapon() {
        return template.getType() >= 3 && template.getType() <= 7;
    }

    public boolean isAnimalArmor() {
        return template.getType() >= 14 && template.getType() <= 18;
    }

    public void subDefend(byte typeBuy) {
        if (template.getType() < 2 || template.getType() == 10 || template.getType() == 11 || isAnimalArmor()) {
            short thuVat = getValue((byte) 1);
            if (typeBuy == ItemEquipConst.DAMAGE_MAGIC) {
                setValue((byte) 6, thuVat);
                setValue((byte) 1, (short) (thuVat / 10));
            } else if (typeBuy == ItemEquipConst.DAMAGE_PHYSIC) {
                setValue((byte) 6, (short) (thuVat / 10));
            }
        }
    }

    public short getValue(byte idAtt) {
        for (int i = 0; i < itemAttributes.size(); i++) {
            Attribute attribute = itemAttributes.get(i);
            if (attribute != null && attribute.getTemplate().getId() == idAtt) {
                return attribute.getValue();
            }
        }
        return 0;
    }

    public void setValue(byte idAtt, short valueNew) {
        for (int i = 0; i < itemAttributes.size(); i++) {
            Attribute attribute = itemAttributes.get(i);
            if (attribute != null && attribute.getTemplate().getId() == idAtt) {
                attribute.setValue(valueNew);
                break;
            }
        }
    }

    public void dispose() {
        template = null;
        if (itemAttributes != null) {
            for (Attribute att : itemAttributes) {
                att.dispose();
            }
            itemAttributes.clear();
        }
        itemAttributes = null;
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(idItem);
        arr.put(template.getId());
        arr.put(classChar);
        arr.put(level);
        arr.put(plusTemplate);
        arr.put(colorName);
        arr.put(isLock);
        arr.put(mDurable);
        arr.put(durable);
        arr.put(viTriVe);
        arr.put(rank);
        arr.put(damageType);
        arr.put(nameCharSeal);
        arr.put(dayUse);
        arr.put(timeCreateItem);
        arr.put(he);
        try {
            arr.put(new JSONArray(itemAttributes.toString()));
        } catch (JSONException ex) {
        }
        return arr.toString();
    }
}
