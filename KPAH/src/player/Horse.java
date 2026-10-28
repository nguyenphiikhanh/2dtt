package player;

import item.ItemAnimal;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class Horse {

    private byte useHorse;
    private byte imageHorse;
    private short idItem;
    private boolean isFly;
    private ItemAnimal animalUse;

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(useHorse);
        arr.put(imageHorse);
        arr.put(idItem);
        arr.put(isFly);
        try {
            arr.put(new JSONArray(animalUse == null ? "[]" : animalUse.toString()));
        } catch (JSONException ex) {
        }
        return arr.toString();
    }
}
