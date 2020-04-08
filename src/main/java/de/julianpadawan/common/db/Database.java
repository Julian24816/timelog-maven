package de.julianpadawan.common.db;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * holds a connection pool
 */
public final class Database {
    private static BiConsumer<String, Throwable> errorHandler = (sql, error) -> {
        System.out.println(sql);
        error.printStackTrace();
    };

    private static BasicDataSource dataSource;

    private Database() {
    }

    public static void init(String url, String user, String password) {
        dataSource = new BasicDataSource();
        dataSource.setMinIdle(1);
        dataSource.setMaxIdle(10);
        dataSource.setMaxOpenPreparedStatements(100);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
    }

    public static void execFile(Path filename) throws IOException {
        for (String sql : Files.readString(filename).split(";")) {
            sql = sql.strip();
            if (!sql.isEmpty()) execute(sql, PreparedStatement::execute, null);
        }
    }

    public static <R> R execute(String sql, SQLFunction<PreparedStatement, R> executor, R errorValue) {
        try (final Connection connection = getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            return executor.apply(statement);
        } catch (SQLException e) {
            errorHandler.accept(sql, e);
            return errorValue;
        }
    }

    private static Connection getConnection() throws SQLException {
        if (dataSource == null) throw new IllegalStateException("dataSource not initialized");
        return dataSource.getConnection();
    }

    public static void setErrorHandler(BiConsumer<String, Throwable> errorHandler) {
        Database.errorHandler = Objects.requireNonNull(errorHandler);
    }

}
