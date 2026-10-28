package vn.indiedev.tutien.game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public final class GameService {

    private static final KpahStarterMap STARTER_MAP = KpahStarterMap.get();
    private static final int STARTER_MAP_ID = STARTER_MAP.id();
    private static final int MAX_MOVE_STEP = 8;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,24}");
    private static final Pattern CHARACTER_NAME_PATTERN = Pattern.compile("[\\p{L}\\p{N}_]{3,18}");
    private static final Set<String> CULTIVATOR_TYPES = Set.of("NAM_TU", "NU_TU");
    private static final Set<String> SPIRIT_TYPES = Set.of("THANH_LONG", "BACH_HO", "HUYEN_VU", "CHU_TUOC");
    private static final String[] MALE_STARTER_ITEMS = {"2", "28", "4", "30"};
    private static final String[] FEMALE_STARTER_ITEMS = {"1", "27", "3", "29"};
    private static final int MAX_CHARACTERS = 3;

    private final DataSource dataSource;
    private final PasswordHasher passwordHasher = new PasswordHasher();

    public GameService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public AccountProfile register(String username, char[] password) throws SQLException, GameException {
        String normalizedUsername = normalizeUsername(username);
        validatePassword(password);

        String passwordHash = passwordHasher.hash(password);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO accounts (username, password_hash) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalizedUsername);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Account insert returned no generated id");
                }
                return new AccountProfile(keys.getLong(1), normalizedUsername, List.of());
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new GameException("username_taken", "Tên tài khoản đã được sử dụng.");
            }
            throw e;
        }
    }

    public AccountProfile login(String username, char[] password) throws SQLException, GameException {
        String normalizedUsername = normalizeUsername(username);
        if (password == null || password.length == 0 || password.length > 128) {
            throw invalidCredentials();
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, username, password_hash FROM accounts WHERE username = ? LIMIT 1")) {
            statement.setString(1, normalizedUsername);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next() || !passwordHasher.verify(password, rows.getString("password_hash"))) {
                    throw invalidCredentials();
                }
                long accountId = rows.getLong("id");
                String accountName = rows.getString("username");
                return new AccountProfile(accountId, accountName, listCharacters(connection, accountId));
            }
        }
    }

    public WorldPosition enterWorld(long accountId, long characterId) throws SQLException, GameException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, name, cultivator_type, spirit_type, current_map_id, position_x, position_y "
                             + "FROM characters WHERE id = ? AND account_id = ?")) {
            statement.setLong(1, characterId);
            statement.setLong(2, accountId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    throw new GameException("character_not_found", "Không tìm thấy nhân vật thuộc tài khoản này.");
                }
                int mapId = row.getInt("current_map_id");
                if (mapId != STARTER_MAP_ID) {
                    throw new GameException("map_not_found", "Bản đồ hiện tại chưa được mở.");
                }
                return new WorldPosition(row.getLong("id"), row.getString("name"),
                        row.getString("cultivator_type"), row.getString("spirit_type"), mapId,
                        STARTER_MAP.name(), row.getInt("position_x"), row.getInt("position_y"),
                        STARTER_MAP.pixelWidth(), STARTER_MAP.pixelHeight());
            }
        }
    }

    public WorldPosition moveCharacter(long accountId, long characterId, int dx, int dy)
            throws SQLException, GameException {
        if (Math.abs(dx) > MAX_MOVE_STEP || Math.abs(dy) > MAX_MOVE_STEP || (dx == 0 && dy == 0)) {
            throw new GameException("invalid_move", "Bước di chuyển không hợp lệ.");
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                WorldPosition current;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name, cultivator_type, spirit_type, current_map_id, position_x, position_y "
                                + "FROM characters WHERE id = ? AND account_id = ? FOR UPDATE")) {
                    statement.setLong(1, characterId);
                    statement.setLong(2, accountId);
                    try (ResultSet row = statement.executeQuery()) {
                        if (!row.next()) {
                            throw new GameException("character_not_found", "Không tìm thấy nhân vật thuộc tài khoản này.");
                        }
                        int mapId = row.getInt("current_map_id");
                        if (mapId != STARTER_MAP_ID) {
                            throw new GameException("map_not_found", "Bản đồ hiện tại chưa được mở.");
                        }
                        current = new WorldPosition(row.getLong("id"), row.getString("name"),
                                row.getString("cultivator_type"), row.getString("spirit_type"), mapId,
                                STARTER_MAP.name(), row.getInt("position_x"), row.getInt("position_y"),
                                STARTER_MAP.pixelWidth(), STARTER_MAP.pixelHeight());
                    }
                }
                int x = current.x() + dx;
                int y = current.y() + dy;
                if (!STARTER_MAP.isWalkable(x, y)) {
                    connection.commit();
                    return current;
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE characters SET position_x = ?, position_y = ? WHERE id = ? AND account_id = ?")) {
                    update.setInt(1, x);
                    update.setInt(2, y);
                    update.setLong(3, characterId);
                    update.setLong(4, accountId);
                    update.executeUpdate();
                }
                connection.commit();
                return new WorldPosition(current.characterId(), current.characterName(), current.cultivatorType(),
                        current.spiritType(), current.mapId(), current.mapName(), x, y,
                        current.mapWidth(), current.mapHeight());
            } catch (SQLException | GameException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public CharacterProfile createCharacter(long accountId, String name,
                                             String cultivatorType, String spiritType)
            throws SQLException, GameException {
        String safeName = name == null ? "" : name.strip();
        if (!CHARACTER_NAME_PATTERN.matcher(safeName).matches()) {
            throw new GameException("invalid_character_name", "Tên nhân vật cần dài 3–18 ký tự, chỉ gồm chữ, số hoặc gạch dưới.");
        }
        if (!CULTIVATOR_TYPES.contains(cultivatorType)) {
            throw new GameException("invalid_cultivator_type", "Hãy chọn Nam Tu hoặc Nữ Tu.");
        }
        if (!SPIRIT_TYPES.contains(spiritType)) {
            throw new GameException("invalid_spirit_type", "Linh thể đã chọn không hợp lệ.");
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement accountLock = connection.prepareStatement(
                        "SELECT id FROM accounts WHERE id = ? FOR UPDATE")) {
                    accountLock.setLong(1, accountId);
                    try (ResultSet rows = accountLock.executeQuery()) {
                        if (!rows.next()) {
                            throw new GameException("unauthenticated", "Phiên đăng nhập không còn hợp lệ.");
                        }
                    }
                }

                try (PreparedStatement count = connection.prepareStatement(
                        "SELECT COUNT(*) FROM characters WHERE account_id = ?")) {
                    count.setLong(1, accountId);
                    try (ResultSet rows = count.executeQuery()) {
                        rows.next();
                        if (rows.getInt(1) >= MAX_CHARACTERS) {
                            throw new GameException("character_limit", "Mỗi tài khoản tạo tối đa 3 nhân vật.");
                        }
                    }
                }

                long characterId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO characters (account_id, name, cultivator_type, spirit_type, current_map_id, position_x, position_y) "
                                + "VALUES (?, ?, ?, ?, 105, 184, 280)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, accountId);
                    insert.setString(2, safeName);
                    insert.setString(3, cultivatorType);
                    insert.setString(4, spiritType);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Character insert returned no generated id");
                        }
                        characterId = keys.getLong(1);
                    }
                }
                createStarterItems(connection, characterId, cultivatorType);
                CharacterProfile createdCharacter = new CharacterProfile(characterId, safeName,
                        cultivatorType, spiritType, "MORTAL", 1, listCharacterItems(connection, characterId));
                connection.commit();
                return createdCharacter;
            } catch (SQLException | GameException e) {
                connection.rollback();
                if (e instanceof SQLException sqlException && sqlException.getErrorCode() == 1062) {
                    throw new GameException("character_name_taken", "Tên nhân vật đã được sử dụng.");
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private List<CharacterProfile> listCharacters(Connection connection, long accountId) throws SQLException {
        List<CharacterRow> rowsToLoad = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, cultivator_type, spirit_type, realm_code, level "
                        + "FROM characters WHERE account_id = ? ORDER BY id")) {
            statement.setLong(1, accountId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    rowsToLoad.add(new CharacterRow(
                            rows.getLong("id"),
                            rows.getString("name"),
                            rows.getString("cultivator_type"),
                            rows.getString("spirit_type"),
                            rows.getString("realm_code"),
                            rows.getInt("level")));
                }
            }
        }
        List<CharacterProfile> characters = new ArrayList<>(rowsToLoad.size());
        for (CharacterRow row : rowsToLoad) {
            characters.add(new CharacterProfile(row.id(), row.name(), row.cultivatorType(),
                    row.spiritType(), row.realmCode(), row.level(), listCharacterItems(connection, row.id())));
        }
        return characters;
    }

    public CharacterProfile equipItem(long accountId, long characterId, long itemId)
            throws SQLException, GameException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String cultivatorType;
                int characterLevel;
                try (PreparedStatement character = connection.prepareStatement(
                        "SELECT cultivator_type, level FROM characters WHERE id = ? AND account_id = ? FOR UPDATE")) {
                    character.setLong(1, characterId);
                    character.setLong(2, accountId);
                    try (ResultSet row = character.executeQuery()) {
                        if (!row.next()) {
                            throw new GameException("character_not_found", "Không tìm thấy nhân vật thuộc tài khoản này.");
                        }
                        cultivatorType = row.getString("cultivator_type");
                        characterLevel = row.getInt("level");
                    }
                }

                String slot;
                int requiredLevel;
                String requiredGender;
                try (PreparedStatement item = connection.prepareStatement(
                        "SELECT t.slot_code, t.required_level, t.gender_type "
                                + "FROM character_items i JOIN equipment_templates t ON t.template_id = i.template_id "
                                + "WHERE i.id = ? AND i.character_id = ?")) {
                    item.setLong(1, itemId);
                    item.setLong(2, characterId);
                    try (ResultSet row = item.executeQuery()) {
                        if (!row.next()) {
                            throw new GameException("item_not_found", "Trang bị không nằm trong hành trang nhân vật.");
                        }
                        slot = row.getString("slot_code");
                        requiredLevel = row.getInt("required_level");
                        requiredGender = row.getString("gender_type");
                    }
                }

                if (!cultivatorType.equals(requiredGender)) {
                    throw new GameException("wrong_gender", "Trang bị này không phù hợp với nhân vật.");
                }
                if (characterLevel < requiredLevel) {
                    throw new GameException("level_required", "Nhân vật chưa đủ cấp để mặc trang bị này.");
                }

                try (PreparedStatement equip = connection.prepareStatement(
                        "INSERT INTO character_equipment (character_id, slot_code, item_instance_id) VALUES (?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE item_instance_id = VALUES(item_instance_id), equipped_at = CURRENT_TIMESTAMP")) {
                    equip.setLong(1, characterId);
                    equip.setString(2, slot);
                    equip.setLong(3, itemId);
                    equip.executeUpdate();
                }
                CharacterProfile updatedCharacter;
                try (PreparedStatement character = connection.prepareStatement(
                        "SELECT id, name, cultivator_type, spirit_type, realm_code, level FROM characters "
                                + "WHERE id = ? AND account_id = ?")) {
                    character.setLong(1, characterId);
                    character.setLong(2, accountId);
                    try (ResultSet row = character.executeQuery()) {
                        if (!row.next()) {
                            throw new GameException("character_not_found", "Không tìm thấy nhân vật thuộc tài khoản này.");
                        }
                        updatedCharacter = new CharacterProfile(row.getLong("id"), row.getString("name"),
                                row.getString("cultivator_type"), row.getString("spirit_type"),
                                row.getString("realm_code"), row.getInt("level"),
                                listCharacterItems(connection, characterId));
                    }
                }
                connection.commit();
                return updatedCharacter;
            } catch (SQLException | GameException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void createStarterItems(Connection connection, long characterId, String cultivatorType) throws SQLException {
        String[] templateIds = "NAM_TU".equals(cultivatorType) ? MALE_STARTER_ITEMS : FEMALE_STARTER_ITEMS;
        try (PreparedStatement insertItem = connection.prepareStatement(
                "INSERT INTO character_items (character_id, template_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement equipItem = connection.prepareStatement(
                     "INSERT INTO character_equipment (character_id, slot_code, item_instance_id) VALUES (?, ?, ?)")) {
            for (int index = 0; index < templateIds.length; index++) {
                int templateId = Integer.parseInt(templateIds[index]);
                insertItem.setLong(1, characterId);
                insertItem.setInt(2, templateId);
                insertItem.executeUpdate();
                try (ResultSet generatedKeys = insertItem.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Starter item insert returned no generated id");
                    }
                    if (index < 2) {
                        equipItem.setLong(1, characterId);
                        equipItem.setString(2, index == 0 ? "SHIRT" : "PANTS");
                        equipItem.setLong(3, generatedKeys.getLong(1));
                        equipItem.executeUpdate();
                    }
                }
            }
        }
    }

    private List<CharacterItemProfile> listCharacterItems(Connection connection, long characterId) throws SQLException {
        List<CharacterItemProfile> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT i.id, t.template_id, t.name, t.slot_code, t.sprite_style, t.icon_id, "
                        + "t.gender_type, t.required_level, (e.item_instance_id IS NOT NULL) AS equipped "
                        + "FROM character_items i JOIN equipment_templates t ON t.template_id = i.template_id "
                        + "LEFT JOIN character_equipment e ON e.character_id = i.character_id AND e.item_instance_id = i.id "
                        + "WHERE i.character_id = ? ORDER BY t.slot_code, i.id")) {
            statement.setLong(1, characterId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    items.add(new CharacterItemProfile(rows.getLong("id"), rows.getInt("template_id"),
                            rows.getString("name"), rows.getString("slot_code"), rows.getInt("sprite_style"),
                            rows.getInt("icon_id"), rows.getString("gender_type"),
                            rows.getInt("required_level"), rows.getBoolean("equipped")));
                }
            }
        }
        return items;
    }

    private String normalizeUsername(String username) throws GameException {
        String normalized = username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new GameException("invalid_username", "Tài khoản cần dài 3–24 ký tự, chỉ gồm chữ không dấu, số hoặc gạch dưới.");
        }
        return normalized;
    }

    private void validatePassword(char[] password) throws GameException {
        if (password == null || password.length < 8 || password.length > 128) {
            throw new GameException("invalid_password", "Mật khẩu cần dài từ 8 đến 128 ký tự.");
        }
    }

    private GameException invalidCredentials() {
        return new GameException("invalid_credentials", "Tài khoản hoặc mật khẩu không chính xác.");
    }

    private record CharacterRow(long id, String name, String cultivatorType, String spiritType,
                                String realmCode, int level) {
    }
}
