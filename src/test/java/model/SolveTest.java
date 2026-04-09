package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolveTest {

    Solve solve;

    @BeforeEach
    void setUp() {
        solve = new Solve(1, 10000, "R U R' U'");
    }


    @Test
    void effectiveTimeMsWithNoPenaltyReturnsTimeMs() {
        assertEquals(10000, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithNoneReturnsTimeMs() {
        solve.setPenalty(Penalty.NONE);
        assertEquals(10000, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithPlus2ReturnsTimeMsPlusTwo() {
        solve.setPenalty(Penalty.PLUS2);
        assertEquals(12000, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithDnfReturnsMaxValue() {
        solve.setPenalty(Penalty.DNF);
        assertEquals(Long.MAX_VALUE, solve.getEffectiveTimeMs());
    }

    @Test
    void effectiveTimeMsWithPenaltyChangesReturnsCorrectValue() {
        solve.setPenalty(Penalty.PLUS2);
        assertEquals(12000, solve.getEffectiveTimeMs());
        solve.setPenalty(Penalty.DNF);
        assertEquals(Long.MAX_VALUE, solve.getEffectiveTimeMs());
        solve.setPenalty(Penalty.NONE);
        assertEquals(10000, solve.getEffectiveTimeMs());
    }
}