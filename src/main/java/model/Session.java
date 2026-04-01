package model;

import java.time.LocalDateTime;

public class Session {

    private int id;
    private final int cubeId;
    private String name;
    private final LocalDateTime createdAt;

    public Session(int cubeId, String name) {
        this.id = -1;
        this.cubeId = cubeId;
        this.name = name;
        this.createdAt = null;
    }

    public Session(int id, int cubeId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.cubeId = cubeId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getCubeId() { return cubeId; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public boolean isPersisted() { return id != -1; }

    @Override
    public String toString() {
        return "Session{id=" + id + ", cubeId=" + cubeId + ", name='" + name + "', createdAt=" + createdAt + "}";
    }
}