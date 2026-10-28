package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class HoaTieuTemplate {

    private byte id;
    private short[][] mapId;
    private String[] nameMap;
    private String[][] nameMapChild;
    private short[] x;
    private short[] y;
}
