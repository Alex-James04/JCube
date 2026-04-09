package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DatabaseManagerTest extends DatabaseTestBase {

    @Test
    void cubesTableExists() throws SQLException {
        assertTrue(tableExists("cubes"));
    }

    @Test
    void sessionsTableExists() throws SQLException {
        assertTrue(tableExists("sessions"));
    }

    @Test
    void solvesTableExists() throws SQLException {
        assertTrue(tableExists("solves"));
    }

    @Test
    void settingsTableExists() throws SQLException {
        assertTrue(tableExists("settings"));
    }

    @Test
    void settingsRowIsSeeded() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM settings");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
    }

    @Test
    void settingsRowHasCorrectDefaultTheme() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT theme FROM settings WHERE id = 1");
        assertTrue(rs.next());
        assertEquals("dark", rs.getString("theme"));
    }

    @Test
    void settingsRowHasCorrectDefaultShowScramble() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT show_scramble FROM settings WHERE id = 1");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("show_scramble"));
    }

    @Test
    void getConnectionReturnsNonNullConnection() throws SQLException {
        assertNotNull(DatabaseManager.getConnection());
    }

    @Test
    void getConnectionReturnsSameInstance() throws SQLException {
        Connection first = DatabaseManager.getConnection();
        Connection second = DatabaseManager.getConnection();
        assertTrue(first == second);
    }

    @Test
    void resetConnectionClosesConnection() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        DatabaseManager.resetConnection();
        assertTrue(conn.isClosed());
    }

    private boolean tableExists(String tableName) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '" + tableName + "'"
        );
        return rs.next() && rs.getInt(1) == 1;
    }
}