package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SolveTest {

    @Test
    void effectiveTimeMsWithNoPenaltyReturnsTimeMs() {
        Solve solve = new Solve(1, 10000, "R U R' U'");
        assertEquals(10000, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithPlus2ReturnsTimeMsPlusTwo() {
        Solve solve = new Solve(1, 10000, "R U R' U'");
        solve.setPenalty(Penalty.PLUS2);
        assertEquals(12000, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithDnfReturnsMaxValue() {
        Solve solve = new Solve(1, 10000, "R U R' U'");
        solve.setPenalty(Penalty.DNF);
        assertEquals(Long.MAX_VALUE, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithDnfIgnoresTimeMs() {
        Solve solve = new Solve(1, 99999, "R U R' U'");
        solve.setPenalty(Penalty.DNF);
        assertEquals(Long.MAX_VALUE, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsReflectsUpdatedPenalty() {
        Solve solve = new Solve(1, 10000, "R U R' U'");
        assertEquals(10000, solve.getEffectiveTimeMs());
        solve.setPenalty(Penalty.DNF);
        assertEquals(Long.MAX_VALUE, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithPlus2UsesRealisticTime() {
        Solve solve = new Solve(1, 8340, "R U R' U'");
        solve.setPenalty(Penalty.PLUS2);
        assertEquals(10340, solve.getEffectiveTimeMs());
    }
}