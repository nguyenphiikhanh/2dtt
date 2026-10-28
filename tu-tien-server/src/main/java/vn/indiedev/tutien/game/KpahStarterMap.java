package vn.indiedev.tutien.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

final class KpahStarterMap {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final KpahStarterMap INSTANCE = load();

    private final int id;
    private final String name;
    private final int tileSize;
    private final int width;
    private final int height;
    private final int spawnX;
    private final int spawnY;
    private final int[] collision;

    private KpahStarterMap(JsonNode map) {
        id = map.path("id").asInt();
        name = map.path("name").asText("KPAH Map 105");
        tileSize = map.path("tileSize").asInt(16);
        width = map.path("width").asInt();
        height = map.path("height").asInt();
        spawnX = map.path("spawn").path("x").asInt();
        spawnY = map.path("spawn").path("y").asInt();
        JsonNode collisionData = map.path("collision");
        if (!collisionData.isArray() || collisionData.size() != width * height) {
            throw new IllegalStateException("KPAH starter map has invalid collision data");
        }
        collision = new int[collisionData.size()];
        for (int i = 0; i < collision.length; i++) {
            collision[i] = collisionData.get(i).asInt();
        }
    }

    static KpahStarterMap get() { return INSTANCE; }

    private static KpahStarterMap load() {
        try (InputStream input = KpahStarterMap.class.getClassLoader()
                .getResourceAsStream("static/assets/kpah/maps/105.json")) {
            if (input == null) throw new IllegalStateException("KPAH starter map asset is missing");
            return new KpahStarterMap(JSON.readTree(input));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load KPAH starter map asset", exception);
        }
    }

    int id() { return id; }
    String name() { return name; }
    int pixelWidth() { return width * tileSize; }
    int pixelHeight() { return height * tileSize; }
    int spawnX() { return spawnX; }
    int spawnY() { return spawnY; }

    boolean isWalkable(int x, int y) {
        int tileX = x / tileSize;
        int tileY = y / tileSize;
        if (x < 0 || y < 0 || tileX >= width || tileY >= height) return false;
        return (collision[tileY * width + tileX] & 2) != 2;
    }
}
