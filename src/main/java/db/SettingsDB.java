package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Settings;

public class SettingsDB {
    public Settings get() {
        String sql = "SELECT theme, show_scramble FROM settings WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return fromRow(rs);
            } else {
                throw new RuntimeException("Settings row is missing — schema seeding may have failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve settings", e);
        }
    }

    public void update(Settings settings) {
        String sql = "UPDATE settings SET theme = ?, show_scramble = ? WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, settings.getTheme());
            stmt.setInt(2, settings.isShowScramble() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update settings", e);
        }
    }

    private static Settings fromRow(ResultSet rs) throws SQLException {
        String theme = rs.getString("theme");
        boolean showScramble = rs.getInt("show_scramble") == 1;
        return new Settings(theme, showScramble);
    }
}