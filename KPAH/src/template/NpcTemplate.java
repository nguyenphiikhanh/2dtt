package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class NpcTemplate {

    private short id;
    private String name;
    private short head;
    private short[] itemEquipment;
    private short[] idModels;
}
