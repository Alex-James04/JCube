package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.InspectionMode;
import model.Settings;
import model.SpacebarMode;
import model.StatSpec;

public class SettingsDB {
    public Settings get() {
        String sql = "SELECT theme, show_scramble, spacebar_mode, inspection_mode, stat_specs, confirm_deletes FROM settings WHERE id = 1";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromRow(rs);
            } else {
                throw new RuntimeException("Settings row is missing — schema seeding may have failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve settings", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public void update(Settings settings) {
        String sql = "UPDATE settings SET theme = ?, show_scramble = ?, spacebar_mode = ?, inspection_mode = ?, stat_specs = ?, confirm_deletes = ? WHERE id = 1";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, settings.getTheme());
            stmt.setInt(2, settings.isShowScramble() ? 1 : 0);
            stmt.setString(3, settings.getSpacebarMode().name());
            stmt.setString(4, settings.getInspectionMode().name());
            stmt.setString(5, StatSpec.encodeList(settings.getStatSpecs()));
            stmt.setInt(6, settings.isConfirmDeletes() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update settings", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    private static Settings fromRow(ResultSet rs) throws SQLException {
        String theme = rs.getString("theme");
        boolean showScramble = rs.getInt("show_scramble") == 1;
        SpacebarMode spacebarMode = SpacebarMode.valueOf(rs.getString("spacebar_mode"));
        InspectionMode inspectionMode = InspectionMode.valueOf(rs.getString("inspection_mode"));
        var statSpecs = StatSpec.parseList(rs.getString("stat_specs"));
        boolean confirmDeletes = rs.getInt("confirm_deletes") == 1;
        return new Settings(theme, showScramble, spacebarMode, inspectionMode, statSpecs, confirmDeletes);
    }
}
