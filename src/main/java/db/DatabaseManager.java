package db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_PATH = resolveDatabasePath();
    private static boolean initialized = false;

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
        if (os.contains("win")) {
            return dataDir + "\\jcube.db";
        } else {
            return dataDir + "/jcube.db";
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        if (!initialized) {
            initializeDatabase(connection);
            initialized = true;
        }
        return connection;
    }

    private static void initializeDatabase(Connection conn) throws SQLException {
        try (InputStream inputStream = DatabaseManager.class.getResourceAsStream("/db/schema.sql")) {
            if (inputStream == null) {
                throw new RuntimeException("schema.sql not found on classpath");
            }
            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema.sql", e);
        }
    }
}