package vn.indiedev.tutien.game;

import java.util.List;

public record AccountProfile(long id, String username, List<CharacterProfile> characters) {
}
