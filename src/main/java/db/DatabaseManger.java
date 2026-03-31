package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManger {

    private static final String DB_PATH = "jcube.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);   
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to database", e);
            }
            initializeDatabase(connection);
        }
        return connection;
    }

private static void initializeDatabase(Connection connection) throws SQLException {
    try (InputStream inputStream = new FileInputStream("/db/schema.sql")) {
        String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        for (String statement : sql.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                connection.createStatement().execute(trimmed);
            }
        }
    } catch (IOException e) {
        throw new RuntimeException("Failed to read schema.sql", e);
    }
}

    public static void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}