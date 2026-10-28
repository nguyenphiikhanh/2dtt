package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class XaPhuTemplate {

    private byte id;

    private short[] mapID;

    private short[] x;

    private short[] y;

    private short[] price;
}
