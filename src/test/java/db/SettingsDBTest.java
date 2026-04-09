package db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Settings;

class SettingsDBTest extends DatabaseTestBase {

    private SettingsDB settingsDB;

    @BeforeEach
    void setUp() {
        settingsDB = new SettingsDB();
    }

    @Test
    void getReturnsNonNullSettings() {
        Settings settings = settingsDB.get();
        assertNotNull(settings);
    }

    @Test
    void getReturnsDefaultTheme() {
        Settings settings = settingsDB.get();
        assertEquals("dark", settings.getTheme());
    }

    @Test
    void getReturnsDefaultShowScramble() {
        Settings settings = settingsDB.get();
        assertTrue(settings.isShowScramble());
    }

    @Test
    void updateThemePersistsCorrectly() {
        Settings settings = settingsDB.get();
        settings.setTheme("light");
        settingsDB.update(settings);
        Settings result = settingsDB.get();
        assertEquals("light", result.getTheme());
    }

    @Test
    void updateShowScrambleFalsePersistsCorrectly() {
        Settings settings = settingsDB.get();
        settings.setShowScramble(false);
        settingsDB.update(settings);
        Settings result = settingsDB.get();
        assertFalse(result.isShowScramble());
    }

    @Test
    void updateShowScrambleTruePersistsCorrectly() {
        Settings settings = settingsDB.get();
        settings.setShowScramble(false);
        settingsDB.update(settings);
        settings.setShowScramble(true);
        settingsDB.update(settings);
        Settings result = settingsDB.get();
        assertTrue(result.isShowScramble());
    }

    @Test
    void updateBothFieldsPersistCorrectly() {
        Settings settings = settingsDB.get();
        settings.setTheme("light");
        settings.setShowScramble(false);
        settingsDB.update(settings);
        Settings result = settingsDB.get();
        assertEquals("light", result.getTheme());
        assertFalse(result.isShowScramble());
    }
}