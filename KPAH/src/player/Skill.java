package player;

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
public class Skill {

    private byte[] levelSkill;
    private long[] timeLastUseSkills;
    private byte typeSkill;
    private byte typeBuffSkill;

    @Override
    public String toString() {
        JSONArray skills = new JSONArray();
        try {
            skills.put(new JSONArray(levelSkill));
        } catch (JSONException ex) {
        }
        return skills.toString();
    }
}
