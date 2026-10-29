package vn.indiedev.tutien.game;

import java.util.List;

public record CharacterProfile(
        long id,
        String name,
        int classId,
        String className,
        int gender,
        int headId,
        int nationId,
        String nationName,
        int elementId,
        String elementName,
        int level,
        int strength,
        int agility,
        int spirit,
        int health,
        int luck,
        int basePoints,
        int skillPoints,
        long exp,
        int hp,
        int maxHp,
        int mp,
        int maxMp,
        long xu,
        int luong,
        int luongKhoa,
        List<CharacterItemProfile> items
) {
}
