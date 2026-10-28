package item;

import lombok.Builder;
import lombok.Data;
import lombok.Synchronized;
import org.json.JSONArray;
import template.PotionTemplate;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ItemPotion {

    private PotionTemplate template;
    private int quantity;

    public void dispose() {
        template = null;
    }

    public boolean isHpAverage() {
        short id = template.getId();
        return id == 1 || id == 2 || id == 3 || id == 21 || id == 22 || id == 93 || id == 94;
    }

    public boolean isMpAverage() {
        short id = template.getId();
        return id == 4 || id == 5 || id == 6 || id == 23 || id == 24 || id == 95 || id == 96;
    }

    public void minusQuantity(int quantity) {
        if (quantity <= 0) {
            return;
        }
        this.quantity -= quantity;
        if (this.quantity < 0) {
            this.quantity = 0;
        }
    }

    public void plusQuantity(int quantity) {
        if (quantity <= 0) {
            return;
        }
        this.quantity += quantity;
        if (this.quantity < 0) {
            this.quantity = 0;
        }
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(template.getId());
        arr.put(quantity);
        return arr.toString();
    }
}
