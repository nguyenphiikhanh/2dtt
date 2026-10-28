package top;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
@Data
@Builder
public class ClanInfo {

    private short indexIcon;
    private String name;
    private String nameLeader;
    private byte level;
    private short members;
    private long xu;
    private byte nationId;
}
