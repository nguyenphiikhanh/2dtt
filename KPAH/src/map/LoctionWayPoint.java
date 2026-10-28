package map;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class LoctionWayPoint {

    private short toMap;
    private short toX;
    private short toY;
    private short x;
    private short y;
}
