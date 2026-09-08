package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.ColorScheme;

public class ColorSchemeDB {
    public ColorScheme get() {
        String sql = "SELECT background, surface, text_primary, text_secondary, accent, button, button_hover, danger, border FROM color_scheme WHERE id = 1";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromRow(rs);
            } else {
                throw new RuntimeException("Color scheme row is missing — schema seeding may have failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve color scheme", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public void update(ColorScheme scheme) {
        String sql = "UPDATE color_scheme SET background = ?, surface = ?, text_primary = ?, text_secondary = ?, "
                + "accent = ?, button = ?, button_hover = ?, danger = ?, border = ? WHERE id = 1";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, scheme.getBackground());
            stmt.setString(2, scheme.getSurface());
            stmt.setString(3, scheme.getTextPrimary());
            stmt.setString(4, scheme.getTextSecondary());
            stmt.setString(5, scheme.getAccent());
            stmt.setString(6, scheme.getButton());
            stmt.setString(7, scheme.getButtonHover());
            stmt.setString(8, scheme.getDanger());
            stmt.setString(9, scheme.getBorder());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update color scheme", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    private static ColorScheme fromRow(ResultSet rs) throws SQLException {
        return new ColorScheme(
                rs.getString("background"),
                rs.getString("surface"),
                rs.getString("text_primary"),
                rs.getString("text_secondary"),
                rs.getString("accent"),
                rs.getString("button"),
                rs.getString("button_hover"),
                rs.getString("danger"),
                rs.getString("border"));
    }
}
