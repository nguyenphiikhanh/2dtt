package map;

import lombok.Builder;
import lombok.Data;
import template.NpcServerTemplate;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class NpcServer {

    private NpcServerTemplate template;
    private short x;
    private short y;
}
