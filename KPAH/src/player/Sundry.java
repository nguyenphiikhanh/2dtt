package player;

import deposite.DepositeItemEquip;
import deposite.DepositeItemGem;
import item.ItemBuyNpc;
import item.ItemEquip;
import item.ItemGem;
import item.ItemPotion;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class Sundry {

    private boolean isInGame;
    private boolean isNewlyRevived;
    private long lastTimeRevived;
    private long lastTimeDie;
    private byte dayCanRestore;
    private long lastTimeEndDelete;
    private long lastTimeLogout;

    private byte idNpcOpen;
    private byte selected;

    private byte idOpenMenu;
    private byte selectedOption;

    private boolean comeHome;
    private long lastTimeComeHome;

    private Friend clanMember;

    private int xuQuyenGop;

    private long lastTimeUpdateDatabase;

    private byte pk;
    private long lastTimeChangePk;
    private boolean isKiller;

    private boolean isOnBoard;
    private long lastTimeOnBoard;

    private byte indexHoaTieu;
    private byte idHoaTieu;
    private byte countDie;
    private int miliSecondRevive;
    private Player trader;
    @Builder.Default
    private final List<ItemPotion> itemPotionTrade = new ArrayList<>();
    private boolean isConfirmTrade;
    private boolean isFinishTrade;
    @Builder.Default
    private List<ItemBuyNpc> itemNpcShop = new ArrayList<>();
    @Builder.Default
    private List<DepositeItemEquip> depositeItemEquips = new ArrayList<>();
    @Builder.Default
    private List<DepositeItemGem> depositeItemGems = new ArrayList<>();

    private byte npcTypeDeposite;
    private byte indexShopDeposite;
    private int priceDeposite;
    private short itemIdDeposite;
    private byte categoryDeposite;

    public int indexOfGem(@NonNull DepositeItemGem itemGem) {
        return depositeItemGems.indexOf(itemGem);
    }

    public int countItemGemDeposite(short id) {
        return (int) depositeItemGems.stream().filter(gem -> gem != null && gem.getItem().getIdItem() == id).count();
    }

    public boolean containsDepositeItemEquip(@NonNull ItemEquip itemEquip) {
        return depositeItemEquips.stream().anyMatch(i -> i != null && i.getItem().getIdItem() == itemEquip.getIdItem());
    }

    public boolean containsDepositeItemGem(@NonNull DepositeItemGem depositeItemGem) {
        return depositeItemGems.contains(depositeItemGem);
    }

    public boolean containsDepositeItemGem(int idTemplate) {
        return depositeItemGems.stream().anyMatch(g -> g != null && g.getItem().getTemplate().getId() == idTemplate);
    }

    public DepositeItemGem findItemGemDeposite(short id) {
        return depositeItemGems.stream().filter(it -> it != null && it.getIdReal() == id).findFirst().orElse(null);
    }

    public DepositeItemEquip findItemEquipDeposite(short id) {
        return depositeItemEquips.stream().filter(it -> it != null && it.getItem().getIdItem() == id).findFirst().orElse(null);
    }

    public void addDepositeItemGem(@NonNull DepositeItemGem itemGem) {
        depositeItemGems.add(itemGem);
    }

    public void addDepositeItemEquip(@NonNull DepositeItemEquip itemEquip) {
        depositeItemEquips.add(itemEquip);
    }

    public void removeDepositeItemGem(@NonNull DepositeItemGem itemGem) {
        for (int i = 0; i < depositeItemGems.size(); i++) {
            DepositeItemGem depositeItemGem = depositeItemGems.get(i);
            if (depositeItemGem != null && depositeItemGem.getIdReal() == itemGem.getIdReal()) {
                depositeItemGems.remove(i);
                break;
            }
        }
    }

    public void removeDepositeItemGem(@NonNull ItemGem itemGem) {
        depositeItemGems.removeIf(g -> g != null && g.getItem().getIdItem() == itemGem.getIdItem());
    }

    public void removeDepositeItemEquip(@NonNull ItemEquip itemEquip) {
        depositeItemEquips.removeIf(g -> g != null && g.getItem().getIdItem() == itemEquip.getIdItem());
    }

    public void plusCountDie() {
        countDie++;
        if (countDie <= 0) {
            countDie = Byte.MAX_VALUE;
        }
    }

    public boolean hasItemPotionTrade(short idTemplate) {
        return itemPotionTrade.stream().anyMatch(it -> it != null && it.getTemplate().getId() == idTemplate);
    }

    public ItemPotion findItemPotion(short id) {
        return itemPotionTrade.stream().filter(it -> it != null && it.getTemplate().getId() == id).findFirst().orElse(null);
    }

    public void dispose() {
        if (itemPotionTrade != null) {
            itemPotionTrade.clear();
        }
        for (DepositeItemEquip d : depositeItemEquips) {
            d.dispose();
        }
        depositeItemEquips.clear();
        for (DepositeItemGem d : depositeItemGems) {
            d.dispose();
        }
        depositeItemGems.clear();
        itemNpcShop.clear();
        itemNpcShop = null;
        clanMember = null;
        depositeItemEquips = null;
        depositeItemGems = null;
    }
}
