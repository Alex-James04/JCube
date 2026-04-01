package model;

import java.time.LocalDateTime;

public class Solve {

    private int id;
    private final int sessionId;
    private final long timeMs;
    private Penalty penalty;
    private final String scramble;
    private final LocalDateTime createdAt;

    public Solve(int sessionId, long timeMs, String scramble) {
        this.id = -1;
        this.sessionId = sessionId;
        this.timeMs = timeMs;
        this.penalty = Penalty.NONE;
        this.scramble = scramble;
        this.createdAt = null;
    }

    public Solve(int id, int sessionId, long timeMs, Penalty penalty, String scramble, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.timeMs = timeMs;
        this.penalty = penalty;
        this.scramble = scramble;
        this.createdAt = createdAt;
    }

    public long getEffectiveTimeMs() {
        return switch (penalty) {
            case NONE  -> timeMs;
            case PLUS2 -> timeMs + 2000;
            case DNF   -> Long.MAX_VALUE;
        };
    }

    public int getId() { return id; }
    public int getSessionId() { return sessionId; }
    public long getTimeMs() { return timeMs; }
    public Penalty getPenalty() { return penalty; }
    public String getScramble() { return scramble; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setPenalty(Penalty penalty) { this.penalty = penalty; }

    public boolean isPersisted() { return id != -1; }

    @Override
    public String toString() {
        return "Solve{id=" + id + ", sessionId=" + sessionId + ", timeMs=" + timeMs + ", penalty=" + penalty + ", scramble='" + scramble + "', createdAt=" + createdAt + "}";
    }
}