package vn.indiedev.tutien.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record ServerConfig(String host, int port, String databaseUrl,
                           String databaseUser, String databasePassword) {

    public static ServerConfig fromEnvironment() {
        Properties properties = new Properties();
        String configPath = environmentValue("SERVER_CONFIG", "server.properties");
        Path path = Path.of(configPath);
        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot read server config file: " + path.toAbsolutePath(), exception);
            }
        }

        return new ServerConfig(
                value(properties, "SERVER_HOST", "server.host", "0.0.0.0"),
                intValue(properties, "SERVER_PORT", "server.port", 19130),
                value(properties, "DB_URL", "db.url", "jdbc:mariadb://127.0.0.1:3306/tu_tien?useUnicode=true&characterEncoding=utf8&sslMode=disable"),
                value(properties, "DB_USER", "db.user", "tu_tien"),
                value(properties, "DB_PASSWORD", "db.password", "change-me")
        );
    }

    private static String value(Properties properties, String environmentKey, String propertyKey, String defaultValue) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        String propertyValue = properties.getProperty(propertyKey);
        return propertyValue == null || propertyValue.isBlank() ? defaultValue : propertyValue.strip();
    }

    private static int intValue(Properties properties, String environmentKey, String propertyKey, int defaultValue) {
        String value = value(properties, environmentKey, propertyKey, Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid port in " + propertyKey + ": " + value, exception);
        }
    }

    private static String environmentValue(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
