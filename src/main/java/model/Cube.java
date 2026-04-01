package model;

import java.time.LocalDateTime;

public class Cube {

    private int id;
    private String name;
    private final LocalDateTime dateTimeCreated;

    public Cube(String name) {
        this.id = -1;
        this.name = name;
        this.dateTimeCreated = null;
    }

    public Cube(int id, String name, LocalDateTime dateTimeCreated) {
        this.id = id;
        this.name = name;
        this.dateTimeCreated = dateTimeCreated;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getDateTimeCreated() { return dateTimeCreated; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public boolean isPersisted() { return id != -1; }

    @Override
    public String toString() {
        return "Cube{id=" + id + ", name='" + name + "', dateTimeCreated=" + dateTimeCreated + "}";
    }
}