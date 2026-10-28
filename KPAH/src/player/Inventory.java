package player;

import consts.ItemEquipConst;
import item.ItemAnimal;
import item.ItemEquip;
import item.ItemGem;
import item.ItemPotion;
import item.ItemQuest;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import manager.Manager;
import org.json.JSONArray;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class Inventory {

    private int luong;
    private int luongKhoa;
    private long xu;
    private byte limItemBag;
    private short maxIdItem;
    private List<ItemEquip> itemBody;
    private List<ItemEquip> itemBag;
    private List<ItemEquip> itemBox;
    private List<ItemEquip> itemSold;
    private List<ItemGem> itemGem;
    private List<ItemGem> itemGemLock;
    private List<ItemPotion> itemPotion;
    private List<ItemQuest> itemQuest;
    private List<ItemAnimal> itemAnimal;
    private List<ItemAnimal> itemAnimalExpiry;

    @Builder.Default
    private final long[] lastTimeUsePotion = new long[Manager.POTION_TEMPLATES.size()];

    public void initIdItem() {
        maxIdItem = Short.MIN_VALUE;
        for (ItemEquip item : itemBag) {
            if (item != null) {
                item.setIdItem(++maxIdItem);
            }
        }
        for (ItemEquip item : itemBox) {
            if (item != null) {
                item.setIdItem(++maxIdItem);
            }
        }
        for (ItemEquip item : itemBody) {
            if (item != null) {
                item.setIdItem(++maxIdItem);
            }
        }
        for (ItemAnimal item : itemAnimal) {
            if (item != null) {
                for (ItemEquip itemE : item.getItemBody()) {
                    if (itemE != null) {
                        itemE.setIdItem(++maxIdItem);
                    }
                }
            }
        }
        for (ItemEquip item : itemSold) {
            if (item != null) {
                item.setIdItem(++maxIdItem);
            }
        }
        for (ItemGem item : itemGem) {
            if (item != null) {
                item.setIdItem(++maxIdItem);
            }
        }
        for (ItemGem item : itemGemLock) {
            if (item != null) {
                item.setIdItem(++maxIdItem);
            }
        }
    }

    public int getPriceRepair(int type) {
        int num = 0;
        int num2 = 0;
        for (int i = 0; i < itemBody.size(); i++) {
            ItemEquip item = itemBody.get(i);
            if (item.getTemplate().getType() == 3 || item.getTemplate().getType() == 4 || item.getTemplate().getType() == 5 || item.getTemplate().getType() == 6 || item.getTemplate().getType() == 7) {
                num2 += item.getTemplate().getPrice() / 10;
            } else {
                num += item.getTemplate().getPrice() / 10;
            }
        }
        return switch (type) {
            case ItemEquipConst.REPAIR_EQUIP ->
                num;
            case ItemEquipConst.REPAIR_WEAPON ->
                num2;
            default ->
                num2 + num;
        };
    }

    public boolean plusLuong(int luong) {
        if (luong <= 0 || this.luong + luong < 0) {
            return false;
        }
        this.luong += luong;
        return true;
    }

    public boolean plusLuongKhoa(int luongKhoa) {
        if (luongKhoa <= 0 || this.luongKhoa + luongKhoa < 0) {
            return false;
        }
        this.luongKhoa += luongKhoa;
        return true;
    }

    public boolean plusXu(long xu) {
        if (xu <= 0 || this.xu + xu < 0) {
            return false;
        }
        this.xu += xu;
        return true;
    }

    public boolean minusLuong(int luong) {
        if (luong <= 0 || this.luong < luong) {
            return false;
        }
        this.luong -= luong;
        return true;
    }

    public boolean minusLuongKhoa(int luongKhoa) {
        if (luongKhoa <= 0 || this.luongKhoa < luongKhoa) {
            return false;
        }
        this.luongKhoa -= luongKhoa;
        return true;
    }

    public boolean minusXu(long xu) {
        if (xu <= 0 || this.xu < xu) {
            return false;
        }
        this.xu -= xu;
        return true;
    }

    public boolean isFullInventory() {
        return fullInventory() > 42 * limItemBag;
    }

    public int fullInventory() {
        return itemBody.size() + itemPotion.size() + itemQuest.size() + itemGem.size() + itemGemLock.size() + itemAnimal.size() + itemAnimalExpiry.size();
    }

    public void dispose() {
        if (itemSold != null) {
            for (ItemEquip it : itemSold) {
                it.dispose();
            }
            this.itemSold.clear();
        }
        if (itemBody != null) {
            for (ItemEquip it : itemBody) {
                it.dispose();
            }
            this.itemBody.clear();
        }
        if (itemPotion != null) {
            for (ItemPotion it : itemPotion) {
                it.dispose();
            }
            this.itemPotion.clear();
        }
        if (itemBag != null) {
            for (ItemEquip it : itemBag) {
                it.dispose();
            }
            this.itemBag.clear();
        }
        if (itemQuest != null) {
            for (ItemQuest it : itemQuest) {
                it.dispose();
            }
            this.itemQuest.clear();
        }
        if (itemBox != null) {
            for (ItemEquip it : itemBox) {
                it.dispose();
            }
            this.itemBox.clear();
        }
        if (itemGem != null) {
            for (ItemGem it : itemGem) {
                it.dispose();
            }
            this.itemGem.clear();
        }
        if (itemGemLock != null) {
            for (ItemGem it : itemGemLock) {
                it.dispose();
            }
            this.itemGemLock.clear();
        }
        if (itemAnimal != null) {
            for (ItemAnimal it : itemAnimal) {
                it.dispose();
            }
            this.itemAnimal.clear();
        }
        if (itemAnimalExpiry != null) {
            for (ItemAnimal it : itemAnimalExpiry) {
                it.dispose();
            }
            this.itemAnimalExpiry.clear();
        }
        this.itemAnimalExpiry = null;
        this.itemAnimal = null;
        this.itemGem = null;
        this.itemBox = null;
        this.itemPotion = null;
        this.itemBody = null;
        this.itemBag = null;
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(luong);
        arr.put(luongKhoa);
        arr.put(xu);
        arr.put(limItemBag);
        arr.put(maxIdItem);
        return arr.toString();
    }
}
