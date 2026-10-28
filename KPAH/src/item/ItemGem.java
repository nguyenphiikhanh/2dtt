package item;

import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import template.GemTemplate;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ItemGem {

    private short idItem;
    private GemTemplate template;
    private short quantity;

    public void dispose() {
        this.template = null;
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
        arr.put(idItem);
        arr.put(template.getId());
        arr.put(quantity);
        return arr.toString();
    }
}
