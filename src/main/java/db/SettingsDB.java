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
        String sql = "SELECT show_scramble, spacebar_mode, inspection_mode, stat_specs, confirm_deletes, decimal_places FROM settings WHERE id = 1";
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
        String sql = "UPDATE settings SET show_scramble = ?, spacebar_mode = ?, inspection_mode = ?, stat_specs = ?, confirm_deletes = ?, decimal_places = ? WHERE id = 1";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, settings.isShowScramble() ? 1 : 0);
            stmt.setString(2, settings.getSpacebarMode().name());
            stmt.setString(3, settings.getInspectionMode().name());
            stmt.setString(4, StatSpec.encodeList(settings.getStatSpecs()));
            stmt.setInt(5, settings.isConfirmDeletes() ? 1 : 0);
            stmt.setInt(6, settings.getDecimalPlaces());
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
        boolean showScramble = rs.getInt("show_scramble") == 1;
        SpacebarMode spacebarMode = SpacebarMode.valueOf(rs.getString("spacebar_mode"));
        InspectionMode inspectionMode = InspectionMode.valueOf(rs.getString("inspection_mode"));
        var statSpecs = StatSpec.parseList(rs.getString("stat_specs"));
        boolean confirmDeletes = rs.getInt("confirm_deletes") == 1;
        int decimalPlaces = rs.getInt("decimal_places");
        return new Settings(showScramble, spacebarMode, inspectionMode, statSpecs, confirmDeletes, decimalPlaces);
    }
}
