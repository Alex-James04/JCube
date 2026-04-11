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

import model.Cube;

public class CubeDB {
    public void insert(Cube cube) {
        if (cube.isPersisted()) {
            throw new IllegalArgumentException("Cube is already persisted: " + cube);
        }
        String sql = "INSERT INTO cubes (name) VALUES (?)";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cube.getName());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                cube.setId(keys.getInt(1));
            } else {
                throw new RuntimeException("Insert succeeded but no generated key was returned");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert cube: " + cube, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public Optional<Cube> findById(int id) {
        String sql = "SELECT id, name, created_at FROM cubes WHERE id = ?";
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
            throw new RuntimeException("Failed to find cube with id: " + id, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
        return Optional.empty();
    }

    public List<Cube> findAll() {
        String sql = "SELECT id, name, created_at FROM cubes ORDER BY created_at ASC, id ASC";
        List<Cube> cubes = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cubes.add(fromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all cubes", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
        return cubes;
    }

    public void updateName(int id, String name) {
        if (id == -1) {
            throw new IllegalArgumentException("Cannot update a cube that is not persisted (id == -1)");
        }
        String sql = "UPDATE cubes SET name = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update name for cube with id: " + id, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM cubes WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete cube with id: " + id, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    private static Cube fromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        return new Cube(id, name, createdAt);
    }
}