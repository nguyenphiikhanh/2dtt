package item;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;
import template.AnimalTemplate;
import utils.Util;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ItemAnimal {

    private short id;
    private byte level;
    private AnimalTemplate template;
    private List<Attribute> attributes;
    private List<ItemEquip> itemBody;
    private int minutes;
    private long timeStart;

    public String getInfo() {
        StringBuilder st = new StringBuilder();
        for (Attribute att : attributes) {
            if (att != null) {
                switch (att.getTemplate().getId()) {
                    case 56 ->
                        st.append("PT vật tăng: ").append(att.getValue()).append("%\n");
                    case 60 ->
                        st.append("PT ma tăng: ").append(att.getValue()).append("%\n");
                    case 30 ->
                        st.append("Tấn công tăng: ").append(att.getValue()).append("%\n");
                    case 33 ->
                        st.append("Tăng: ").append(att.getValue()).append(" HP\n");
                    case 34 ->
                        st.append("Tăng: ").append(att.getValue()).append(" MP\n");
                    case 5 ->
                        st.append("Tăng sức khỏe: ").append(att.getValue()).append("\n");
                    case 26 ->
                        st.append("Tỷ lệ nhận x2: ").append(att.getValue()).append("%\n");
                    case 81 ->
                        st.append("Hấp thu ").append(att.getValue()).append("%").append(" sát thương tỷ lệ: ").append(att.getValue()).append("%\n");
                    case 28 ->
                        st.append("Tỉ lệ giảm sát thương: ").append(att.getValue()).append("%\n");
                    case 36 ->
                        st.append("Tăng khéo léo: ").append(att.getValue()).append("\n");
                    default -> {
                        String name = att.getTemplate().getName();
                        if (name.startsWith("Tăng ")) {
                            name = name.replace("Tăng ", "");
                            st.append(Util.capitalizeFirstLetter(name)).append(" tăng: ").append(att.getValue()).append((att.getTemplate().getIsPercent() != 0 ? " %\n" : "\n"));
                        } else {
                            st.append(Util.capitalizeFirstLetter(name)).append(": ").append(att.getValue()).append((att.getTemplate().getIsPercent() != 0 ? " %\n" : "\n"));
                        }
                    }
                }
            }
        }
        return st.toString();
    }

    public void dispose() {
        template = null;
        if (attributes != null) {
            for (Attribute att : attributes) {
                att.dispose();
            }
            attributes.clear();
        }
        attributes = null;
    }

    public void plusLevel(byte level) {
        if (level <= 0 || this.level + level <= 0) {
            return;
        }
        this.level += level;
    }

    public short getValue(byte idAtt) {
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            if (attribute != null && attribute.getTemplate().getId() == idAtt) {
                return attribute.getValue();
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        arr.put(template.getIdTrung());
        arr.put(level);
        try {
            arr.put(new JSONArray(attributes.toString()));
            arr.put(new JSONArray(itemBody.toString()));
        } catch (JSONException ex) {
        }
        arr.put(minutes);
        arr.put(timeStart);
        return arr.toString();
    }
}
