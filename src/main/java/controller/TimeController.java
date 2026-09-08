package controller;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import model.InspectionMode;
import model.Penalty;
import model.SpacebarMode;

// Timer state machine: IDLE/STOPPED -> INSPECTION -> RUNNING -> STOPPED. Driven entirely by
// explicit timestamps (passed in by the caller) rather than reading the clock itself, so it can
// be unit tested deterministically and reused unchanged by whatever UI feeds it key events.
public class TimeController {

    public enum State { IDLE, INSPECTION, RUNNING, STOPPED }

    // How long the spacebar must be held during inspection before a release is allowed to start
    // the solve, when using WCA-style arming. SIMPLE mode ignores this entirely.
    public static final long WCA_HOLD_THRESHOLD_MS = 500;
    public static final long INSPECTION_PLUS2_LIMIT_MS = 15000;
    public static final long INSPECTION_DNF_LIMIT_MS = 17000;

    private final SpacebarMode spacebarMode;
    private final InspectionMode inspectionMode;
    private final Consumer<State> onStateChanged;
    private final BiConsumer<Long, Penalty> onSolveCompleted;

    private State state = State.IDLE;
    private long holdStartMs;
    private long inspectionStartMs;
    private long runStartMs;
    private Penalty pendingInspectionPenalty = Penalty.NONE;

    public TimeController(SpacebarMode spacebarMode, InspectionMode inspectionMode,
                           Consumer<State> onStateChanged, BiConsumer<Long, Penalty> onSolveCompleted) {
        this.spacebarMode = spacebarMode;
        this.inspectionMode = inspectionMode;
        this.onStateChanged = onStateChanged;
        this.onSolveCompleted = onSolveCompleted;
    }

    public State getState() {
        return state;
    }

    public void onSpacebarPressed(long nowMs) {
        switch (state) {
            case IDLE, STOPPED -> {
                inspectionStartMs = nowMs;
                holdStartMs = nowMs;
                setState(State.INSPECTION);
            }
            // Resets on every press during inspection, not just the first: WCA arming requires
            // the hold immediately before release to reach the threshold, so a press-release-
            // press sequence (fidgeting, a mis-timed early press) must restart the clock rather
            // than let an earlier, already-released hold count toward this release.
            case INSPECTION -> holdStartMs = nowMs;
            case RUNNING -> stopRun(nowMs);
        }
    }

    public void onSpacebarReleased(long nowMs) {
        if (state != State.INSPECTION) {
            return;
        }
        long holdDurationMs = nowMs - holdStartMs;
        boolean armed = spacebarMode == SpacebarMode.SIMPLE || holdDurationMs >= WCA_HOLD_THRESHOLD_MS;
        if (armed) {
            startRun(nowMs);
        }
    }

    // Call periodically (e.g. once per animation frame) while running so the WCA 17s inspection
    // overrun is caught even if the user never releases the spacebar at all.
    public void tick(long nowMs) {
        if (state == State.INSPECTION && inspectionMode == InspectionMode.WCA
                && nowMs - inspectionStartMs > INSPECTION_DNF_LIMIT_MS) {
            completeSolve(0, Penalty.DNF);
        }
    }

    public long elapsedInspectionMs(long nowMs) {
        return state == State.INSPECTION ? nowMs - inspectionStartMs : 0;
    }

    public long elapsedRunMs(long nowMs) {
        return state == State.RUNNING ? nowMs - runStartMs : 0;
    }

    // This DNF check duplicates part of what tick() already watches for, rather than relying on
    // tick() alone: tick() only runs on a timer (once per animation frame) and exists to catch the
    // case where the user never releases the spacebar at all. If they DO release — even a whole
    // frame after crossing the 17s limit — that release goes through onSpacebarReleased/startRun
    // directly, with no guarantee tick() ran in between. Without this check here, a late-but-real
    // release could slip through as a normal (or +2) start instead of the DNF it should be.
    private void startRun(long nowMs) {
        Penalty inspectionPenalty = inspectionPenaltyFor(nowMs - inspectionStartMs);
        if (inspectionPenalty == Penalty.DNF) {
            completeSolve(0, Penalty.DNF);
            return;
        }
        runStartMs = nowMs;
        pendingInspectionPenalty = inspectionPenalty;
        setState(State.RUNNING);
    }

    private Penalty inspectionPenaltyFor(long inspectionElapsedMs) {
        if (inspectionMode != InspectionMode.WCA) {
            return Penalty.NONE;
        }
        if (inspectionElapsedMs > INSPECTION_DNF_LIMIT_MS) {
            return Penalty.DNF;
        }
        if (inspectionElapsedMs > INSPECTION_PLUS2_LIMIT_MS) {
            return Penalty.PLUS2;
        }
        return Penalty.NONE;
    }

    private void stopRun(long nowMs) {
        completeSolve(nowMs - runStartMs, pendingInspectionPenalty);
    }

    private void completeSolve(long timeMs, Penalty penalty) {
        pendingInspectionPenalty = Penalty.NONE;
        setState(State.STOPPED);
        onSolveCompleted.accept(timeMs, penalty);
    }

    private void setState(State newState) {
        state = newState;
        onStateChanged.accept(newState);
    }
}
