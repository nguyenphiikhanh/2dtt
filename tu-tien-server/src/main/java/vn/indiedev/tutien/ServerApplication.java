package vn.indiedev.tutien;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import vn.indiedev.tutien.config.ServerConfig;
import vn.indiedev.tutien.game.GameService;
import vn.indiedev.tutien.net.GameServer;
import vn.indiedev.tutien.persistence.Database;

public final class ServerApplication {

    private ServerApplication() {
    }

    public static void main(String[] args) throws InterruptedException {
        ServerConfig config = ServerConfig.fromEnvironment();
        HikariDataSource dataSource = Database.createDataSource(config);

        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();

            try (GameServer server = new GameServer(config, new GameService(dataSource))) {
                server.start();
                System.out.printf("Tu Tien server listening on ws://%s:%d/game%n",
                        config.host(), config.port());
                server.awaitClose();
            }
        } finally {
            dataSource.close();
        }
    }
}
