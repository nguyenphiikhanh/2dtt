package vn.indiedev.tutien.game;

public final class GameException extends Exception {

    private final String code;

    public GameException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
