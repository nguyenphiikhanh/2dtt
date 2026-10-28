package vn.indiedev.tutien.game;

import java.util.List;

public record CharacterProfile(
        long id,
        String name,
        String cultivatorType,
        String spiritType,
        String realmCode,
        int level,
        List<CharacterItemProfile> items
) {
}
