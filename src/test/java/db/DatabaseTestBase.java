package db;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class DatabaseTestBase {

    @BeforeEach
    void setUpDatabase() throws SQLException {
        DatabaseManager.setTestConnection();
    }

    @AfterEach
    void tearDownDatabase() throws SQLException {
        DatabaseManager.resetConnection();
    }
}