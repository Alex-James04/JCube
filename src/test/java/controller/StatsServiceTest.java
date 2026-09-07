package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Penalty;
import model.Solve;
import model.StatSpec;

class StatsServiceTest {

    StatsService stats;

    @BeforeEach
    void setUp() {
        stats = new StatsService();
    }

    private Solve solve(long timeMs) {
        return new Solve(1, timeMs, "R U R' U'");
    }

    private Solve dnf(long timeMs) {
        Solve s = solve(timeMs);
        s.setPenalty(Penalty.DNF);
        return s;
    }

    private List<Solve> solves(Solve... solves) {
        return new ArrayList<>(List.of(solves));
    }

    // ---- averageOf ----

    @Test
    void averageOfDropsBestAndWorstThenMeansTheRest() {
        List<Solve> window = solves(solve(10000), solve(11000), solve(12000), solve(13000), solve(14000));
        assertEquals(Optional.of(12000L), stats.averageOf(window, 5));
    }

    @Test
    void averageOfDiscardsASingleDnfAsTheWorst() {
        List<Solve> window = solves(solve(10000), solve(11000), solve(12000), solve(13000), dnf(99999));
        assertEquals(Optional.of(12000L), stats.averageOf(window, 5));
    }

    @Test
    void averageOfIsDnfWhenTwoDnfsSurviveTheTrim() {
        List<Solve> window = solves(solve(10000), solve(11000), solve(12000), dnf(1), dnf(2));
        assertEquals(Optional.of(Long.MAX_VALUE), stats.averageOf(window, 5));
    }

    @Test
    void averageOfIsEmptyWhenNotEnoughSolves() {
        List<Solve> window = solves(solve(10000), solve(11000), solve(12000), solve(13000));
        assertEquals(Optional.empty(), stats.averageOf(window, 5));
    }

    @Test
    void averageOfOnlyUsesTheMostRecentSolvesInTheWindow() {
        List<Solve> allSolves = solves(
                solve(100000), solve(100000),
                solve(10000), solve(11000), solve(12000), solve(13000), solve(14000));
        assertEquals(Optional.of(12000L), stats.averageOf(allSolves, 5));
    }

    @Test
    void averageOfRejectsWindowsSmallerThanThree() {
        List<Solve> window = solves(solve(10000), solve(11000));
        assertThrows(IllegalArgumentException.class, () -> stats.averageOf(window, 2));
    }

    @Test
    void averageOfWorksForArbitraryWindowSizes() {
        List<Solve> window = solves(
                solve(1000), solve(2000), solve(3000), solve(4000), solve(5000),
                solve(6000), solve(7000), solve(8000), solve(9000), solve(10000));
        assertEquals(Optional.of(5500L), stats.averageOf(window, 10));
    }

    // ---- mean ----

    @Test
    void meanAveragesAllSolvesWithNoTrim() {
        List<Solve> all = solves(solve(10000), solve(20000), solve(30000));
        assertEquals(Optional.of(20000L), stats.mean(all));
    }

    @Test
    void meanIsDnfWhenAnySolveIsADnf() {
        List<Solve> all = solves(solve(10000), solve(20000), dnf(1));
        assertEquals(Optional.of(Long.MAX_VALUE), stats.mean(all));
    }

    @Test
    void meanIsEmptyForNoSolves() {
        assertEquals(Optional.empty(), stats.mean(new ArrayList<>()));
    }

    // ---- personalBest ----

    @Test
    void personalBestIgnoresDnfs() {
        List<Solve> all = solves(solve(15000), dnf(1), solve(9000), solve(20000));
        assertEquals(Optional.of(9000L), stats.personalBest(all));
    }

    @Test
    void personalBestIsEmptyWhenAllSolvesAreDnf() {
        List<Solve> all = solves(dnf(1), dnf(2));
        assertTrue(stats.personalBest(all).isEmpty());
    }

    @Test
    void personalBestIsEmptyForNoSolves() {
        assertFalse(stats.personalBest(new ArrayList<>()).isPresent());
    }

    // ---- compute (StatSpec dispatch) ----

    @Test
    void computeDispatchesAverageSpecToAverageOf() {
        List<Solve> window = solves(solve(10000), solve(11000), solve(12000), solve(13000), solve(14000));
        assertEquals(stats.averageOf(window, 5), stats.compute(StatSpec.average(5), window));
    }

    @Test
    void computeDispatchesMeanSpecToMean() {
        List<Solve> all = solves(solve(10000), solve(20000), solve(30000));
        assertEquals(stats.mean(all), stats.compute(StatSpec.mean(), all));
    }

    @Test
    void computeDispatchesPbSpecToPersonalBest() {
        List<Solve> all = solves(solve(15000), dnf(1), solve(9000));
        assertEquals(stats.personalBest(all), stats.compute(StatSpec.pb(), all));
    }
}
