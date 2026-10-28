package vn.indiedev.tutien.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import vn.indiedev.tutien.config.ServerConfig;

public final class Database {

    private Database() {
    }

    public static HikariDataSource createDataSource(ServerConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("tu-tien-db");
        hikari.setJdbcUrl(config.databaseUrl());
        hikari.setUsername(config.databaseUser());
        hikari.setPassword(config.databasePassword());
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(10_000);
        hikari.setValidationTimeout(5_000);
        return new HikariDataSource(hikari);
    }
}
