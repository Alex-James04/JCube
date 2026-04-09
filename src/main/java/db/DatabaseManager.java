package db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String DB_PATH = resolveDatabasePath();
    private static Connection connection;

    private static String resolveDatabasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        String dataDir;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                dataDir = appData + "\\JCube";
            } else {
                dataDir = home + "\\AppData\\Roaming\\JCube";
            }
        } else if (os.contains("mac")) {
            dataDir = home + "/Library/Application Support/JCube";
        } else {
            dataDir = home + "/.local/share/JCube";
        }
        try {
            Files.createDirectories(Paths.get(dataDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create JCube data directory: " + dataDir, e);
        }
        String separator;
        if (os.contains("win")) {
            separator = "\\";
        } else {
            separator = "/";
        }
        return dataDir + separator + "jcube.db";
    }

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

    public static void setTestConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        initializeDatabase(connection);
    }

    public static void resetConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = null;
    }

    private static void initializeDatabase(Connection connection) throws SQLException {
        try (InputStream inputStream = DatabaseManager.class.getResourceAsStream("/db/schema.sql")) {
            if (inputStream == null) {
                throw new RuntimeException("schema.sql not found on classpath");
            }
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