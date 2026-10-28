package template;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class SkillNewTemplate {

    private short id;
    private String name;
    private String decript;
    private byte idSkill;
    private byte classChar;
    private int price;
}
