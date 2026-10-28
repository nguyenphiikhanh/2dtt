package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class GemTemplate {

    private byte type;
    private byte typeEp;
    private String name;
    private String decript;
    private byte idImage;
    private short id;
    private byte typeMoney;
    private boolean isSell;
    private int price;
}
