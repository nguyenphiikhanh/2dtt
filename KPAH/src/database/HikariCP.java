package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import manager.Settings;

/**
 *
 * @author Dev Duy
 */
public class HikariCP {

    private static final String DB_URL = "jdbc:mysql://" + Settings.HOST + "/" + Settings.DATABASE + "?useUnicode=true&characterEncoding=utf-8";
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;

    static {
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(DB_URL);
        config.setUsername(Settings.USER);
        config.setPassword(Settings.PASS);
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static boolean execute(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement s = connection.prepareStatement(sql);
            return s.execute();
        }
    }

    public static ResultSetImpl executeQuery(final String sql) throws SQLException {
        try (final Connection con = getConnection(); final Statement ps = con.createStatement(); final ResultSet rs = ps.executeQuery(sql)) {
            return new ResultSetImpl(rs);
        }
    }

    public static ResultSetImpl executeQuery(final String sql, final Object... objs) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement(sql);) {
            for (int i = 0; i < objs.length; ++i) {
                ps.setObject(i + 1, objs[i]);
            }
            return new ResultSetImpl(ps.executeQuery());
        }
    }

    public static int executeUpdate(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement s = connection.prepareStatement(sql);
            return s.executeUpdate();
        }
    }

    public static int execute(final String sql, final int id, final Object... objs) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);) {
            for (int i = 0; i < objs.length; ++i) {
                ps.setObject(i + 1, objs[i]);
            }
            ps.execute();
            final int r;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.first();
                r = rs.getInt(id);
            }
            return r;
        }
    }

    public static int execute(final String sql, final int id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement s = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            s.execute();
            final int r;
            try (ResultSet rs = s.getGeneratedKeys()) {
                rs.first();
                r = rs.getInt(id);
            }
            return r;
        }
    }

    public static int executeExist(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement s = connection.prepareStatement(sql); ResultSet rs = s.executeQuery()) {
            rs.first();
            return rs.getInt(1);
        }
    }
}
