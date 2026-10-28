package vn.indiedev.tutien.game;

public record WorldPosition(long characterId, String characterName, String cultivatorType,
                            String spiritType, int mapId, String mapName,
                            int x, int y, int mapWidth, int mapHeight) {
}
