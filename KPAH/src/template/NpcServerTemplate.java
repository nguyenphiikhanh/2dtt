package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class NpcServerTemplate {

    private short id;
    private String name;
    private short idImage;
    private short w0;
    private short h0;
    private byte frame;
    private byte typeLimit;
}
