package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class PotionTemplate {

    private short id;
    private short idImage;
    private short delay;
    private boolean isTrade;
    private String name;
    private String name2;
    private short price;
    private short recovered;
}
