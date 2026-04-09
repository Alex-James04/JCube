package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Session;

public class SessionDB {
    public void insert(Session session) {
        if (session.isPersisted()) {
            throw new IllegalArgumentException("Session is already persisted: " + session);
        }
        String sql = "INSERT INTO sessions (cube_id, name) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getCubeId());
            stmt.setString(2, session.getName());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    session.setId(keys.getInt(1));
                } else {
                    throw new RuntimeException("Insert succeeded but no generated key was returned");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert session: " + session, e);
        }
    }

    public Optional<Session> findById(int id) {
        String sql = "SELECT id, cube_id, name, created_at FROM sessions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(fromRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find session with id: " + id, e);
        }
        return Optional.empty();
    }

    public List<Session> findByCubeId(int cubeId) {
        String sql = "SELECT id, cube_id, name, created_at FROM sessions WHERE cube_id = ? ORDER BY created_at ASC";
        List<Session> sessions = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cubeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(fromRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find sessions for cube with id: " + cubeId, e);
        }
        return sessions;
    }

    public void updateName(int id, String name) {
        if (id == -1) {
            throw new IllegalArgumentException("Cannot update a session that is not persisted (id == -1)");
        }
        String sql = "UPDATE sessions SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update name for session with id: " + id, e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM sessions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete session with id: " + id, e);
        }
    }

    private static Session fromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int cubeId = rs.getInt("cube_id");
        String name = rs.getString("name");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        return new Session(id, cubeId, name, createdAt);
    }
}