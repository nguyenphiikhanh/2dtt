package vn.indiedev.tutien.game;

public record WorldPosition(long characterId, String characterName, int classId, String className,
                            int gender, int headId, int nationId, int mapId, String mapName,
                            int x, int y, int mapWidth, int mapHeight) {
}
