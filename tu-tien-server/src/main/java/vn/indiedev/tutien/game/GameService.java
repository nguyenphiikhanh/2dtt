package vn.indiedev.tutien.game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public final class GameService {

    private static final KpahStarterMap STARTER_MAP = KpahStarterMap.get();
    private static final int STARTER_MAP_ID = STARTER_MAP.id();
    private static final int MAX_MOVE_STEP = 24;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,24}");
    private static final Pattern CHARACTER_NAME_PATTERN = Pattern.compile("[\\p{L}\\p{N}_]{3,18}");
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
                     "SELECT id, name, class_id, gender, head_id, nation_id, current_map_id, position_x, position_y "
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
                int classId = row.getInt("class_id");
                return new WorldPosition(
                        row.getLong("id"),
                        row.getString("name"),
                        classId,
                        KpahConstants.getClassName(classId),
                        row.getInt("gender"),
                        row.getInt("head_id"),
                        row.getInt("nation_id"),
                        mapId,
                        STARTER_MAP.name(),
                        row.getInt("position_x"),
                        row.getInt("position_y"),
                        STARTER_MAP.pixelWidth(),
                        STARTER_MAP.pixelHeight());
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
                        "SELECT id, name, class_id, gender, head_id, nation_id, current_map_id, position_x, position_y "
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
                        int classId = row.getInt("class_id");
                        current = new WorldPosition(
                                row.getLong("id"),
                                row.getString("name"),
                                classId,
                                KpahConstants.getClassName(classId),
                                row.getInt("gender"),
                                row.getInt("head_id"),
                                row.getInt("nation_id"),
                                mapId,
                                STARTER_MAP.name(),
                                row.getInt("position_x"),
                                row.getInt("position_y"),
                                STARTER_MAP.pixelWidth(),
                                STARTER_MAP.pixelHeight());
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
                return new WorldPosition(
                        current.characterId(),
                        current.characterName(),
                        current.classId(),
                        current.className(),
                        current.gender(),
                        current.headId(),
                        current.nationId(),
                        current.mapId(),
                        current.mapName(),
                        x,
                        y,
                        current.mapWidth(),
                        current.mapHeight());
            } catch (SQLException | GameException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public CharacterProfile createCharacter(long accountId, String name, int classId, int gender, int headId, int nationId)
            throws SQLException, GameException {
        String safeName = name == null ? "" : name.strip();
        if (!CHARACTER_NAME_PATTERN.matcher(safeName).matches()) {
            throw new GameException("invalid_character_name", "Tên nhân vật cần dài 3–18 ký tự, chỉ gồm chữ, số hoặc gạch dưới.");
        }
        if (classId < 0 || classId > 4) {
            throw new GameException("invalid_class", "Vui lòng chọn 1 trong 5 môn phái KPAH.");
        }
        if (gender != 1 && gender != 2) {
            throw new GameException("invalid_gender", "Vui lòng chọn giới tính Nam hoặc Nữ.");
        }
        if (nationId < 0 || nationId > 2) {
            throw new GameException("invalid_nation", "Vui lòng chọn 1 trong 3 Quốc gia (Đông Sơn, Nam Sơn, Bắc Sơn).");
        }

        int[] stats = KpahConstants.getStarterStats(classId);
        int elementId = KpahConstants.getElement(classId);

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
                        "INSERT INTO characters ("
                                + "account_id, name, class_id, gender, head_id, nation_id, element_id, "
                                + "level, strength, agility, spirit, health, luck, base_points, skill_points, "
                                + "exp, hp, max_hp, mp, max_mp, xu, luong, luong_khoa, current_map_id, position_x, position_y) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?, ?, 0, 0, 0, 100, 100, 100, 100, 10000, 0, 0, 105, 184, 280)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, accountId);
                    insert.setString(2, safeName);
                    insert.setInt(3, classId);
                    insert.setInt(4, gender);
                    insert.setInt(5, headId);
                    insert.setInt(6, nationId);
                    insert.setInt(7, elementId);
                    insert.setInt(8, stats[0]); // strength
                    insert.setInt(9, stats[1]); // agility
                    insert.setInt(10, stats[2]); // spirit
                    insert.setInt(11, stats[3]); // health
                    insert.setInt(12, stats[4]); // luck
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Character insert returned no generated id");
                        }
                        characterId = keys.getLong(1);
                    }
                }

                createStarterItemsKpah(connection, characterId, classId, gender);

                CharacterProfile createdCharacter = new CharacterProfile(
                        characterId,
                        safeName,
                        classId,
                        KpahConstants.getClassName(classId),
                        gender,
                        headId,
                        nationId,
                        KpahConstants.getNationName(nationId),
                        elementId,
                        KpahConstants.getElementName(elementId),
                        1,
                        stats[0],
                        stats[1],
                        stats[2],
                        stats[3],
                        stats[4],
                        0,
                        0,
                        0,
                        100,
                        100,
                        100,
                        100,
                        10000,
                        0,
                        0,
                        listCharacterItems(connection, characterId));

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

    private void createStarterItemsKpah(Connection connection, long characterId, int classId, int gender) throws SQLException {
        int shirtId = gender == KpahConstants.GENDER_FEMALE ? 1 : 2;     // Áo bà ba hoặc Áo nhập môn
        int pantsId = gender == KpahConstants.GENDER_FEMALE ? 27 : 28;  // Quần bà ba hoặc Quần nhập môn
        int weaponId = KpahConstants.getStarterWeaponId(classId);       // Vũ khí theo phái KPAH

        int extraShirtId = gender == KpahConstants.GENDER_FEMALE ? 3 : 4;  // Áo đai thô
        int extraPantsId = gender == KpahConstants.GENDER_FEMALE ? 29 : 30; // Quần đai thô

        int[] equippedTemplates = {shirtId, pantsId, weaponId};
        String[] slots = {"SHIRT", "PANTS", "WEAPON"};

        try (PreparedStatement insertItem = connection.prepareStatement(
                "INSERT INTO character_items (character_id, template_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement equipItem = connection.prepareStatement(
                     "INSERT INTO character_equipment (character_id, slot_code, item_instance_id) VALUES (?, ?, ?)")) {

            // Thêm và mặc các món trang bị khởi đầu
            for (int i = 0; i < equippedTemplates.length; i++) {
                insertItem.setLong(1, characterId);
                insertItem.setInt(2, equippedTemplates[i]);
                insertItem.executeUpdate();
                try (ResultSet keys = insertItem.getGeneratedKeys()) {
                    if (keys.next()) {
                        long itemId = keys.getLong(1);
                        equipItem.setLong(1, characterId);
                        equipItem.setString(2, slots[i]);
                        equipItem.setLong(3, itemId);
                        equipItem.executeUpdate();
                    }
                }
            }

            // Thêm các món trang bị dự phòng vào túi đồ (chưa mặc)
            int[] bagTemplates = {extraShirtId, extraPantsId};
            for (int templateId : bagTemplates) {
                insertItem.setLong(1, characterId);
                insertItem.setInt(2, templateId);
                insertItem.executeUpdate();
            }
        }
    }

    public List<CharacterProfile> listCharacters(Connection connection, long accountId) throws SQLException {
        List<CharacterProfile> characters = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, class_id, gender, head_id, nation_id, element_id, level, "
                        + "strength, agility, spirit, health, luck, base_points, skill_points, exp, "
                        + "hp, max_hp, mp, max_mp, xu, luong, luong_khoa "
                        + "FROM characters WHERE account_id = ? ORDER BY id")) {
            statement.setLong(1, accountId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    long id = rows.getLong("id");
                    int classId = rows.getInt("class_id");
                    int nationId = rows.getInt("nation_id");
                    int elementId = rows.getInt("element_id");

                    characters.add(new CharacterProfile(
                            id,
                            rows.getString("name"),
                            classId,
                            KpahConstants.getClassName(classId),
                            rows.getInt("gender"),
                            rows.getInt("head_id"),
                            nationId,
                            KpahConstants.getNationName(nationId),
                            elementId,
                            KpahConstants.getElementName(elementId),
                            rows.getInt("level"),
                            rows.getInt("strength"),
                            rows.getInt("agility"),
                            rows.getInt("spirit"),
                            rows.getInt("health"),
                            rows.getInt("luck"),
                            rows.getInt("base_points"),
                            rows.getInt("skill_points"),
                            rows.getLong("exp"),
                            rows.getInt("hp"),
                            rows.getInt("max_hp"),
                            rows.getInt("mp"),
                            rows.getInt("max_mp"),
                            rows.getLong("xu"),
                            rows.getInt("luong"),
                            rows.getInt("luong_khoa"),
                            listCharacterItems(connection, id)));
                }
            }
        }
        return characters;
    }

    public CharacterProfile equipItem(long accountId, long characterId, long itemId)
            throws SQLException, GameException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String slot;
                int requiredLevel;
                String genderType;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT t.slot_code, t.required_level, t.gender_type FROM character_items i "
                                + "JOIN equipment_templates t ON t.template_id = i.template_id "
                                + "WHERE i.id = ? AND i.character_id = ?")) {
                    query.setLong(1, itemId);
                    query.setLong(2, characterId);
                    try (ResultSet row = query.executeQuery()) {
                        if (!row.next()) {
                            throw new GameException("item_not_found", "Vật phẩm không tồn tại trong hành trang.");
                        }
                        slot = row.getString("slot_code");
                        requiredLevel = row.getInt("required_level");
                        genderType = row.getString("gender_type");
                    }
                }

                int characterLevel;
                int characterGender;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT level, gender FROM characters WHERE id = ? AND account_id = ?")) {
                    query.setLong(1, characterId);
                    query.setLong(2, accountId);
                    try (ResultSet row = query.executeQuery()) {
                        if (!row.next()) {
                            throw new GameException("character_not_found", "Không tìm thấy nhân vật.");
                        }
                        characterLevel = row.getInt("level");
                        characterGender = row.getInt("gender");
                    }
                }

                if (!"ALL".equalsIgnoreCase(genderType)) {
                    if (characterGender == KpahConstants.GENDER_FEMALE && "NAM_TU".equalsIgnoreCase(genderType)) {
                        throw new GameException("gender_mismatch", "Trang bị này chỉ dành cho Nam.");
                    }
                    if (characterGender == KpahConstants.GENDER_MALE && "NU_TU".equalsIgnoreCase(genderType)) {
                        throw new GameException("gender_mismatch", "Trang bị này chỉ dành cho Nữ.");
                    }
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

                CharacterProfile updatedCharacter = null;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name, class_id, gender, head_id, nation_id, element_id, level, "
                                + "strength, agility, spirit, health, luck, base_points, skill_points, exp, "
                                + "hp, max_hp, mp, max_mp, xu, luong, luong_khoa "
                                + "FROM characters WHERE id = ? AND account_id = ?")) {
                    statement.setLong(1, characterId);
                    statement.setLong(2, accountId);
                    try (ResultSet rows = statement.executeQuery()) {
                        if (rows.next()) {
                            int classId = rows.getInt("class_id");
                            int nationId = rows.getInt("nation_id");
                            int elementId = rows.getInt("element_id");

                            updatedCharacter = new CharacterProfile(
                                    characterId,
                                    rows.getString("name"),
                                    classId,
                                    KpahConstants.getClassName(classId),
                                    rows.getInt("gender"),
                                    rows.getInt("head_id"),
                                    nationId,
                                    KpahConstants.getNationName(nationId),
                                    elementId,
                                    KpahConstants.getElementName(elementId),
                                    rows.getInt("level"),
                                    rows.getInt("strength"),
                                    rows.getInt("agility"),
                                    rows.getInt("spirit"),
                                    rows.getInt("health"),
                                    rows.getInt("luck"),
                                    rows.getInt("base_points"),
                                    rows.getInt("skill_points"),
                                    rows.getLong("exp"),
                                    rows.getInt("hp"),
                                    rows.getInt("max_hp"),
                                    rows.getInt("mp"),
                                    rows.getInt("max_mp"),
                                    rows.getLong("xu"),
                                    rows.getInt("luong"),
                                    rows.getInt("luong_khoa"),
                                    listCharacterItems(connection, characterId));
                        }
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
}
