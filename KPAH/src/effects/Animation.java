package effects;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@AllArgsConstructor
public class Animation {

    private byte[] frame;

    @Override
    public String toString() {
        try {
            JSONArray arr = new JSONArray(frame);
            return arr.toString();
        } catch (JSONException ex) {
        }
        return "[]";
    }
}
