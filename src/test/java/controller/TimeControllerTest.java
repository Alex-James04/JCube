package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import model.InspectionMode;
import model.Penalty;
import model.SpacebarMode;

class TimeControllerTest {

    private final List<TimeController.State> states = new ArrayList<>();
    private final List<Object[]> completedSolves = new ArrayList<>();

    private TimeController newController(SpacebarMode spacebarMode, InspectionMode inspectionMode) {
        return new TimeController(spacebarMode, inspectionMode,
                states::add,
                (timeMs, penalty) -> completedSolves.add(new Object[]{timeMs, penalty}));
    }

    @Test
    void wcaSpacebarRequiresMinimumHoldToArm() {
        TimeController tc = newController(SpacebarMode.WCA, InspectionMode.WCA);

        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(100); // held only 100ms, too short to arm
        assertEquals(TimeController.State.INSPECTION, tc.getState());

        tc.onSpacebarPressed(200);
        tc.onSpacebarReleased(800); // held 600ms, arms and starts
        assertEquals(TimeController.State.RUNNING, tc.getState());
    }

    @Test
    void simpleSpacebarArmsWithNoMinimumHold() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);

        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(0); // zero hold time
        assertEquals(TimeController.State.RUNNING, tc.getState());
    }

    @Test
    void pressingSpacebarWhileRunningStopsAndReportsSolve() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);
        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(0);
        tc.onSpacebarPressed(12340);

        assertEquals(TimeController.State.STOPPED, tc.getState());
        assertEquals(1, completedSolves.size());
        assertEquals(12340L, completedSolves.get(0)[0]);
        assertEquals(Penalty.NONE, completedSolves.get(0)[1]);
    }

    @Test
    void wcaInspectionAppliesPlus2BetweenFifteenAndSeventeenSeconds() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);
        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(16000);
        assertEquals(TimeController.State.RUNNING, tc.getState());

        tc.onSpacebarPressed(16000 + 5000);
        assertEquals(5000L, completedSolves.get(0)[0]);
        assertEquals(Penalty.PLUS2, completedSolves.get(0)[1]);
    }

    @Test
    void inspectionExactlyAtPlus2LimitHasNoPenaltyYet() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);
        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(15000);
        tc.onSpacebarPressed(16000);
        assertEquals(Penalty.NONE, completedSolves.get(0)[1]);
    }

    @Test
    void inspectionExactlyAtDnfLimitIsOnlyPlus2NotDnf() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);
        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(17000);
        tc.onSpacebarPressed(18000);
        assertEquals(Penalty.PLUS2, completedSolves.get(0)[1]);
    }

    @Test
    void inspectionJustPastDnfLimitOnReleaseIsImmediateDnf() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);
        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(17001);

        assertEquals(TimeController.State.STOPPED, tc.getState());
        assertFalse(states.contains(TimeController.State.RUNNING));
        assertEquals(Penalty.DNF, completedSolves.get(0)[1]);
    }

    @Test
    void wcaInspectionAutoDnfsAfterSeventeenSecondsViaTick() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);
        tc.onSpacebarPressed(0);

        tc.tick(10000);
        assertEquals(TimeController.State.INSPECTION, tc.getState());

        tc.tick(17500); // no release ever happened, tick alone must catch the timeout
        assertEquals(TimeController.State.STOPPED, tc.getState());
        assertFalse(states.contains(TimeController.State.RUNNING));
        assertEquals(0L, completedSolves.get(0)[0]);
        assertEquals(Penalty.DNF, completedSolves.get(0)[1]);
    }

    @Test
    void simpleInspectionNeverAutoDnfsRegardlessOfDuration() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.SIMPLE);
        tc.onSpacebarPressed(0);

        tc.tick(60000);
        assertEquals(TimeController.State.INSPECTION, tc.getState());

        tc.onSpacebarReleased(90000);
        assertEquals(TimeController.State.RUNNING, tc.getState());
        assertTrue(completedSolves.isEmpty());
    }

    @Test
    void stoppedStateAllowsStartingANewInspectionImmediately() {
        TimeController tc = newController(SpacebarMode.SIMPLE, InspectionMode.WCA);
        tc.onSpacebarPressed(0);
        tc.onSpacebarReleased(0);
        tc.onSpacebarPressed(5000);
        assertEquals(TimeController.State.STOPPED, tc.getState());

        tc.onSpacebarPressed(6000);
        assertEquals(TimeController.State.INSPECTION, tc.getState());
    }

    @Test
    void releasingOutsideInspectionDoesNothing() {
        TimeController tc = newController(SpacebarMode.WCA, InspectionMode.WCA);
        tc.onSpacebarReleased(0);
        assertEquals(TimeController.State.IDLE, tc.getState());
    }

    @Test
    void reArmingDuringInspectionOnlyCountsTheLatestHold() {
        TimeController tc = newController(SpacebarMode.WCA, InspectionMode.WCA);
        tc.onSpacebarPressed(0);
        tc.onSpacebarPressed(400); // re-press before releasing resets the hold window
        tc.onSpacebarReleased(700); // only 300ms since the re-press, too short
        assertEquals(TimeController.State.INSPECTION, tc.getState());
    }
}
