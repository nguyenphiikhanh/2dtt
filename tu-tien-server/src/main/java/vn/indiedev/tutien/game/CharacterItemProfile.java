package vn.indiedev.tutien.game;

public record CharacterItemProfile(
        long id,
        int templateId,
        String name,
        String slot,
        int spriteStyle,
        int iconId,
        String genderType,
        int requiredLevel,
        boolean equipped
) {
}
