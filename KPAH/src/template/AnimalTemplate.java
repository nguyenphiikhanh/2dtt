package template;

import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class AnimalTemplate {

    private short idTrung;
    private short idImage;
    private String name;
    private byte nFrame;
    private byte type;
    private ConcurrentHashMap<Short, ValueAttributeAnimal> attributeSpecial;

    public short getMaxValueSpecial(short id) {
        ValueAttributeAnimal valueAttributeAnimal = attributeSpecial.getOrDefault(id, null);
        if (valueAttributeAnimal == null) {
            return -1;
        }
        return valueAttributeAnimal.getMaxValue()[0];
    }
}
