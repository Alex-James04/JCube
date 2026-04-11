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

import model.Penalty;
import model.Solve;

public class SolveDB {
    public void insert(Solve solve) {
        if (solve.isPersisted()) {
            throw new IllegalArgumentException("Solve is already persisted: " + solve);
        }
        String sql = "INSERT INTO solves (session_id, time_ms, penalty, scramble) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, solve.getSessionId());
            stmt.setLong(2, solve.getTimeMs());
            stmt.setString(3, solve.getPenalty().name().toLowerCase());
            stmt.setString(4, solve.getScramble());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                solve.setId(keys.getInt(1));
            } else {
                throw new RuntimeException("Insert succeeded but no generated key was returned");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert solve: " + solve, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public Optional<Solve> findById(int id) {
        String sql = "SELECT id, session_id, time_ms, penalty, scramble, created_at FROM solves WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(fromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find solve with id: " + id, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
        return Optional.empty();
    }

    public List<Solve> findBySessionId(int sessionId) {
        String sql = "SELECT id, session_id, time_ms, penalty, scramble, created_at FROM solves WHERE session_id = ? ORDER BY created_at ASC, id ASC";
        List<Solve> solves = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                solves.add(fromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find solves for session with id: " + sessionId, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
        return solves;
    }

    public void updatePenalty(int id, Penalty penalty) {
        if (id == -1) {
            throw new IllegalArgumentException("Cannot update a solve that is not persisted (id == -1)");
        }
        String sql = "UPDATE solves SET penalty = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, penalty.name().toLowerCase());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update penalty for solve with id: " + id, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM solves WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete solve with id: " + id, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public void deleteAll(int sessionId) {
        String sql = "DELETE FROM solves WHERE session_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete solves for session with id: " + sessionId, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public int count(int sessionId) {
        String sql = "SELECT COUNT(*) FROM solves WHERE session_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count solves for session with id: " + sessionId, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
        return 0;
    }

    private static Solve fromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int sessionId = rs.getInt("session_id");
        long timeMs = rs.getLong("time_ms");
        Penalty penalty = Penalty.valueOf(rs.getString("penalty").toUpperCase());
        String scramble = rs.getString("scramble");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        return new Solve(id, sessionId, timeMs, penalty, scramble, createdAt);
    }
}