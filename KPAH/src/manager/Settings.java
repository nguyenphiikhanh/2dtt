package manager;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class Settings {

    public static final int MAX_PLAYER = 40000; // không được vượt quá 60000

    public static final String NAME_SERVER = "KPAH DUY";
    public static final String DATABASE = "kpah";
    public static final String HOST = "127.0.0.1:3306";
    public static final String USER = "root";
    public static final String PASS = "";
    public static final int PORT_SERVER = 19129;

    public static final byte[] KEYS = "kpah".getBytes();
    public static final byte DAY_WAIT_FOR_DELETE = 7;

    public static final String NUMBER_SUPPORT = "nhập sđt";
    public static final String URL = "localhost";

    public static final int SECOND_WAIT_LOGIN = 30;
    public static final int MILISECOND_WAIT_LOGIN = 30000;
    public static final short SECOND_REVIVE_PLAYER = 30;
    public static final int MILISECOND_REVIVE_PLAYER = 30000;
    public static final byte LEVEL_CAN_AUTO_REVIVE = 10;

    public static final byte EXP_DONATE = 1;
    public static final byte PERCENT_EXP_PARTY = 5;
    public static final byte MAX_PLAYER_IN_PARTY = 10;

    public static final int MILISECOND_UPDATE_DATABASE = 300000;

    public static final int TIME_LIVE_MOB = 8000;
    public static final short DISTANCE_MOB_CAN_ATTACK = 90;

    public static final int MILISECOND_WAIT_KICK_SESSION = 60000;
    public static final int MILISECOND_WAIT_KICK_PLAYER = 600000;

    public static final String[] ICON = {
        "█▀▄ █▀▀ █░█ █▀▀ █░░ █▀█ █▀█ █▀▀ █▀▄   █▄▄ █▄█   █▀▄ █░█ █▄█   █▄▀ █░█ █▀█ █▄░█ █▀▀   █▀█ █░█ ▄▀█ █",
        "█▄▀ ██▄ ▀▄▀ ██▄ █▄▄ █▄█ █▀▀ ██▄ █▄▀   █▄█ ░█░   █▄▀ █▄█ ░█░   █░█ █▀█ █▄█ █░▀█ █▄█   █▀▀ █▀█ █▀█ █\n\n"
    };

    public static final String[] LOGO_GAME = {
        " ██ ▄█▀    ██▓███        ▄▄▄       ██░ ██ ",
        " ██▄█▒    ▓██░  ██▒    ▒████▄     ▓██░ ██▒",
        "▓███▄░    ▓██░ ██▓▒   ▒██  ▀█▄    ▒██▀▀██░",
        "▓██ █▄    ▒██▄█▓▒ ▒   ░██▄▄▄▄██  ░▓█  ░██",
        "▒██▒ █▄   ▒██▒ ░  ░    ▓█   ▓██▒ ░▓█▒ ▒██▓",
        "▒ ▒▒ ▓▒   ▒▓▒░ ░  ░    ▒▒   ▓▒█░   ▒ ░░▒░▒",
        "░ ░▒ ▒░   ░▒ ░          ▒   ▒▒ ░    ▒ ░▒░ ░",
        "░ ░░ ░    ░░            ░   ▒       ░  ░░ ░",
        "░  ░                        ░  ░    ░  ░  ░\n"
    };
}
